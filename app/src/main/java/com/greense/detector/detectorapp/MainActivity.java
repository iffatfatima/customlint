package com.greense.detector.detectorapp;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    Camera camera;
    SparseArray<Long> map = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onStart() {
        Log.d("", "");

        super.onStart();
        camera = Camera.open();
    }

    @Override
    public void onStop() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        super.onStop();
    }
}



    /*@Override
    public void onStop(){
        if(handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onStop();
    }*/