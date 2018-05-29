package com.example.iaderegg.platerecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class LtDetector extends AppCompatActivity {

    private static final String TAG = "LtDetectorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lt_detector);

        Intent intent = getIntent();
        String path = intent.getStringExtra("message");
        File plateFile = new  File((String)path);

        Log.d(TAG, ""+path);

        Bitmap myBitmap = BitmapFactory.decodeFile(plateFile.getAbsolutePath());
        Log.d(TAG, ""+path);

        ImageView plateImage = (ImageView) findViewById(R.id.plateCrop);
        plateImage.setImageBitmap(myBitmap);

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(path);
    }
}
