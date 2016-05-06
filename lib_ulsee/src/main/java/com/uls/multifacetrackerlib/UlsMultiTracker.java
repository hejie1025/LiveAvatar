package com.uls.multifacetrackerlib;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.Log;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

public class UlsMultiTracker {
    static private final String TAG = "UlsMultiTracker";

    public enum UlsTrackerInterfaceType { OPENGL_TEXTURE, NV21_BYTEARRAY };

    // Modified.
    public enum ImageDataType {
        NV21(1), ARGB(2);

        private final int miValue;

        ImageDataType(int value) {
            miValue = value;
        }

        public int getValue() {
            return miValue;
        }
    };

    private EGL10 mEGL = null;
    private int mSrcTextureName = -1;

    private UlsTrackerInterfaceType mAPI;

    private int mTrackerCount;
    private boolean[] mAlive;

    private boolean mInitialised = false;

    private boolean mPredictPupils = false, mPoseEnabled = false;
    private boolean mHighPrecision = false, mSticky = true;
    private UlsTrackerMode mTrackMode = UlsTrackerMode.TRACK_FACE;

    public UlsMultiTracker(final Context context, int trackerCount, UlsTrackerInterfaceType
            apiType) {
        mAPI = apiType;
        if (mAPI == UlsTrackerInterfaceType.NV21_BYTEARRAY) mInitialised = true;
        Log.i(TAG, "External folder: " + context.getExternalFilesDir(null));
//        context.getExternalFilesDir("/").mkdirs();

        mTrackerCount = Math.max(trackerCount, 2);
        if (mTrackerCount != trackerCount) {
            Log.i(TAG, "Max number of trackers is " + mTrackerCount);
        }

        mShapePointCount = new int[mTrackerCount];
        mAlive = new boolean[mTrackerCount];
        Arrays.fill(mShapePointCount, 0);
        Arrays.fill(mAlive, false);

        boolean ok = naMultiInitialiseFromAssets(mTrackerCount, mAPI == UlsTrackerInterfaceType
                        .OPENGL_TEXTURE, context.getAssets(),
                context.getCacheDir().getAbsolutePath());
        if (!ok) {
            throw new RuntimeException("Can't initialise trackers");
        }

        createVariables(mTrackerCount);
    }

    public UlsMultiTracker(final String path, int trackerCount, UlsTrackerInterfaceType apiType) {
        mAPI = apiType;
        if (mAPI == UlsTrackerInterfaceType.NV21_BYTEARRAY) mInitialised = true;

        mTrackerCount = Math.max(trackerCount, 2);
        if (mTrackerCount != trackerCount) {
            Log.i(TAG, "Max number of trackers is " + mTrackerCount);
        }

        mShapePointCount = new int[mTrackerCount];
        mAlive = new boolean[mTrackerCount];
        Arrays.fill(mShapePointCount, 0);
        Arrays.fill(mAlive, false);

        boolean ok = naMultiInitialiseFromPath(mTrackerCount,
                mAPI == UlsTrackerInterfaceType.OPENGL_TEXTURE, path);
        if (!ok) {
            throw new RuntimeException("Can't initialise trackers from path " + path);
        }

        createVariables(mTrackerCount);
    }

    public UlsMultiTracker(final Context context, int trackerCount) {
        this(context, trackerCount, UlsTrackerInterfaceType.OPENGL_TEXTURE);
    }
    public UlsMultiTracker(final String path, int trackerCount) {
        this(path, trackerCount, UlsTrackerInterfaceType.OPENGL_TEXTURE);
    }

    private void createVariables(int count) {
        mShape = new float[count][];
        mShape3D = new float[count][];
        mConfidence = new float[count][];
        mPose = new float[count][];
        mEulerAngles = new float[count][];
        mPupils = new float[count][];
        mGaze = new float[count][];
        mPoseQuality = new float[count];

        for (int i = 0; i < count; i++) {
            mShape[i] = new float[2 * mShapePointCount[i]];
            mShape3D[i] = new float[3 * mShapePointCount[i]];
            mConfidence[i] = new float[mShapePointCount[i]];
            mPose[i] = new float[6];
            mPose[i][0] = -1000;
            mEulerAngles[i] = new float[3];
            //mPoseQuality = 0;
            mPupils[i] = new float[4];
            mPupils[i][0] = -1000;
            mGaze[i] = new float[6];
            mGaze[i][0] = -1000;
        }
    }

