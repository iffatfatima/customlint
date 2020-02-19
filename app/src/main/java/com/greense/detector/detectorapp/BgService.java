package com.greense.detector.detectorapp;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.List;

public class BgService extends Service {
    class Hello {

    }
    public BgService() {
    }

    @Override
    public void onCreate() {

            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    {

                        startActivity(new Intent(this, MainActivity.class));

                    }
                }
            }
            ;

        }

            @Override
            public IBinder onBind(Intent intent) {
                throw new UnsupportedOperationException("Not yet implemented");
            }

        }
