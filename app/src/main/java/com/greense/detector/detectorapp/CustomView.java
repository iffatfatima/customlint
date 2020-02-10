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
        //TODO: call invalidate on a specific Rect instead of redrawing the complete view as per your use case
        invalidate();
        return false;
    }

}