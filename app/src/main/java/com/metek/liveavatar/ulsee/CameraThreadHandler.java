package com.metek.liveavatar.ulsee;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

public class CameraThreadHandler extends Handler {
    final static private String TAG = "CameraThreadHandler";
    private final int OPEN_CAMERA_MSG = 0;
    private final int CLOSE_CAMERA_MSG = 1;
    private final int SURFACE_SET_MSG = 2;

    private final int SHUTDOWN_MSG = 6;

    private final int START_FACEDET = 10;
    private final int STOP_FACEDET = 11;

    private WeakReference<CameraThread> mCameraThreadWeakReference;
    public CameraThreadHandler(CameraThread thread) {
        mCameraThreadWeakReference = new WeakReference<>(thread);
    }

    //messages that get called from the main thread
    public void openCamera(int width, int height) {
        sendMessage(obtainMessage(OPEN_CAMERA_MSG, width, height));
    }
    public void closeCamera() {
        sendMessage(obtainMessage(CLOSE_CAMERA_MSG));
    }
    public void setSurfaceTexture(SurfaceTexture texture) {
        sendMessage(obtainMessage(SURFACE_SET_MSG, texture));
    }
    public void startFaceDetection() {
        sendMessage(obtainMessage(START_FACEDET));
    }
    public void stopFaceDetection() {
        sendMessage(obtainMessage(STOP_FACEDET));
    }
    public void shutdown() {sendMessage(obtainMessage(SHUTDOWN_MSG));}

    @Override
    public void handleMessage(Message message) {
        int mess = message.what;
        CameraThread thread = mCameraThreadWeakReference.get();
        if (thread == null) {
            Log.w(TAG, "CameraThreadHandler: thread is null");
            return;
        }

        switch (mess) {
            case OPEN_CAMERA_MSG:
                thread.openCamera(message.arg1, message.arg2);
                break;
            case CLOSE_CAMERA_MSG:
                thread.closeCamera();
                break;
            case SURFACE_SET_MSG:
                thread.setSurfaceTexture((SurfaceTexture) message.obj);
                break;
            case SHUTDOWN_MSG:
                thread.shutdown();
                break;
            case START_FACEDET:
                thread.startFaceDet();
                break;
            case STOP_FACEDET:
                thread.stopFaceDet();
                break;
            default:
                throw new RuntimeException("unknown message id: " + mess);
        }
    }
}