    /**
     * Initialise the trackers with a previously created OpenGL texture
     *
     * <p> The current EGLContext will be used to initialise some OpenGL objects inside the tracker.
     * This EGLContext must be the current context when the {@link #update update} and
     * {@link #addFaces(RectF[], int[]) resetWithFaceRectangles} methods are called.
     *
     * @param srcOpenGLTexture A valid OpenGL texture name in the current EGLContext
     * @param inputImageWidth Width of the input texture
     * @param inputImageHeight Height of the input texture
     * @return true if the initialisation succeeded.
     */
    public boolean initialise(int srcOpenGLTexture, int inputImageWidth, int inputImageHeight) {
        if (mAPI != UlsTrackerInterfaceType.OPENGL_TEXTURE) {
            throw new RuntimeException("Wrong initialisation function for the chosen API.");
        }
        mEGL = (EGL10) EGLContext.getEGL();
        EGLContext context = mEGL.eglGetCurrentContext();
        if (context == EGL10.EGL_NO_CONTEXT || srcOpenGLTexture < 0) {
            throw new RuntimeException("No current EGL context, or texture < 0");
        }
        mSrcTextureName = srcOpenGLTexture;
        mInitialised = naMultiSetupOpenGL(mSrcTextureName, inputImageWidth, inputImageHeight);

        return mInitialised;
    }

    /**
     * Initialise the tracker when selected input is NV21 byte array
     * @return true on success
     */
    public boolean initialise() {
        if (mAPI != UlsTrackerInterfaceType.NV21_BYTEARRAY) {
            throw new RuntimeException("Wrong initialisation function for the chosen API.");
        }
        mInitialised = naMultiSetupByteArray();
        return mInitialised;
    }

    /**
     * Releases the native objects
     */
    public void dispose() {
        naMultiDispose();
    }

    public void finalize() throws Throwable {
        super.finalize();

        if (nativeTrackerPtr != 0) {
            Log.i(TAG, "You can also release the native object by calling dispose().");
            naMultiDispose();
        }
    }

    /**
     * Activates the tracker with a key
     * @param key A string containing a valid key
     * @return true if the key is valid, false otherwise.
     */
    public boolean activate(String key) {
        mInitialised = false;
        return naMultiActivate(key);
    }

    /**
     * Signal the tracker that the Activity is being paused, and the EGLContext is expected
     * to become invalid.
     *
     * <p>A call to {@link #initialise(int, int, int)} must be
     * made before using the tracker again</p>
     */
    public void onPause() {
        if (mAPI == UlsTrackerInterfaceType.OPENGL_TEXTURE) {
            mInitialised = false;
            naMultiEGLContextInvalidated();
        }
        Arrays.fill(mAlive, false);
    }


    public int getSrcTextureName() {
        return mSrcTextureName;
    }

    public int getNumberOfPoints(int index) {
        if (index >= mTrackerCount) throw new RuntimeException("Invalid tracker index");
        return mShapePointCount[index];
    }

    public float[] getShape(int index) {
        if (index >= mTrackerCount) throw new RuntimeException("Invalid tracker index");
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return null;
        if (mAlive[index]) return mShape[index];
        else return null;
    }

    public float[] getShape3D(int index) {
        if (index >= mTrackerCount) throw new RuntimeException("Invalid tracker index");
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return null;
        if (mAlive[index]) return mShape3D[index];
        else return null;
    }
    public float[] getConfidence(int index) {
        if (index >= mTrackerCount) throw new RuntimeException("Invalid tracker index");
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return null;
        if (mAlive[index]) return mConfidence[index];
        else return null;
    }
    /**
     * Rotation angles
     * @return A float[3] array with {pitch, yaw, roll} angles, or null if not available
     */
    public float[] getRotationAngles(int index) {
        if (index >= mTrackerCount) throw new RuntimeException("Invalid tracker index");
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return null;
        if (mAlive[index] && mPoseEnabled)
            return Arrays.copyOf(mEulerAngles[index], mEulerAngles[index].length);
        return null;
    }

