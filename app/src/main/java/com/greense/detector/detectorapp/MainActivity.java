package com.greense.detector.detectorapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    Camera camera;
    HashMap<Integer, Long> map = new HashMap<>();
    FragmentListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = (FragmentListener) this;
        startService(new Intent(this, BgService.class));
        startActivity(new Intent(this, MainActivity.class));
        getExternalFilesDir("");
        getExternalCacheDir();
        getExternalCacheDirs();
        Environment.getExternalStorageDirectory();
        Environment.getExternalStoragePublicDirectory("");
        Bitmap bmp = BitmapFactory.decodeFile("");
        getCacheDir();
        getFilesDir();
        BitmapFactory.decodeFile("");
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