package com.cv.faceapi;


import com.cv.faceapi.CvFaceApiBridge.*;
import com.cv.faceapi.CvFace21;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * CvFaceSDK API: CvFaceDetector
 * Created by Guangli W on 9/7/15.
 */
public class CvFaceDetector {
    private final Pointer detectorHandle;

    /**
     * Initialize the Native Handler with model and memory
     */
    public CvFaceDetector() {
        detectorHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_create_detector(null,
        		CvFaceApiBridge.CV_DETECT_SKIP_BELOW_THRESHOLD | CvFaceApiBridge.CV_DETECT_ENABLE_ALIGN);
    }

    @Override
    protected void finalize() throws Throwable {
        CvFaceApiBridge.FACESDK_INSTANCE.cv_face_destroy_detector(detectorHandle);
    }
	
    /**
     * Given the Image by Bitmap to detect face
     * @param image Input image
     * @return CvFace array, each one in array is Detected by SDK native API
     */
    public CvFace21[] detect(Bitmap image) {
        int[] colorData = CvUtils.getBGRAImageByte(image);
        return detect(colorData, CVImageFormat.CV_PIX_FMT_BGRA8888, image.getWidth(), image.getHeight(), image.getWidth());
    }

    /**
     * Given the Image by Byte Array to detect face
     * @param image Input image
     * @return CvFace array, each one in array is Detected by SDK native API
     */
    public CvFace21[] detect(int[] colorImage, int cvImageFormat, int imageWidth, int imageHeight, int imageStride) {
        PointerByReference ptrToArray = new PointerByReference();
        IntByReference ptrToSize = new IntByReference();
        int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_detect(detectorHandle, colorImage, cvImageFormat, imageWidth,
                imageHeight, imageStride, 0, ptrToArray, ptrToSize);

        if (rst != ResultCode.CV_OK.getResultCode()) {
            throw new RuntimeException("Calling cv_face_detect() method failed! ResultCode=" + rst);
        }

        if (ptrToSize.getValue() == 0) {
            return new CvFace21[0];
        }

        cv_face_t arrayRef = new cv_face_t(ptrToArray.getValue());
        arrayRef.read();
        cv_face_t[] array = cv_face_t.arrayCopy((cv_face_t[]) arrayRef.toArray(ptrToSize.getValue()));
        CvFaceApiBridge.FACESDK_INSTANCE.cv_face_release_detector_result(ptrToArray.getValue(), ptrToSize.getValue());
        
        CvFace21[] ret = new CvFace21[array.length]; 
        for (int i = 0; i < array.length; i++) {
        	ret[i] = new CvFace21(array[i]);
        }

        return ret;
    }
}
