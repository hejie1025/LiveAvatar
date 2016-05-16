package com.metek.liveavatar.face.ulsee;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

/**
 * CameraThread class configuring the camera and callbacks on a separate thread.
 * Methods must be called from the thread, use the Handler to post Runnables to it, or call
 * functions in the CameraThreadHandler to send messages.
 *
 * The callbacks for face detection and the preview buffer will run on this thread.
 */

@SuppressWarnings("deprecation")
public class CameraThread extends Thread {
    private static final String TAG = "CameraThread";
    private volatile CameraThreadHandler mHandler;

    private Camera.PreviewCallback mPreviewCallback;

    final private Object mWaitReadyLock = new Object();
    private boolean mReady = false;

    private Camera mCamera = null;
    private boolean mFaceDetectionOn = false;
    private boolean mRequiresRecordingHint = false;
    private int mImageWidth, mImageHeight;

    private int mCameraOrientation;

    private byte[] mPreviewBuffer;

    private Context mContext;

    public interface OnCameraOpenListener {
        void onCameraOpen(int cameraRotation, boolean supportsFaceDetection);
    }

    private OnCameraOpenListener mOnCameraOpenLister;

    public CameraThread(Context context) {
        mContext = context;

        //recording hint for Nexus devices
        List<String> nexus4 = Arrays.asList("mako", "grouper", "tilapia", "flo", "deb", "manta");
        if (nexus4.contains(Build.DEVICE)) {
            mRequiresRecordingHint = true;
            Log.d(TAG, "This is a Nexus device, using recording hint");
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new CameraThreadHandler(this);
        synchronized (mWaitReadyLock) {
            mReady = true;
            mWaitReadyLock.notify();
        }
        Looper.loop();

        mHandler = null;
        synchronized (mWaitReadyLock) {
            mReady = false;
        }
    }

    //waits until the looper handler is ready
    public void waitUntilHandlerReady() {
        synchronized (mWaitReadyLock) {
            while (!mReady) {
                try {
                    mWaitReadyLock.wait();
                } catch (Exception e) {

                }
            }
        }
    }

    public CameraThreadHandler getHandler() {
        return mHandler;
    }

    public void setFaceDetectorListener(Camera.FaceDetectionListener listener) {
        mCamera.setFaceDetectionListener(listener);
    }

    public void setPreviewCallback(Camera.PreviewCallback listener) {
        mCamera.setPreviewCallbackWithBuffer(listener);
        mPreviewCallback = listener; //save it to set it when the preview starts/stops
    }

    public void setOnCameraOpenLister(OnCameraOpenListener listener) {
        mOnCameraOpenLister = listener;
    }

    public void openCamera(int width, int height) {
        if (mCamera != null) {
            Log.w(TAG, "Camera is open");
            return;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int k = 0; k < Camera.getNumberOfCameras(); k++) {
            Camera.getCameraInfo(k, info);
            if (info.facing == FragmentTracker.CAMERA_FACING) {
                mCamera = Camera.open(k);
                break;
            }
        }
        if (mCamera == null) {
            throw new RuntimeException("Can't open frontal camera");
        }

        mCameraOrientation = info.orientation;
        Camera.Parameters params = mCamera.getParameters();
        int detectFaces = params.getMaxNumDetectedFaces();
        if (detectFaces == 0) {
            Log.i(TAG, "Device doesn't support face detection");
        }

        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        mImageWidth = width;
        mImageHeight = height;

        Toast.makeText(mContext, "Current camera resolution: " + mImageWidth + "*" + mImageHeight, Toast.LENGTH_LONG).show();

        params.setPreviewSize(mImageWidth, mImageHeight);
        params.setPictureSize(mImageWidth, mImageHeight);
        if (mRequiresRecordingHint) {
            params.setRecordingHint(true);
        }
        mCamera.setParameters(params);

        mFaceDetectionOn = false;

        //allocate the buffer
        int yStride = (int) Math.ceil(mImageWidth / 16.0) * 16;
        int uvStride = (int) Math.ceil((yStride / 2) / 16.0) * 16;
        int ySize = yStride * mImageHeight;
        int uvSize = uvStride * mImageHeight / 2;

        mPreviewBuffer = new byte[ySize + uvSize * 2];
        mCamera.addCallbackBuffer(mPreviewBuffer);
        if (mOnCameraOpenLister != null) {
            mOnCameraOpenLister.onCameraOpen(mCameraOrientation, detectFaces > 0);
        }
    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mPreviewCallback = null;
    }

    public void setSurfaceTexture(SurfaceTexture texture) {
        if (mCamera == null) return;
        //initialise the preview
        try {
            mCamera.stopPreview(); //preview must be stopped to change the texture (not needed if
            // it was null to begin with)
            mCamera.setPreviewTexture(texture);
            // setting this callback here does nothing if texture==null, because the
            // previewTexture is required for preview to work
            mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    public void shutdown() {
        Log.d(TAG, "shutdown");
        this.closeCamera();
        Looper.myLooper().quit();
    }

    public void startFaceDet() {
        if (!mFaceDetectionOn) {
            if (mRequiresRecordingHint) {
                Camera.Parameters params = mCamera.getParameters();
                mCamera.stopPreview();
                params.setRecordingHint(false);
                mCamera.setParameters(params);
                mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
                mCamera.startPreview();
            }
            Log.d(TAG, "Device's face detection starts.");
            mCamera.startFaceDetection();
            mFaceDetectionOn = true;
        }
    }

    public void stopFaceDet() {
        if (mFaceDetectionOn) {
            if (mRequiresRecordingHint) {
                Camera.Parameters params = mCamera.getParameters();
                mCamera.stopPreview();
                params.setRecordingHint(true);
                mCamera.setParameters(params);
                mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
                mCamera.startPreview();
            }
            mCamera.stopFaceDetection();
            mFaceDetectionOn = false;
        }
    }
}