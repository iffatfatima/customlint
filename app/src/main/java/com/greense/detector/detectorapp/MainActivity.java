package com.greense.detector.detectorapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//NLMR
public class MainActivity extends AppCompatActivity {
    @Override
    public void onStop() {
        stopService(new Intent(this, BgService.class));

        super.onStop();
    }

    public static SQLiteDatabase db = SQLiteDatabase.create(null);//ERB
    public static SQLiteDatabase db1;
    Camera camera;//RL
    android.graphics.Camera cam;
    MediaPlayer mediaPlayer;//RL
    private FragmentListener listener;//LC
    Handler myHandler;//LT	@Override

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (int i = 0; i < 10; i++) {
                Toast.makeText(context, i + " seconds gone!", Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int j = i; j < 40; j++) {
                    while (j < 10) {
                        if (i % 2 == 0) {
                            System.out.print(i + " " + j);
                        } else if (i % 3 == 0) {
                            System.out.print(i + " " + j);
                        } else {
                            System.out.print(i + " " + j);
                        }
                    }
                }
                int a = 10;
                int b = 20;
                if (a + b < 2) {
                    a++;
                }
                if (a + b == 2) {
                    a--;
                }
                if (a + b < 2) {
                    a++;
                }
                if (a + b == 2) {
                    a--;
                }
                if (a + b < 2) {
                    a++;
                }
                if (a + b == 2) {
                    a--;
                }
                if (a + b < 2) {
                    a++;
                }
                if (a + b == 2) {
                    a--;
                }
                if (a + b < 2) {
                    a++;
                }
                if (a + b == 2) {
                    a--;
                }
                if (a + b < 2) {
                    a++;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLiteDatabase db = SQLiteDatabase.create(null);
        listener = (FragmentListener) this;
        startService(new Intent(this, BgService.class));//VBS
        startActivity(new Intent(this, MainActivity.class));
        getExternalFilesDir("");//PD
        getExternalCacheDir();//PD
        getExternalCacheDirs();//PD
        Environment.getExternalStorageDirectory();//PD
        Environment.getExternalStoragePublicDirectory("");//PD
        Bitmap bmp = BitmapFactory.decodeFile("");//DTWC
        getCacheDir();
        Handler handler;
        getFilesDir();
        BitmapFactory.decodeFile("");

    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
