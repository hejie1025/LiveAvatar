package com.metek.liveavatar.face.ulsee;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.metek.liveavatar.R;
import com.uls.multifacetrackerlib.UlsMultiTracker;
import com.uls.multifacetrackerlib.UlsTrackerMode;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("deprecation")
public class FragmentTracker extends Fragment
        implements InputSurfaceView.OnInputSurfaceEventHandler,
        Camera.PreviewCallback, Camera.FaceDetectionListener,
        CameraThread.OnCameraOpenListener,
        SurfaceHolder.Callback, RenderThread.OnSurfaceTextureUpdatedListener {
    final private static String TAG = "FragmentTracker";

    private final static int CAMERA_PREVIEW_SIZE_MAX = 2000 * 2000,
                            CAMERA_PREVIEW_SIZE_MIN = 640 * 480;
    public final static int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private static final String ACTIVATION_KEY = "3F2pYg29sdE4BVA2hYPERF5bclc7eMhL";

    private LinearLayout mInfoLayout;

    private int mCameraWidth = 640, mCameraHeight = 480;
    private int mCameraRotation = 90;
    private int mDisplayRotation;
    CameraThread cameraThread;
    RenderThread renderThread;

    //this variable mirrors the status of the face detector in the camera thread.
    //it may fall behind the updates on that thread, but a frame or two are not a problem.
    private boolean mFaceDetectionOn = false, mSupportsFaceDetection;
    android.graphics.Matrix mCameraMatrix;

    Handler mMainHandler;
    TextView mProcTimeLabel;

    UlsMultiTracker mTracker;
    final int mMaxTrackers = 1;

    private float [][] mShape, mConfidence;
    private float[][] mPose;
    private float[] mPoseQuality;
    private float[] [] mPupils, mGaze;

    // Modified.
    private long mTimeDoFaceDet = 0;
    private boolean mbFaceDetectionThreadRunning = false;

    public FragmentTracker() {
        // Required empty public constructor

        //get the main loop handler
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tracker, container, false);
        InputSurfaceView isv = (InputSurfaceView) view.findViewById(R.id.surfaceView);
        isv.setInputEventHandler(this);
        isv.getHolder().addCallback(this);

        mProcTimeLabel = (TextView)view.findViewById(R.id.textView1);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        menu.findItem(R.id.stickySwitch).setChecked(mTracker.getSticky());
        menu.findItem(R.id.highPrecision).setChecked(mTracker.getHighPrecision());
        UlsTrackerMode mode = mTracker.getTrackMode();
        if (mode == UlsTrackerMode.TRACK_COMBINED
                || mode == UlsTrackerMode.TRACK_FACE_AND_POSE) {
            menu.findItem(R.id.showPose).setChecked(true);
        }
        if (mode == UlsTrackerMode.TRACK_COMBINED
                || mode == UlsTrackerMode.TRACK_FACE_AND_PUPILS) {
            menu.findItem(R.id.showPupilsGaze).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        UlsTrackerMode trackMode = mTracker.getTrackMode();

        switch (item.getItemId()) {
            case R.id.info:
                if(mInfoLayout.getVisibility() == View.VISIBLE)
                    mInfoLayout.setVisibility(View.INVISIBLE);
                else {
                    mInfoLayout.setVisibility(View.VISIBLE);
                    mInfoLayout.bringToFront();
                }
                return true;
            case R.id.stickySwitch:
                final boolean sticky = !item.isChecked();
                mTracker.setSticky(sticky);
                item.setChecked(sticky);
                return true;
            case R.id.highPrecision:
                final boolean hp = !item.isChecked();
                mTracker.setHighPrecision(hp);
                item.setChecked(hp);
                return true;
            case R.id.showPupilsGaze:
                final boolean pupils = !item.isChecked();
                item.setChecked(pupils);
                if (pupils) {
                    switch (trackMode) {
                        case TRACK_FACE:
                            trackMode = UlsTrackerMode.TRACK_FACE_AND_PUPILS;
                            break;
                        case TRACK_FACE_AND_POSE:
                            trackMode = UlsTrackerMode.TRACK_COMBINED;
                            break;
                    }
                } else {
                    switch (trackMode) {
                        case TRACK_FACE_AND_PUPILS:
                            trackMode = UlsTrackerMode.TRACK_FACE;
                            break;
                        case TRACK_COMBINED:
                            trackMode = UlsTrackerMode.TRACK_FACE_AND_POSE;
                            break;
                    }
                }
                mTracker.setTrackMode(trackMode);
                return true;
            case R.id.showPose:
                final boolean pose = !item.isChecked();
                item.setChecked(pose);
                if (pose) {
                    switch (trackMode) {
                        case TRACK_FACE:
                            trackMode = UlsTrackerMode.TRACK_FACE_AND_POSE;
                            break;
                        case TRACK_FACE_AND_PUPILS:
                            trackMode = UlsTrackerMode.TRACK_COMBINED;
                            break;
                    }
                } else {
                    switch (trackMode) {
                        case TRACK_FACE_AND_POSE:
                            trackMode = UlsTrackerMode.TRACK_FACE;
                            break;
                        case TRACK_COMBINED:
                            trackMode = UlsTrackerMode.TRACK_FACE_AND_PUPILS;
                            break;
                    }
                }
                mTracker.setTrackMode(trackMode);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");

        super.onAttach(activity);

        mDisplayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        this.initialiseTracker(activity);

        cameraThread = new CameraThread(activity);
        cameraThread.setName("Camera and processing thread");
        cameraThread.setOnCameraOpenLister(this);
        cameraThread.start();

        renderThread = new RenderThread();
        renderThread.setName("Rendering thread");
        renderThread.start();
    }

    @Override
    public void onResume() {
        super.onResume();

        determineCameraResolution();

        cameraThread.waitUntilHandlerReady();
        CameraThreadHandler cth = cameraThread.getHandler();
        cth.openCamera(mCameraWidth, mCameraHeight);
        cth.post(new Runnable() {
            @Override
            public void run() {
                cameraThread.setFaceDetectorListener(FragmentTracker.this);
                cameraThread.setPreviewCallback(FragmentTracker.this);
            }
        });

        mCameraMatrix = new android.graphics.Matrix();
        mCameraMatrix.postScale(mCameraWidth / 2000f, mCameraHeight / 2000f);
        mCameraMatrix.postTranslate(mCameraWidth / 2f, mCameraHeight / 2f);


        //force a reset of the surface texture
        RenderThreadHandler handler = renderThread.getHandler();
        if (handler != null) {
            handler.resetSurfaceTextureToListener();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        CameraThreadHandler handler = cameraThread.getHandler();
        handler.closeCamera();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        CameraThreadHandler handler = cameraThread.getHandler();
        handler.shutdown();

        try {
            cameraThread.join();
        } catch (Exception e) {
            Log.e(TAG, "Error joining camera thread: " + e.getLocalizedMessage());
        }

        RenderThreadHandler rth = renderThread.getHandler();
        rth.shutdown();

        try {
//            cameraThread.join();
            renderThread.join();
        } catch (Exception e) {
            Log.e(TAG, "Error joining render thread: " + e.getLocalizedMessage());
        }
//        cameraThread = null;
//        renderThread = null;
    }

    @Override
    public void onInputSurfaceSingleTapHandler(MotionEvent event) {
/*
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            CameraThreadHandler handler = cameraThread.getHandler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Resetting trackers");
                    for (int i = 0; i < mMaxTrackers; i++)
                        mTracker.resetTracker(i);
                    cameraThread.startFaceDet();
                }
            });
        }
*/
    }


    public void onProcessingTimeUpdate(final float millisPerFrame) {
        //we don't know where this is coming from, so run it on the main thread
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (millisPerFrame <= 0) {
                    mProcTimeLabel.setText("Proc. time: - ms");
                } else {
                    mProcTimeLabel.setText(String.format("Proc. time: %.2f ms", millisPerFrame));
                }
            }
        });
    }

    // these methods are here and not in the RenderThread because we want to send messages to the
    // RenderThreadHandler -> this way the GL contexts are created in the RenderThread and not the
    // main thread.

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //pass this surfaceHolder to the renderer
        renderThread.waitUntilHandlerReady();

        renderThread.setOnSurfaceTextureListener(this);

        RenderThreadHandler handler = renderThread.getHandler();
        if (handler != null) {
            handler.sendSurfaceCreated(surfaceHolder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, final int i1, final int i2) {
        Log.d(TAG, "Surface size: " + i1 + "x" + i2);
        RenderThreadHandler handler = renderThread.getHandler();
        if (handler != null) handler.sendSurfaceChanged(surfaceHolder, i1, i2);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "onSurfaceDestroyed");

        RenderThreadHandler handler = renderThread.getHandler();
        if (handler != null) {
            handler.sendSurfaceDestroyed(surfaceHolder);
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        //get the handler and send a message, so the actual update is made on the
        // cameraThread and not on the renderThread.

        CameraThreadHandler handler = cameraThread.getHandler();
        if (handler != null) handler.setSurfaceTexture(texture);
    }

    /******************************************/
    //  Tracker functions. The callbacks run in the thread of the video stream source

    private void initialiseTracker(Context context) {
        mTracker = new UlsMultiTracker(context, mMaxTrackers,
                UlsMultiTracker.UlsTrackerInterfaceType.NV21_BYTEARRAY);
        boolean activation = mTracker.activate(ACTIVATION_KEY);
        if (!activation) {
//            throw new RuntimeException("ULSee Multi-Face Tracker activation failed. Please check your key.");
            Toast.makeText(getActivity(), "Activation key failed.", Toast.LENGTH_LONG).show();
        } else
            mTracker.initialise();

        mShape = new float[mMaxTrackers][];
        mConfidence = new float[mMaxTrackers][];
        mPose = new float[mMaxTrackers][];
        mPupils = new float[mMaxTrackers][];
        mGaze = new float[mMaxTrackers][];
        mPoseQuality = new float[mMaxTrackers];
    }

    private long mTimingCounter = 0;
    private int mTimingMS = 0;

    @Override
    public void onPreviewFrame(final byte[] bytes, Camera camera) {
        long t0 = System.currentTimeMillis();

        int alive = 0;

//        if (!mbFaceDetectionThreadRunning)
            alive = mTracker.update(bytes, mCameraWidth, mCameraHeight, UlsMultiTracker.ImageDataType.NV21);

        long t1 = System.currentTimeMillis();

        if (camera != null) camera.addCallbackBuffer(bytes);

        mTimingCounter++;
        mTimingMS += (t1 - t0);

        if (mTimingCounter == 10) {
            // update label on the main thread
            this.onProcessingTimeUpdate(mTimingMS / (float) mTimingCounter);
            mTimingCounter = 0;
            mTimingMS = 0;
        }

        Log.d(TAG, "FragmentTracker.onPreviewFrame(): detected faces: " + String.valueOf(alive) + ", max faces: " + String.valueOf(mMaxTrackers));

        // check if we have a non-empty shape
        if (alive < mMaxTrackers) {
            if (mSupportsFaceDetection) {
                if (!mFaceDetectionOn) {
                    try {
                        CameraThreadHandler cth = cameraThread.getHandler();
                        if (cth != null) cth.startFaceDetection();
                        mFaceDetectionOn = true;
                    } catch (Exception e) {
                        //the camera thread may be called right after it's destroyed, when the app is moving to the background).
                    }
                }
            } else {
                //run the face detection every x frames to avoid slowing things down too much
                //the camera rotation is required here!!
                if (t0 - mTimeDoFaceDet >= 500 && !mbFaceDetectionThreadRunning) {

                    mbFaceDetectionThreadRunning = true;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean b = mTracker.findFacesAndAdd(bytes, mCameraWidth, mCameraHeight, mCameraRotation, UlsMultiTracker.ImageDataType.NV21);

//                            Log.d(TAG, "UlsMultiTracker.findFacesAndAdd() returns " + String.valueOf(b));

                            mbFaceDetectionThreadRunning = false;
                            mTimeDoFaceDet = System.currentTimeMillis();
                        }
                    }).start();
                }
            }
        } else {
            if (mFaceDetectionOn && mSupportsFaceDetection) {
                try {
                    CameraThreadHandler ch = cameraThread.getHandler();
                    if (ch != null) ch.stopFaceDetection();
                    mFaceDetectionOn = false;
                } catch (Exception e) {
                    //the camera thread may be called right after it's destroyed, when the app is moving to the background).
                }
            }
        }

        if (alive > 0) {
            for (int k = 0; k < mMaxTrackers; k++) {
                mShape[k] = mTracker.getShape(k);
                mConfidence[k] = mTracker.getConfidence(k);
                float[] xy = mTracker.getTranslationInImage(k);
                if (xy != null) {
                    this.mPose[k] = new float[6];
                    float[] angles = mTracker.getRotationAngles(k);
                    mPose[k][0] = angles[0];
                    mPose[k][1] = angles[1];
                    mPose[k][2] = angles[2];
                    mPose[k][3] = xy[0];
                    mPose[k][4] = xy[1];
                    mPose[k][5] = mTracker.getScaleInImage(k);
                } else {
                    mPose[k] = null;
                }
                mPoseQuality[k] = mTracker.getPoseQuality(k);
                mGaze[k] = mTracker.getGaze(k);
                mPupils[k] = mTracker.getPupils(k);
            }
        } else {
            for (int k = alive; k < mMaxTrackers; k++) {
                mShape[k] = null;
                mConfidence[k] = null;
                this.mPose[k] = null;
                mPoseQuality[k] = 0.0f;
                mGaze[k] = null;
                mPupils[k] = null;
            }
        }

        renderThread.updateResults(mShape, mConfidence, mPose, mPoseQuality,
                mPupils, mGaze);
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        Log.d(TAG, "FragmentTracker.onFaceDetection()");

        if (faces.length > 0) {
            Log.d(TAG, "number of faces: " + String.valueOf(faces.length));

            RectF[] rects = new RectF[faces.length];
            int[] rotations = new int[faces.length];
            int j = 0;
            for (int i = 0; i < faces.length; i++) {
//                if (faces[i].score > 50) {
                    RectF ro = new RectF(faces[i].rect);
                    mCameraMatrix.mapRect(ro);

                    // Unlike iOS, Android's face detector doesn't give you the roll of the face
                    // Some devices support eye/mouth detection, which could be used to guess the roll.
                    // the camera rotation is used by default if the eyes are not available
//                    int rotation = mCameraRotation;
//                    if (faces[i].leftEye != null && faces[i].rightEye != null) {
//                        // compute rotation here
//                    }

                    float ratio = ro.width() / ro.height();
                    if (ratio > 2.0f) return;
                    if (ratio < 0.5f) return;

                    rects[j] = ro;
                    rotations[j] = mCameraRotation;
                    j++;
//                }
            }
            if (j > 0) {
                boolean ok = mTracker.addFaces(Arrays.copyOf(rects, j),
                        Arrays.copyOf(rotations, j));

                Log.d(TAG, "UlsMultiTracker.addFaces() returns " + String.valueOf(ok) + "; rotation is " + mCameraRotation);

//                if (!ok) {
//                    throw new RuntimeException("Error adding faces");
//                }
             }
        }
    }

    private int computeCameraRotation(int orientation, int displayRotation) {
        int degrees = 0;
        switch (displayRotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        result = (orientation + degrees) % 360;
        result = (360 - result) % 360;  // compensate the mirror

        return result;
    }

    @Override
    public void onCameraOpen(int cameraOrientation, boolean supportsFaceDetection) {
        mCameraRotation = computeCameraRotation(cameraOrientation,
                mDisplayRotation);
//        mSupportsFaceDetection = supportsFaceDetection;
        mSupportsFaceDetection = false;   // Don't use device's face detection.
        Log.d(TAG, "onCameraOpen, cameraOrientation: " + cameraOrientation + " " + mCameraRotation);
        if (mSupportsFaceDetection) Log.d(TAG, "Supports face detection.");
        else Log.d(TAG, "No face detection.");
        //we need to send this to the renderer.
        RenderThreadHandler handler = renderThread.getHandler();
        if (handler != null) {
            handler.sendCameraImageSize(mCameraWidth, mCameraHeight);
            handler.sendCameraRotation(mCameraRotation);
        }
    }

    private void determineCameraResolution() {
        Camera camera = null;
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == CAMERA_FACING) {
                camera = Camera.open(i);
                break;
            }
        }
        if (camera == null) {
            throw new RuntimeException("Can't open camera");
        }

        Camera.Parameters params = camera.getParameters();

        // Detect highest camera resolution.
        Camera.Size bestPreviewSize = getBestSize(CAMERA_PREVIEW_SIZE_MAX, CAMERA_PREVIEW_SIZE_MIN, params);

        mCameraWidth = bestPreviewSize.width;
        mCameraHeight = bestPreviewSize.height;

        camera.release();
    }

    private Camera.Size getBestSize(int iSizeMax, int iSizeMin, Camera.Parameters camParas) {
        Camera.Size sizeBest = null;
        int areaBestSize = 0;

        List<Camera.Size> listSize = camParas.getSupportedPreviewSizes();

        for (Camera.Size s : listSize) {
            int area = s.width * s.height;
            if (area > iSizeMax || area < iSizeMin)
                continue;
            if (Math.abs(1.0 * s.height / s.width - 9.0 / 16.0) < 0.01)
                continue;
            if (area > areaBestSize) {
                // Record this as the best size.
                sizeBest = s;
                areaBestSize = area;
            }
        }

        return sizeBest;
    }
}
