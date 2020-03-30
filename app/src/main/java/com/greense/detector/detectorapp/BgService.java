package com.greense.detector.detectorapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BgService extends Service {
    class Hello {

    }
    public BgService() {
    }

    @Override
    public void onCreate() {

        startActivity(new Intent(this, MainActivity.class));//IB

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
