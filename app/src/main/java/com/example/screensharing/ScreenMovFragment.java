package com.example.screensharing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.ContentValues.TAG;

public class ScreenMovFragment extends Fragment {
    private Surface mSurface;
    private SurfaceView mSurfaceview;
    //request
    private static final String STATE_RESULT_CODE = "result_code";
    private static final String STATE_RESULT_DATA = "result_data";
    private static final int REQUEST_MEDIA_PROJECTION = 1;

    private int mScreenDensity;
    private int mResultCode;
    private Intent mResultData;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;
    private Button start;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mResultCode = savedInstanceState.getInt(STATE_RESULT_CODE);
            mResultData = savedInstanceState.getParcelable(STATE_RESULT_DATA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scrrenvalue, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mSurfaceview = (SurfaceView) view.findViewById(R.id.surface);
        mSurface = mSurfaceview.getHolder().getSurface();
        start=view.findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVirtualDisplay == null) {
                    startScreenCapture();
                } else {
                    stopScreenCapture();
                }
            }
        });
    }

    @SuppressLint("NewApi")
    private void stopScreenCapture() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        start.setText("Start");
    }

    @SuppressLint("NewApi")
    private void setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
        Log.e("medialgetdata",""+mMediaProjection);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("pause","pause");
        stopScreenCapture();
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.e("resume","resume");
//        startScreenCapture();
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("destroy","destroy");
        stopScreenCapture();
    }
    @SuppressLint("NewApi")
    private void setUpVirtualDisplay() {
        Log.e("setupvirtual",""+mMediaProjection);
        Log.i(TAG, "Setting up a VirtualDisplay: " +
                mSurfaceview.getWidth() + "x" + mSurfaceview.getHeight() +
                " (" + mScreenDensity + ")");
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                mSurfaceview.getWidth(), mSurfaceview.getHeight(), mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mSurface, null, null);
        Log.e("setupvirtualdata",""+mVirtualDisplay);
        Log.e("width",""+mSurfaceview.getWidth());
        Log.e("height",""+mSurfaceview.getHeight());
        Log.e("screendensity",""+mScreenDensity);
       // Log.e("screendensity",""+mScreenDensity);
        start.setText("stop");
    }

    @SuppressLint("NewApi")
    private void startScreenCapture() {
        Activity activity = getActivity();
        Log.e("activitycreated",""+activity);
        if (mSurface == null || activity == null) {
            return;
        }
        if (mMediaProjection != null) {
            Log.e("mediaprojectiondata",""+mMediaProjection);
            setUpVirtualDisplay();

        } else if (mResultCode != 0 && mResultData != null) {
            Log.e("mediaprojectionnull",""+mMediaProjection);
            setUpMediaProjection();
            setUpVirtualDisplay();
        } else {
            Log.e("mediascreenvalues","allowed");
            Log.i(TAG, "Requesting confirmation");
            // This initiates a prompt dialog for the user to confirm screen projection.
            startActivityForResult(
                    mMediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mMediaProjectionManager = (MediaProjectionManager)
                activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mResultData != null) {
            outState.putInt(STATE_RESULT_CODE, mResultCode);
            outState.putParcelable(STATE_RESULT_DATA, mResultData);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Log.i(TAG, "User cancelled");
                Toast.makeText(getActivity(), "User Cancelled", Toast.LENGTH_SHORT).show();
                return;
            }
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }

            Log.i(TAG, "Starting screen capture");
            mResultCode = resultCode;
            mResultData = data;
            setUpMediaProjection();
            setUpVirtualDisplay();
            Log.e("allowedresultcode",""+mResultCode);
            Log.e("allowedresultdata",""+mResultData);
        }
    }
}
