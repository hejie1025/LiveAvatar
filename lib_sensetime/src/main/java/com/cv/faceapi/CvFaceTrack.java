package com.cv.faceapi;

import android.graphics.Bitmap;
import android.util.Log;

import com.cv.faceapi.CvFaceApiBridge.ResultCode;
import com.cv.faceapi.CvFaceApiBridge.cv_face_t;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class CvFaceTrack {
	
	private Pointer trackHandle;
    
    public class Config{
    	public static final int FACE_KEY_POINTS_COUNT = 106;
        public static final int CV_FACE_SKIP_BELOW_THRESHOLD = 1;
        public static final int CV_FACE_ENABLE_ALIGN = 8;
    }
    /**
     * Initialize the Native Handler with model and memory
     */
    public CvFaceTrack() {
    	init(null, Config.CV_FACE_SKIP_BELOW_THRESHOLD);
    }
    
    public CvFaceTrack(String path, int config) {
    	init(path, config);
    }
    
    private void init(String path, int config){
    	trackHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_create_tracker(null, Config.CV_FACE_SKIP_BELOW_THRESHOLD);
    }

    @Override
    protected void finalize() throws Throwable {
        CvFaceApiBridge.FACESDK_INSTANCE.cv_face_destroy_tracker(trackHandle);
    }
	
    /**
     * Given the Image by Bitmap to track face
     * @param image Input image
     * @return CvFace array, each one in array is Detected by SDK native API
     */
    public CvFace21[] track(Bitmap image, int orientation) {
    	System.out.println("SampleLiveness-------->CvFaceTrack--------->track1");
        int[] colorImage = CvUtils.getBGRAImageByte(image);
        return track(colorImage, CVImageFormat.CV_PIX_FMT_BGRA8888,image.getWidth(), image.getHeight(), image.getWidth(), orientation);
    }

    /**
     * Given the Image by Byte Array to track face
     * @param image Input image
     * @return CvFace array, each one in array is Detected by SDK native API
     */
    public CvFace21[] track(int[] colorImage,int cvImageFormat, int imageWidth, int imageHeight, int imageStride, int orientation) {
    	System.out.println("SampleLiveness-------->CvFaceTrack--------->track2");
    	PointerByReference ptrToArray = new PointerByReference();
        IntByReference ptrToSize = new IntByReference();
        long startTime = System.currentTimeMillis();
        int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_track(trackHandle, colorImage, cvImageFormat,imageWidth,
                imageHeight, imageStride, orientation, ptrToArray, ptrToSize);
        long endTime = System.currentTimeMillis();
        Log.d("Test", "track time: "+(endTime-startTime)+"ms");
        if (rst != ResultCode.CV_OK.getResultCode()) {
            throw new RuntimeException("Calling cv_face_track() method failed! ResultCode=" + rst);
        }

        if (ptrToSize.getValue() == 0) {
        	Log.d("Test", "ptrToSize.getValue() == 0");
            return new CvFace21[0];
        }

        cv_face_t arrayRef = new cv_face_t(ptrToArray.getValue());
        arrayRef.read();
        cv_face_t[] array = cv_face_t.arrayCopy((cv_face_t[]) arrayRef.toArray(ptrToSize.getValue()));
        CvFaceApiBridge.FACESDK_INSTANCE.cv_face_release_tracker_result(ptrToArray.getValue(), ptrToSize.getValue());
        
        CvFace21[] ret = new CvFace21[array.length]; 
        for (int i = 0; i < array.length; i++) {
        	ret[i] = new CvFace21(array[i]);
        }
        Log.d("Test", "track : "+ ret);
        return ret;
    }
    
    /**
     * Given the Image by Byte Array to track face
     * @param image Input image
     * @return CvFace array, each one in array is Detected by SDK native API
     */
    public CvFace21[] track(byte[] colorImage,int cvImageFormat, int imageWidth, int imageHeight, int imageStride, int orientation) {
    	PointerByReference ptrToArray = new PointerByReference();
        IntByReference ptrToSize = new IntByReference();
        int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_track(trackHandle, colorImage, cvImageFormat,imageWidth,
                imageHeight, imageStride, orientation, ptrToArray, ptrToSize);
        if (rst != ResultCode.CV_OK.getResultCode()) {
            throw new RuntimeException("Calling cv_face_track() method failed! ResultCode=" + rst);
        }

        if (ptrToSize.getValue() == 0) {
            return new CvFace21[0];
        }

        cv_face_t arrayRef = new cv_face_t(ptrToArray.getValue());
        arrayRef.read();
        cv_face_t[] array = cv_face_t.arrayCopy((cv_face_t[]) arrayRef.toArray(ptrToSize.getValue()));
        CvFaceApiBridge.FACESDK_INSTANCE.cv_face_release_tracker_result(ptrToArray.getValue(), ptrToSize.getValue());
        
        CvFace21[] ret = new CvFace21[array.length]; 
        for (int i = 0; i < array.length; i++) {
        	ret[i] = new CvFace21(array[i]);
        }
        Log.d("Test", "track : "+ ret);
        return ret;
    }
}