    /**
     * X- and Y- translation in the image
     * @return a float[2] vector, or null if not available
     */
    public float[] getTranslationInImage(int index) {
        if (index >= mTrackerCount) throw new RuntimeException("Invalid tracker index");
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return null;
        if (mAlive[index] && mPoseEnabled) return Arrays.copyOfRange(mPose[index], 3, 5);
        return null;
    }
    /**
     * Weak-perspective camera model's scale parameter
     * @return scale in image, or -1.0f if the pose is not available.
     */
    public float getScaleInImage(int index) {
        if (index >= mTrackerCount) throw new RuntimeException("Invalid tracker index");
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return 0.0f;
        if (mAlive[index] && mPoseEnabled) return mPose[index][5];
        return 0.0f;
    }

    public float getPoseQuality(int index) {
        if (index >= mTrackerCount) throw new RuntimeException("Invalid tracker index");
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return 0.0f;
        if (mAlive[index] && mPoseEnabled) return mPoseQuality[index];
        return 0.0f;
    }
    public float[] getPupils(int index) {
        if (index >= mTrackerCount) throw new RuntimeException("Invalid tracker index");
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return null;
        if (mAlive[index] && mPredictPupils) {
            if (mPupils[index][0] <= -100) return null;
            return mPupils[index];
        }
        return null;
    }

    /**
     * Gaze values
     * @return A float[6] array (x,y,z)(x,y,z), or null.
     */
    public float[] getGaze(int index) {
        if (index >= mTrackerCount) throw new RuntimeException("Invalid tracker index");
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return null;
        if (mAlive[index] && mPredictPupils) {
            if (mGaze[index][0] <= -100) return null;
            return mGaze[index];
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
     * Resets the tracker(s), invalidating the current status
     * @param index Index of the tracker to reset. Use -1 to reset all trackers
     */
    public void resetTracker(int index) {
        if (index < 0) {
            for (int i = 0; i < index; i++) {
                if (mAlive[i]) {
                    if (naMultiResetTracker(i)) Log.d(TAG, "Tracker "+i+" reset");
                    else Log.w(TAG, "Failed to reset tracker " + i);
                    mAlive[i] = false;
                }
            }
        } else if (index < mTrackerCount) {
            naMultiResetTracker(index);
            mAlive[index] = false;
        } else {
            Log.e(TAG, "Error, bad tracker index " + index + " (max: "+ mTrackerCount + ")");
        }
    }

    /**
     * Adds a number of detected faces that may be used to reset the trackers. Resetting only takes
     * pace if the trackers are not tracking, call {@link #resetTracker(int)} to reset them
     *
     * @param rectangles An array of RectF with the locations of the faces
     * @param rotations An array of rotations, of the same length as the rectangles
     * @return False if there's an error
     */
    public boolean addFaces(RectF[] rectangles, int[] rotations) {
        if (!mInitialised) {
//            throw new RuntimeException("Tracker has not been initialised");
            return false;
        }
        if (rectangles.length == 0) return false;
        if (rectangles.length != rotations.length)
            throw new RuntimeException("Face rectangle and rotation arrays should be " +
                "of the same length!");
        //put everything in an array of rectangles with the rotation as an extra parameter
        int[] rect = new int[5 * rectangles.length];
        for (int i = 0; i < rectangles.length; i++) {
            rect[i * 5] = (int)rectangles[i].left;
            rect[i * 5 + 1] = (int)rectangles[i].top;
            rect[i * 5 + 2] = (int)rectangles[i].width();
            rect[i * 5 + 3] = (int)rectangles[i].height();
            rect[i * 5 + 4] = rotations[i];
        }
        naMultiAddFaces(rect);
        return true;
    }

    /**
     * Add faces to the tracker by searching the image using OpenCV's face detector
     *
     * <p>This method must be called from the thread where the object was initialised.
     * @param rotation Rotation of the camera. Either 0, 90, 180 or 270.
     * @return True if at least a face is found, false otherwise
     */
    public boolean findFacesAndAdd(int rotation) {
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return false;
        if (mAPI != UlsTrackerInterfaceType.OPENGL_TEXTURE) throw  new RuntimeException("Wrong " +
                "findFacesAndAdd function, byte[] interface was selected in constructor");
        boolean wasReset = naMultiFindFacesAndAdd(rotation);
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "After face detection, glerror: 0x" + Integer.toHexString(error));
        }
        return wasReset;
    }

