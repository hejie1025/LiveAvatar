package com.cv.faceapi;

import android.graphics.Bitmap;
import android.graphics.PointF;

import com.cv.faceapi.CvFaceApiBridge.ResultCode;
import com.cv.faceapi.CvFaceApiBridge.cv_face_t;
import com.cv.faceapi.CvFaceApiBridge.cv_pointf_t;
import com.cv.faceapi.CvFaceApiBridge.cv_pointi_t;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class CvFaceAlignment {
	private final Pointer alignmentHandle;
    private static final int FACE_KEY_POINTS_COUNT = 106;

    /**
     * Initialize the Native Handler with model and memory
     */
    public CvFaceAlignment() {
    	alignmentHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_create_alignment(null, FACE_KEY_POINTS_COUNT);
    }

    @Override
    protected void finalize() throws Throwable {
        CvFaceApiBridge.FACESDK_INSTANCE.cv_face_destroy_alignment(alignmentHandle);
    }
	
    /**
     * Given the Image by Bitmap to detect face
     * @param image Input image
     * @return CvFace array, each one in array is Detected by SDK native API
     */
    public PointF[] align(Bitmap image, CvFace21 face) {
        int[] colorImage = CvUtils.getBGRAImageByte(image);
        return align(colorImage, CVImageFormat.CV_PIX_FMT_BGRA8888, image.getWidth(), image.getHeight(), image.getWidth()*4, face);
    }

    /**
     * Given the Image by Byte Array to detect face
     * @param image Input image
     * @return CvFace array, each one in array is Detected by SDK native API
     */
    public PointF[] align(int[] colorImage, int cvImageFormat, int imageWidth, int imageHeight, int imageStride, CvFace21 face) {
        PointerByReference ptrToArray = new PointerByReference();
        cv_pointf_t [] facial_points_array = new cv_pointf_t[FACE_KEY_POINTS_COUNT];
        cv_face_t [] face_ts = new cv_face_t[1];
        face_ts[0] = face;
        int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_align(alignmentHandle, colorImage, cvImageFormat, imageWidth,
                imageHeight, imageStride, face_ts, facial_points_array);

        if (rst != ResultCode.CV_OK.getResultCode()) {
            throw new RuntimeException("Calling cv_face_align() method failed! ResultCode=" + rst);
        }

        PointF[] ret = new PointF[FACE_KEY_POINTS_COUNT]; 
        for (int i = 0; i < FACE_KEY_POINTS_COUNT; i++) {
        	ret[i] = new PointF();
        	ret[i].x = facial_points_array[i].x;
        	ret[i].y = facial_points_array[i].y;
        }
        return ret;
    }
}
