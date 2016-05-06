package com.cv.faceapi;

import android.graphics.Bitmap;
import android.util.Log;

import com.cv.faceapi.CvFaceApiBridge.ResultCode;
import com.cv.faceapi.CvFaceApiBridge.cv_face_t;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class CvFaceMultiTrack21 {
	private final Pointer trackHandle;
    private static final int FACE_KEY_POINTS_COUNT = 106;
    private static final int CV_FACE_SKIP_BELOW_THRESHOLD = 1;
    private static final int CV_TRACK_MULTI_TRACKING = 0x10000;//多人脸跟踪选项，开启跟踪所有检测到的人脸，关闭只跟踪检测到的人脸中最大的一张脸(check cv_face.h)
    private static final int CV_FACE_ENABLE_ALIGN = 8;
    static boolean DEBUG = false;
    
    private static final int CV_FACE_RESIZE_IMG_320W = 0x00000002;  ///< resize图像为长边320的图像
    
    PointerByReference ptrToArray = new PointerByReference();
    IntByReference ptrToSize = new IntByReference();
    
    /**
     * Note
        track only one face： 
        frist:trackHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_create_tracker(null, CV_FACE_SKIP_BELOW_THRESHOLD);
        second: setMaxDetectableFaces(1)参数设为1
     *  track多张人脸：	
     *  first：开启多张人脸检测(CV_TRACK_MULTI_TRACKING)
     *  trackHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_create_tracker(null, CV_FACE_SKIP_BELOW_THRESHOLD | CV_TRACK_MULTI_TRACKING);
        second:setMaxDetectableFaces(-1)参数设为-1
     */
    public CvFaceMultiTrack21() {
    	trackHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_create_tracker(null, CV_FACE_SKIP_BELOW_THRESHOLD | CV_TRACK_MULTI_TRACKING | CV_FACE_RESIZE_IMG_320W);
    }
    public int setMaxDetectableFaces(int max)
    {
    	int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_track_set_detect_face_cnt_limit(trackHandle, max);
        return rst;
    }
    @Override
    protected void finalize() throws Throwable {
    	long start_destroy = System.currentTimeMillis();
        CvFaceApiBridge.FACESDK_INSTANCE.cv_face_destroy_tracker(trackHandle);
        long end_destroy = System.currentTimeMillis();
        Log.i("track21", "destroy " + (end_destroy - start_destroy)+" ms");
    }
	
    /**
     * Given the Image by Bitmap to track face
     * @param image Input image by Bitmap
     * @param orientation Image orientation
     * @return CvFace array, each one in array is Detected by SDK native API
     */
    public CvFace21[] track(Bitmap image, int orientation) {
    	if(DEBUG)System.out.println("SampleLiveness-------->CvFaceMultiTrack--------->track1");
    	
        int[] colorImage = CvUtils.getBGRAImageByte(image);
        return track(colorImage, CVImageFormat.CV_PIX_FMT_BGRA8888,image.getWidth(), image.getHeight(), image.getWidth(), orientation);
    }

    /**
     * Given the Image by Byte Array to track face
     * @param colorImage Input image by int
     * @param cvImageFormat Image format
     * @param imageWidth Image width
     * @param imageHeight Image height
     * @param imageStride Image stride
     * @param orientation Image orientation
     * @return CvFace array, each one in array is Detected by SDK native API
     */
    public CvFace21[] track(int[] colorImage,int cvImageFormat, int imageWidth, int imageHeight, int imageStride, int orientation) {
    	if(DEBUG)System.out.println("SampleLiveness-------->CvFaceMultiTrack--------->track2");
    	
        long startTime = System.currentTimeMillis();
        int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_track(trackHandle, colorImage, cvImageFormat,imageWidth,
                imageHeight, imageStride, orientation, ptrToArray, ptrToSize);
        long endTime = System.currentTimeMillis();
        
        if(DEBUG)Log.d("Test", "multi track time: "+(endTime-startTime)+"ms");
        
        if (rst != ResultCode.CV_OK.getResultCode()) {
            throw new RuntimeException("Calling cv_face_multi_track() method failed! ResultCode=" + rst);
        }

        if (ptrToSize.getValue() == 0) {
        	if(DEBUG)Log.d("Test", "ptrToSize.getValue() == 0");
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
        
        if(DEBUG)Log.d("Test", "track : "+ ret);
        
        return ret;
    }
    
    /**
     * Given the Image by Byte to track face
     * @param image Input image by byte
     * @param orientation Image orientation
     * @param width Image width
     * @param height Image height
     * @return CvFace array, each one in array is Detected by SDK native API
     */
    public CvFace21[] track(byte[] image, int orientation,int width,int height) {
    	if(DEBUG){
    		System.out.println("SampleLiveness-------->CvFaceMultiTrack--------->track3");
    	}
    	
        return track(image, CVImageFormat.CV_PIX_FMT_NV21,width, height, width, orientation);
    }

    /**
     * Given the Image by Byte Array to track face
     * @param colorImage Input image by byte
     * @param cvImageFormat Image format
     * @param imageWidth Image width
     * @param imageHeight Image height
     * @param imageStride Image stride
     * @param orientation Image orientation
     * @return CvFace array, each one in array is Detected by SDK native API
     */
    public CvFace21[] track(byte[] colorImage,int cvImageFormat, int imageWidth, int imageHeight, int imageStride, int orientation) {
    	if(DEBUG){
    		System.out.println("SampleLiveness-------->CvFaceMultiTrack--------->track4");
    	}
    	
        long startTime = System.currentTimeMillis();
        int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_track(trackHandle, colorImage, cvImageFormat,imageWidth,
                imageHeight, imageStride, orientation, ptrToArray, ptrToSize);
        long endTime = System.currentTimeMillis();
        
        if(DEBUG)Log.d("Test", "multi track time: "+(endTime-startTime)+"ms");
        
        if (rst != ResultCode.CV_OK.getResultCode()) {
            throw new RuntimeException("Calling cv_face_multi_track() method failed! ResultCode=" + rst);
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
        
        if(DEBUG)Log.d("Test", "track : "+ ret);
        
        return ret;
    }
}
