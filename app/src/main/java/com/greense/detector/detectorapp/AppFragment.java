package com.greense.detector.detectorapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class AppFragment extends Fragment {

    @Override
    public void onStop() {
        mListener = null;
        super.onStop();
    }

    private OnFragmentInteractionListener mListener;

    public static AppFragment newInstance() {
        return new AppFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app, container, false);
    }

    public void onButtonPressed(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Bitmap bitmap = Bitmap.createBitmap(1, 2, Bitmap.Config.ARGB_8888, false);
        }
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
