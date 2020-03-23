package com.greense.detector.detectorapp;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    public static SQLiteDatabase db = SQLiteDatabase.create(null);
    public static SQLiteDatabase db1;
    Camera camera;
    HashMap<Integer, Long> map = new HashMap<>();
    FragmentListener listener;
    Handler handler;


    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLiteDatabase db = SQLiteDatabase.create(null);
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
        Handler handler;
        getFilesDir();
        BitmapFactory.decodeFile("");
        File file;
        new File("fileName.txt");

    }

    @Override
    public void onStart() {
        Log.d("", "");

        super.onStart();
//        camera = Camera.open();

    }

    @Override
    public void onTrimMemory(int level) {
        //todo: Free memory here
    }
}



    /*@Override
    public void onStop(){
        if(handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onStop();
    }*/