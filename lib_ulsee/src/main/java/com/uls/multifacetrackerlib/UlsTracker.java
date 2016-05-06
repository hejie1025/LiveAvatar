package com.uls.multifacetrackerlib;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

/**
 *  This class implements a fast and sparse tracker.
 *
 *  <br>
 *  This version uses OpenGL to do image processing on the input image. There are two options of
 *  initialising the tracker:
 *  <p> <ul>
 *      <li> the user creates an OpenGL texture and a SurfaceTexture with that texture,
 *      and initialises the class with that OpenGL texture.
 *      <li> the class creates an OpenGL texture and a
 *      {@link android.graphics.SurfaceTexture SurfaceTexture} with it. In both cases, the user
 *      is responsible for updating the contents of the OpenGL texture through
 *      the SurfaceTexture by using as preview surface for the Camera class (or another
 *      compatible source).
 *  <br></br>
 *  </ul>
 *  The user of the tracker must manage the updates of the OpenGL texture backing the
 *  SurfaceTexture. When calling {@link #update}, no other thread must be accessing
 *  the OpenGL texture.
 */

public class UlsTracker {
    static private final String TAG = "UlsTracker";

    private EGL10 mEGL = null;
    private int mSrcTextureName = -1;
    private SurfaceTexture mSurfaceTexture = null;

    private boolean mInitialised = false;
    private boolean mIsTracking = false;

    private boolean mPredictPupils = false, mPoseEnabled = false;
    private boolean mHighPrecision = false, mSticky = true;
    private UlsTrackerMode mTrackMode = UlsTrackerMode.TRACK_FACE;


    public UlsTracker(final Context context) {
        boolean ok = naInitialiseFromAssets(context.getAssets(), context.getCacheDir().getAbsolutePath());
        if (ok) {
           createVariables();
        } else {
            throw new RuntimeException("Can't initialise tracker");
        }
    }

    public UlsTracker(final String path) {
        boolean ok = naInitialiseFromPath(path);
        if (ok) {
           createVariables();
        } else {
            throw new RuntimeException("Can't initialise tracker from path " + path);
        }
    }

    private void createVariables() {
        mShape = new float[2*mShapePointCount];
        mShape3D = new float[3*mShapePointCount];
        mConfidence = new float[mShapePointCount];
        mPose = new float[6];
        mPose[0] = -1000;
        mEulerAngles = new float[3];
        //mPoseQuality = 0;
        mPupils = new float[4];
        mPupils[0] = -1000;
        mGaze = new float[6];
        mGaze[0] = -1000;
    }

    /**
     * Initialise the tracker with a previously created OpenGL texture
     *
     * <p> The current EGLContext will be used to initialised some OpenGL objects inside the tracker.
     * This EGLContext must be the current context when the {@link #update update} and
     * {@link #resetTracker resetTracker} methods are called.
     *
     * @param srcOpenGLTexture A valid OpenGL texture name in the current EGLContext
     * @param inputImageWidth Width of the input texture
     * @param inputImageHeight Height of the input texture
     * @return true if the initialisation succeeded.
     */
    public boolean initialise(int srcOpenGLTexture, int inputImageWidth, int inputImageHeight) {
        mEGL = (EGL10)EGLContext.getEGL();
        EGLContext context = mEGL.eglGetCurrentContext();
        if (context == EGL10.EGL_NO_CONTEXT || srcOpenGLTexture < 0) {
            throw new RuntimeException("No current EGL context, or texture < 0");
        }
        mSrcTextureName = srcOpenGLTexture;
        mSurfaceTexture = null;
        mInitialised = naSetupOpenGL(mSrcTextureName, inputImageWidth, inputImageHeight);
        return mInitialised;
    }

    /**
     * Activates the tracker with a key
     * @param key A string containing a valid key
     * @return true if the key is valid, false otherwise.
     */
    public boolean activate(String key) {
        return naActivate(key);
    }

    /**
     * Signal the tracker that the Activity is being paused, and the EGLContext is expected
     * to become invalid.
     *
     * <p>A call to {@link #initialise(int, int, int)} must be
     * made before using the tracker again</p>
     */
    public void onPause() {
        mInitialised = false;
        mIsTracking = false;
        naEGLContextInvalidated();
    }

//region getters and setters
    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public int getSrcTextureName() {
        return mSrcTextureName;
    }

    public int getNumberOfPoints() {
        return mShapePointCount;
    }

    public float[] getShapeTest() {
        if (!mInitialised)
            throw new RuntimeException("Tracker has not been initialised");
        if (!naTestShapeReset()) return new float[0];
        return mShape;
    }

    public float[] getShape() {
        if (!mInitialised)
            throw new RuntimeException("Tracker has not been initialised");
        if (mIsTracking) return mShape;
        else return null;
    }
    public float[] getShape3D() {
        if (!mInitialised)
            throw new RuntimeException("Tracker has not been initialised");
        if (mIsTracking) return mShape3D;
        else return null;
    }
    public float[] getConfidence() {
        if (!mInitialised)
            throw new RuntimeException("Tracker has not been initialised");
        if (mIsTracking) return mConfidence;
        else return null;
    }
    /**
     * Rotation angles
     * @return A float[3] array with {pitch, yaw, roll} angles, or null if not available
     */
    public float[] getRotationAngles() {
        if (!mInitialised)
            throw new RuntimeException("Tracker has not been initialised");
        if (mIsTracking && mPoseEnabled) return Arrays.copyOf(mEulerAngles, mEulerAngles.length);
        return null;
    }

