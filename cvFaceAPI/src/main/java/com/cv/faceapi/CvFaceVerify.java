package com.cv.faceapi;

import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Environment;
import android.util.Log;

import com.cv.faceapi.CvFaceApiBridge.ResultCode;
import com.cv.faceapi.CvFaceApiBridge.cv_face_t;
import com.cv.faceapi.CvFaceApiBridge.cv_feature_t;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * CvFaceSDK API: CvFaceDetector Created by Tracy Wang on 10/20/15.
 */
public class CvFaceVerify {
	private final Pointer verifyHandle;
	private static final String MODEL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/verify.model";

	public CvFaceVerify() {
		verifyHandle = CvFaceApiBridge.FACESDK_INSTANCE
				.cv_verify_create_handle(MODEL_PATH);
	}

	@Override
	protected void finalize() throws Throwable {
		long start_destroy = System.currentTimeMillis();
		CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_destroy_handle(verifyHandle);
		long end_destroy = System.currentTimeMillis();
		Log.i("verify", "destroy cost "+(end_destroy - start_destroy)+" ms");
	}

	public byte [] getFeature(Bitmap image, CvFace21 face) {
		int[] colorImage = CvUtils.getBGRAImageByte(image);
		return getFeature(colorImage, CVImageFormat.CV_PIX_FMT_BGRA8888,
				image.getWidth(), image.getHeight(), image.getWidth() * 4, face);
	}

	public byte [] getFeature(int[] colorImage, int cvImageFormat,
			int imageWidth, int imageHeight, int imageStride, CvFace21 face) {
		PointerByReference ptrToArray = new PointerByReference();
		IntByReference ptrToSize = new IntByReference();
		int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_get_feature(
				verifyHandle, colorImage, cvImageFormat, imageWidth,
				imageHeight, imageStride, face, ptrToArray, ptrToSize);
		if (rst != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_verify_get_feature() method failed! ResultCode=" + rst);
		}
		if (ptrToSize.getValue() == 0) {
			return null;
		}
		byte [] str = new byte[(ptrToSize.getValue()+2)/3*4 + 1];
		int resultCode = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_serialize_feature(ptrToArray.getValue(), str);
		CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_release_feature(ptrToArray.getValue());
		if (resultCode != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_verify_serialize_feature() method failed! ResultCode=" + resultCode);
		}
		return str;
	}

	public float compareFeature(byte [] feature1, byte [] feature2) {
		FloatByReference result = new FloatByReference();
		Pointer ptrFeature1 = null;
		Pointer ptrFeature2 = null;
		ptrFeature1 = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_deserialize_feature(feature1);
		ptrFeature2 = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_deserialize_feature(feature2);
		int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_compare_feature(
				verifyHandle, ptrFeature1, ptrFeature2, result);
		if (rst != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_verify_compare_feature() method failed! ResultCode=" + rst);
		}
		CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_release_feature(ptrFeature1);
		CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_release_feature(ptrFeature2);
		return result.getValue();
	}

}
