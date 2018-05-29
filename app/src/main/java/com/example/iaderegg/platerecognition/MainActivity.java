package com.example.iaderegg.platerecognition;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencv.core.Core.FILLED;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    Intent intent_ltdetector;
    JavaCameraView javaCameraView;
    Mat mRgba, imgGray, imgCanny, imgThreshold, mHierarchy,cropImage;
    private static int counter = 0;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV exitosamente cargado");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "OpenCV no ha cargado exitosamente");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        }

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        imgGray = new Mat(height, width, CvType.CV_8UC1);
        imgThreshold = new Mat(height, width, CvType.CV_8UC1);
        imgCanny = new Mat(height, width, CvType.CV_8UC1);
        mHierarchy = new Mat();


    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        File path;
        String path_name = "";
        // Captura en RGB
        mRgba = inputFrame.rgba();
        // Conversión a escala de grises
        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);
        // Remuestreo factor de 4
        org.opencv.core.Size s = new Size(3,3);
        // Filtro Gaussiano
        Imgproc.GaussianBlur(imgGray, imgGray, s, 2);
        // Filtro Laplaciano
        Imgproc.Laplacian(imgGray, imgGray, CvType.CV_8U, 3, 1, 0);

        Imgproc.threshold(imgGray, imgThreshold, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<MatOfPoint> contours_selected = new ArrayList<MatOfPoint>();

        Imgproc.findContours(imgThreshold, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = 0;
        int idxMax = 0;



        for (int i = 0; i < contours.size(); i++) {
            double rozmiar = Math.abs(Imgproc.contourArea(contours.get(i)));
            if (rozmiar > maxArea) {
                maxArea = rozmiar;
                idxMax = i;
            }
        }

        if (contours.size() >= 1 && counter < 5) {
            Rect r = Imgproc.boundingRect(contours.get(idxMax));
            Log.d(TAG, "X:: "+r.x+" Y:: "+r.y+" Ratio:: "+r.width/r.height+" Height:: "+r.height+" Width:: "+r.width);
            if(r.width/r.height > 1.8 && r.width/r.height < 2.2){
                Imgproc.rectangle(mRgba, r.tl(), r.br(), new Scalar(255, 0, 0, 255), 3, 8, 0); //draw rectangle

                path = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/PlateRecognition/");
                path.mkdirs();
                File file = new File(path, "plate.jpg");
                String filename = file.toString();

                Mat ROI = mRgba.submat(r);

                boolean result = Imgcodecs.imwrite(filename+"", ROI);
                counter++;

                if(counter >= 4){
                    intent_ltdetector =new Intent(this, LtDetector.class);
                    intent_ltdetector.putExtra("message",Environment.getExternalStorageDirectory() + "/DCIM/Camera/PlateRecognition/plate.png");
                    mRgba.release();
                    startActivity(intent_ltdetector);

                }
            }

        }



        return mRgba;
    }

}

