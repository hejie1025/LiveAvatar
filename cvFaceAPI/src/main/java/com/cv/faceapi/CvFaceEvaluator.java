package com.cv.faceapi;

import android.graphics.Bitmap;
import com.cv.faceapi.CvFaceApiBridge.ResultCode;
import com.cv.faceapi.CvFaceApiBridge.cv_face_t;
import com.cv.faceapi.CvFaceApiBridge.cv_quality_result_t;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class CvFaceEvaluator {
	
	private Pointer mEvaluatorHandle;
	private Pointer mTrackerHandle;
	private cv_face_t[] mFaceArray;
	private boolean mIsFirst;
	private int mLastFaceID;
	private int mCurrentFaceIdx = -1;
	private int mAttributeType;
	
	public CvFaceEvaluator(){
		init(null ,null);
	}
	
	public CvFaceEvaluator(String evaluatorModelPath){
		init(evaluatorModelPath, null);
	}
	
	private void init(String evaluatorModelPath,String trackPath){
		mEvaluatorHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_create_evaluator(evaluatorModelPath);
		mTrackerHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_create_tracker(trackPath,CvFaceTrack.Config.CV_FACE_SKIP_BELOW_THRESHOLD);
	}

	@Override
	protected void finalize() throws Throwable {
		CvFaceApiBridge.FACESDK_INSTANCE.cv_face_destroy_evaluator(mEvaluatorHandle);
		CvFaceApiBridge.FACESDK_INSTANCE.cv_face_destroy_tracker(mTrackerHandle);
	}
	
	/**
	 * Given the Image by Bitmap to attribute face
	 * 
	 * @param image : Input image
	 * @return CvAttributeResult attribute result
	 */
	public float evaluator(Bitmap image) {
		return evaluator(image, 0);
	}
	
	
	/**
	 * Given the Image by Bitmap to evaluator face
	 * 
	 * @param image : Input image
	 * @return evaluator result
	 */
	public float evaluator(Bitmap image,int rotation) {
		if(image == null || image.isRecycled()){
			return 0;
		}
		Bitmap bitmap = image;
		if(rotation != 0){
			bitmap = CvUtils.getRotateBitmap(image, rotation);
		}
		int[] colorImage = CvUtils.getBGRAImageByte(bitmap);
		return evaluator(colorImage,CVImageFormat.CV_PIX_FMT_BGRA8888, bitmap.getWidth(), bitmap.getHeight(),
				bitmap.getWidth());
	}
	
	/**
	 * Given the Image by Byte Array to track and attribute face
	 * 
	 * @param image  Input image
	 * 
	 * @return CvAttributeResult 
	 */
	public float evaluator(int[] colorImage,int cvImageFormat, int imageWidth, int imageHeight,
			int imageStride) {
		int i = 0;
		PointerByReference ptrToArray = new PointerByReference();
		IntByReference ptrToSize = new IntByReference();
		int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_track(
				mTrackerHandle, colorImage,cvImageFormat, imageWidth, imageHeight, imageStride,
				0, ptrToArray, ptrToSize);
		if (rst != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_face_track() method failed! ResultCode="
							+ rst);
		}
		if (ptrToSize.getValue() == 0) {
			return 0;
		}
		cv_face_t arrayRef = new cv_face_t(ptrToArray.getValue());
		arrayRef.read();
		mFaceArray = cv_face_t.arrayCopy((cv_face_t[]) arrayRef.toArray(ptrToSize
				.getValue()));
		if(mFaceArray.length < 0){
			return 0;
		}
		
		if (mIsFirst) {
			mLastFaceID = mFaceArray[0].ID;
			mCurrentFaceIdx = 0;

		} else {
			for (i = 0; i < mFaceArray.length; i++) {
				if (mLastFaceID == mFaceArray[i].ID) {
					mCurrentFaceIdx = i;
					break;
				}
			}
			if(i == mFaceArray.length){
				mCurrentFaceIdx = 0;
			}
		}
		mIsFirst = false;
		cv_quality_result_t result = new cv_quality_result_t();
		rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_evaluate(mEvaluatorHandle, colorImage, cvImageFormat, imageWidth, imageHeight, imageStride, mFaceArray[mCurrentFaceIdx], result);
		if (rst != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_face_evaluate() method failed! ResultCode="
							+ rst);
		}
		return result.score;
	}	
}
