package com.greense.detector.detectorapp;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

class CustomView extends View {

    public CustomView(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        invalidate();
        invalidate(1,2,3,4);
        return false;
    }

}