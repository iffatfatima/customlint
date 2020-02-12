package com.greense.detector.detectorapp;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    Camera camera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsyncTask task = new MyTask();
        task.execute();
    }

    static class MyTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            return null;
        }
    };
    @Override
    public void onStart(){
        Log.d("","");

        super.onStart();
        camera = Camera.open();
    }

    @Override
    public void onStop(){
        if (camera != null){
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