    /**
     * X- and Y- translation in the image
     * @return a float[2] vector, or null if not available
     */
    public float[] getTranslationInImage() {
        if (!mInitialised)
            throw new RuntimeException("Tracker has not been initialised");
        if (mIsTracking && mPoseEnabled) return Arrays.copyOfRange(mPose, 3, 5);
        return null;
    }
    /**
     * Weak-perspective camera model's scale parameter
     * @return scale in image, or -1.0f if the pose is not available.
     */
    public float getScaleInImage() {
        if (!mInitialised)
            throw new RuntimeException("Tracker has not been initialised");
        if (mIsTracking && mPoseEnabled) return mPose[5];
        return 0.0f;
    }

    public float getPoseQuality() {
        if (!mInitialised)
            throw new RuntimeException("Tracker has not been initialised");
        if (mIsTracking && mPoseEnabled) return mPoseQuality;
        return 0.0f;
    }
    public float[] getPupils() {
        if (!mInitialised)
            throw new RuntimeException("Tracker has not been initialised");
        if (mIsTracking && mPredictPupils) {
            if (mPupils[0] <= -100) return null;
            return mPupils;
        }
        return null;
    }

    /**
     * Gaze values
     * @return A float[6] array (x,y,z)(x,y,z), or null.
     */
    public float[] getGaze() {
        if (!mInitialised)
            throw new RuntimeException("Tracker has not been initialised");
        if (mIsTracking && mPredictPupils) {
            if (mGaze[0] <= -100) return null;
            return mGaze;
        }
        return null;
    }

    public void setTrackMode(UlsTrackerMode mode) {
        mTrackMode = mode;
        switch (mode) {
            case TRACK_COMBINED:
                mPredictPupils = true;
                mPoseEnabled = true;
                break;
            case TRACK_FACE_AND_PUPILS:
                mPredictPupils = true;
                mPoseEnabled = false;
                break;
            case TRACK_FACE:
                mPoseEnabled = mPredictPupils = false;
                break;
            case TRACK_FACE_AND_POSE:
                mPredictPupils = false;
                mPoseEnabled = true;
                break;
        }
    }
    public UlsTrackerMode getTrackMode() { return mTrackMode; }
    public void setHighPrecision(boolean highPrecision) {
        mHighPrecision = highPrecision;
    }
    public boolean getHighPrecision() { return mHighPrecision; }
    public void setSticky(boolean sticky) {
        mSticky = sticky;
    }
    public boolean getSticky() { return mSticky; }
//endregion

    /**
     * Reset the tracker with the given location of the face.
     *
     * @param x The x-coordinate of the top-left corner of the rectangle containing the face
     * @param y The y-coordinate of the top-left corner of the rectangle containing the face
     * @param width Width of the rectangle containing the face. Set to <= 0 if the value should be
     *              ignored
     * @param height Height of the rectangle containing the face. Set to <= 0 if the value should be
     *               ignored
     * @param rotation Rotation of the face. Either 0, 90, 180 or 270.
     * @return true if the resetting succeeds.
     */
    public boolean resetWithFaceRect(int x, int y, int width, int height, int rotation) {
        if (!mInitialised) return false;
        if (width > 0 && height > 0) {
            return naResetWithFace(x, y, width, height, rotation);
        }
        return false;
    }

    /**
     * Reset the tracker by searching the image for a face
     *
     * <p>Some older Android devices do not support face detection in the Camera API. This method
     * searches the input image for a face using OpenCV and resets the tracker if a face is found.
     *
     * <p>This method must be called from the thread where the object was initialised.
     * @param rotation Rotation of the camera. Either 0, 90, 180 or 270.
     * @return True if a face is found, false otherwise
     */
    public boolean resetTracker(int rotation) {
        if (!mInitialised) throw new RuntimeException("Tracker has not been initialised");
        boolean wasReset = naFindFaceAndReset(rotation);
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "After face detection, glerror: 0x" + Integer.toHexString(error));
        }
        return wasReset;
    }

    /**
     * Update the shape location.
     *
     * <p>This method must be called from the thread where the object was initialised.
     * @return true if the tracker is tracking, false if it needs resetting.
     */
    public boolean update() {
        if (!mInitialised) throw new RuntimeException("Tracker has not been initialised");
        mIsTracking = naUpdateShape(mPredictPupils, mHighPrecision, mSticky);
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "After feature setup, glerror: 0x" + Integer.toHexString(error));
        }
        return mIsTracking;
    }

    private float[] mShape, mShape3D, mConfidence;
    private float[] mPose, mEulerAngles; //mPose is Rodrigues + translation
    private float[] mPupils, mGaze;
    private float mPoseQuality;
    private int mShapePointCount = 0;
    private long nativeTrackerPtr = 0;

    private native boolean naInitialiseFromAssets(AssetManager manager, String cacheDir);
    private native boolean naInitialiseFromPath(String path);
    private native boolean naSetupOpenGL(int srcTextureName, int width, int height);
    private native void naEGLContextInvalidated();
    private native boolean naResetWithFace(int x, int y, int width, int height, int rotation);
    private native boolean naFindFaceAndReset(int rotation);
    private native boolean naUpdateShape(boolean predictPupils, boolean highPrecision,
                                         boolean smooth);
    private native boolean naActivate(String key);

    private native boolean naTestShapeReset();
    static {
        System.loadLibrary("ulsTracker_native");
    }
}