    /**
     * Add faces to the tracker by searching the image using OpenCV's face detector
     *
     * @param nv21 Image data in nv21 format
     * @param width Width of the image
     * @param height Height of the image
     * @param rotation Rotation of the camera. Either 0, 90, 180 or 270.
     * @return True if at least a face is found, false otherwise
     */
    public boolean findFacesAndAdd(byte[] nv21, int width, int height, int rotation, ImageDataType imageDataType) {
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return false;
        if (mAPI != UlsTrackerInterfaceType.NV21_BYTEARRAY) {
            throw new RuntimeException("Wrong findFacesAndAdd function, OpenGL interface was " +
                    "selected in constructor");
        }
        return naMultiFindFacesAndAddByte(nv21, width, height, rotation, imageDataType.getValue());
    }

    /**
     * Update the shape location using the OpenGL texture set in {@link #initialise(int, int, int)}
     *
     * <p>This method must be called from the thread where the object was initialised.
     * @return number of trackers currently in use
     */
    public int update() {
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return 0;
        if (mAPI != UlsTrackerInterfaceType.OPENGL_TEXTURE) throw  new RuntimeException("Wrong " +
                "update function, byte[] interface was selected in constructor");
        //this call updates the contents of the mAlive array!
        int alive = naMultiUpdateShapes(mPredictPupils, mHighPrecision, mSticky);
        if (alive < 0) {
            Log.e(TAG, "Error in update shapes");
        }
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "After feature setup, glerror: 0x" + Integer.toHexString(error));
        }

        return alive;
    }

    /**
     * Update the shape location with the NV21 byte array data
     * @param nv21 Image data in nv21 format
     * @param width Width of the image
     * @param height Height of the image
     * @return number of trackers currently in use
     */
    public int update(byte[] nv21, int width, int height, ImageDataType imageDataType) {
        if (!mInitialised)
//            throw new RuntimeException("Tracker has not been initialised");
            return 0;
        if (mAPI != UlsTrackerInterfaceType.NV21_BYTEARRAY) {
            throw new RuntimeException("Wrong update function, OpenGL interface was selected in " +
                    "constructor");
        }
        int alive = naMultiUpdateShapesByte(nv21, width, height, mPredictPupils, mHighPrecision,
                mSticky, imageDataType.getValue());
        if (alive < 0) Log.e(TAG, "Error in update() shapes");
        return alive;
    }

    private float[][] mShape, mShape3D, mConfidence;
    private float[][] mPose, mEulerAngles; //mPose is Rodrigues + translation
    private float[][] mPupils, mGaze;
    private float[] mPoseQuality;
    private int[] mShapePointCount;
    private long nativeTrackerPtr = 0;

    private native boolean naMultiInitialiseFromAssets(int count, boolean useOGL,
                                                       AssetManager manager,
                                                       String cacheDir);
    private native boolean naMultiInitialiseFromPath(int count, boolean useOGL, String path);

    private native void naMultiDispose();
    private native boolean naMultiActivate(String key);

    private native boolean naMultiSetupByteArray();
    private native boolean naMultiSetupOpenGL(int srcTextureName, int width, int height);
    private native void naMultiEGLContextInvalidated();

    private native boolean naMultiFindFacesAndAdd(int rotation);
    private native boolean naMultiFindFacesAndAddByte(byte[] nv21, int width, int height,
                                                      int rotation, int format);
    private native boolean naMultiAddFaces(int[] faces);

    private native int naMultiUpdateShapes(boolean predictPupils, boolean highPrecision,
                                           boolean smooth);
    private native int naMultiUpdateShapesByte(byte[] nv21, int width, int height,
                                               boolean predictPupils,
                                               boolean highPrecision,
                                               boolean smooth, int format);
    private native boolean naMultiResetTracker(int index);
    static {
        System.loadLibrary("ulsTracker_native");
    }
}
