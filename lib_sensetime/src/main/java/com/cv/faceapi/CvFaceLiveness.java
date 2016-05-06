package com.cv.faceapi;

import android.graphics.Bitmap;
import android.util.Log;

import com.cv.faceapi.CvFaceApiBridge.ResultCode;
import com.cv.faceapi.CvFaceApiBridge.cv_face_t;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class CvFaceLiveness {
	private Pointer mTrackHandle;
	private Pointer mLivenessHandle;
	private int mLastFaceID = -1;
	private IntByReference mLivenessState;
	private cv_face_t[] mFaceArray;
	private int mCurrentFaceID = -1;
	private boolean mIsFirst = true;
	
	public class Config{
		//public static final int CV_LIVENESS_DEFAULT		= 0x01000000;  ///<  关闭自动RESET选项
		public static final int CV_LIVENESS_NORESET		= 0x01000000;  ///<  关闭自动RESET选项
		public static final int CV_LIVENESS_INPUT_BY_FRAME	= 0x02000000;  ///<  针对视频连续帧输入选项，如不使用则为针对摄像头实时获取帧输入
		public static final int CV_LIVENESS_ENABLE_BLINK	= 0x10000000;  ///<  开启眨眼检测
		public static final int CV_LIVENESS_ENABLE_MOUTH	= 0x20000000;  ///<  开启嘴部动作检测
		public static final int CV_LIVENESS_ENABLE_HEADYAW	= 0x40000000;  ///<  开启头部摇头动作检测
		public static final int CV_LIVENESS_ENABLE_HEADNOD	= 0x80000000;  ///<  开启头部点头动作检测
		public static final int CV_LIVENESS_ENABLE_ALL_DETECTOR	= (CV_LIVENESS_ENABLE_BLINK | CV_LIVENESS_ENABLE_MOUTH | CV_LIVENESS_ENABLE_HEADYAW | CV_LIVENESS_ENABLE_HEADNOD);  ///<  开启所有检测功能
		public static final int CV_LIVENESS_DEFAULT = 	CV_LIVENESS_ENABLE_ALL_DETECTOR;

	}
	
	/**
	 * Initialize the Native Handler with model and memory
	 */
	public CvFaceLiveness() {
		init(null, CvFaceTrack.Config.CV_FACE_SKIP_BELOW_THRESHOLD, null, Config.CV_LIVENESS_ENABLE_ALL_DETECTOR);
	}
	
	public CvFaceLiveness(String trackPath ,String livePath) {
		init(trackPath, CvFaceTrack.Config.CV_FACE_SKIP_BELOW_THRESHOLD, livePath, Config.CV_LIVENESS_ENABLE_ALL_DETECTOR);
	}

	public CvFaceLiveness(String trackPath ,int trackConfig,String livePath,int liveConfig) {
		init(trackPath, trackConfig, livePath, liveConfig);
	}
	
	private void init(String trackPath ,int trackConfig,String livePath,int liveConfig){
		mTrackHandle = CvFaceApiBridge.FACESDK_INSTANCE
				.cv_face_create_tracker(trackPath , trackConfig);
		mLivenessHandle = CvFaceApiBridge.FACESDK_INSTANCE
				.cv_face_create_liveness_detector(livePath, liveConfig);
	}
	
	public void reset(){
		CvFaceApiBridge.FACESDK_INSTANCE.cv_face_liveness_detector_reset(mLivenessHandle);
	}

	@Override
	protected void finalize() throws Throwable {
		long start_destroy = System.currentTimeMillis();
		CvFaceApiBridge.FACESDK_INSTANCE
				.cv_face_destroy_tracker(mTrackHandle);
		CvFaceApiBridge.FACESDK_INSTANCE
				.cv_face_destroy_liveness_detector(mLivenessHandle);
		long end_destroy = System.currentTimeMillis();
		Log.i("liveness", "destroy cost "+ (end_destroy - start_destroy));
	}

	/**
	 * Given the Image by Bitmap to track face
	 * 
	 * @param image
	 *            Input image
	 * @return int liveness_state
	 */
	public CvLivenessResult liveness(Bitmap image, int orientation) {
		System.out
				.println("SampleLiveness-------->CvFaceLiveness--------->liveness1");
		int[] colorImage = CvUtils.getBGRAImageByte(image);
		return liveness(colorImage,CVImageFormat.CV_PIX_FMT_BGRA8888, image.getWidth(), image.getHeight(),
				image.getWidth(), orientation);
	}
	
	public CvLivenessResult liveness(byte[] colorImage,int cvImageFormat, int imageWidth, int imageHeight,
			int imageStride, int orientation){
		int i = 0;
		CvLivenessResult livenessResult = new CvLivenessResult();
		System.out
				.println("SampleLiveness-------->CvFaceLiveness--------->liveness2");
		PointerByReference ptrToArray = new PointerByReference();
		IntByReference ptrToSize = new IntByReference();
		int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_track(
				mTrackHandle, colorImage,cvImageFormat, imageWidth, imageHeight, imageStride,
				orientation, ptrToArray, ptrToSize);
		if (rst != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_face_track() method failed! ResultCode="
							+ rst);
		}
		if (ptrToSize.getValue() == 0) {
			livenessResult.mStatus = CvLivenessResult.CVLIVENESSRESULT_STATUS_NULL;
			return livenessResult;
		}
		cv_face_t arrayRef = new cv_face_t(ptrToArray.getValue());
		arrayRef.read();
		mFaceArray = cv_face_t.arrayCopy((cv_face_t[]) arrayRef.toArray(ptrToSize
				.getValue()));

		if (mIsFirst && mFaceArray.length > 0) {
			mLastFaceID = mFaceArray[0].ID;
			mCurrentFaceID = 0;
		} else if (mIsFirst == false && mFaceArray.length > 0) {
			for (i = 0; i < mFaceArray.length; i++) {
				if (mLastFaceID == mFaceArray[i].ID) {
					mCurrentFaceID = i;
					break;
				}
			}
			if(i == mFaceArray.length){
				mCurrentFaceID = 0;
			}
		}
		mIsFirst = false;
		FloatByReference liveness_score = new FloatByReference();
		mLivenessState = new IntByReference();
		long startTime = System.currentTimeMillis();
		rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_liveness_detect(
				mLivenessHandle, colorImage,cvImageFormat, imageWidth, imageHeight,
				imageStride, mFaceArray[mCurrentFaceID], liveness_score, mLivenessState);
		long endTime = System.currentTimeMillis();
		Log.i("Test", "liveness detect time: "+(endTime-startTime)+"ms");
		if (rst != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_face_liveness_detect() method failed! ResultCode="
							+ rst);
		}
					
		CvFaceApiBridge.FACESDK_INSTANCE.cv_face_release_tracker_result(
				ptrToArray.getValue(), ptrToSize.getValue());
		livenessResult.mStatus = mLivenessState == null ? CvLivenessResult.CVLIVENESSRESULT_STATUS_NULL : mLivenessState.getValue();
		livenessResult.mScore = liveness_score.getValue();
		livenessResult.mFaceRect = new CvFace21(mFaceArray[mCurrentFaceID]);
		return livenessResult;
	}

	/**
	 * Given the Image by Byte Array to track and liveness face
	 * 
	 * @param image  Input image
	 * 
	 * @return int liveness_state, 
	 */
	public CvLivenessResult liveness(int[] colorImage,int cvImageFormat, int imageWidth, int imageHeight,
			int imageStride, int orientation) {
		int i = 0;
		CvLivenessResult livenessResult = new CvLivenessResult();
		System.out
				.println("SampleLiveness-------->CvFaceLiveness--------->liveness2");
		PointerByReference ptrToArray = new PointerByReference();
		IntByReference ptrToSize = new IntByReference();
		int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_track(
				mTrackHandle, colorImage,cvImageFormat, imageWidth, imageHeight, imageStride,
				orientation, ptrToArray, ptrToSize);
		if (rst != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_face_track() method failed! ResultCode="
							+ rst);
		}
		if (ptrToSize.getValue() == 0) {
			livenessResult.mStatus = CvLivenessResult.CVLIVENESSRESULT_STATUS_NULL;
			return livenessResult;
		}
		cv_face_t arrayRef = new cv_face_t(ptrToArray.getValue());
		arrayRef.read();
		mFaceArray = cv_face_t.arrayCopy((cv_face_t[]) arrayRef.toArray(ptrToSize
				.getValue()));

		if (mIsFirst && mFaceArray.length > 0) {
			mLastFaceID = mFaceArray[0].ID;
			mCurrentFaceID = 0;
		} else if (mIsFirst == false && mFaceArray.length > 0) {

			for (i = 0; i < mFaceArray.length; i++) {
				if (mLastFaceID == mFaceArray[i].ID) {
					mCurrentFaceID = i;
					break;
				}
			}

			if (i == mFaceArray.length)
			{
				CvFaceApiBridge.FACESDK_INSTANCE
						.cv_face_liveness_detector_reset(mLivenessHandle);
				livenessResult.mStatus = CvLivenessResult.CVLIVENESSRESULT_STATUS_RESET;
				return livenessResult;
			}

		}
		mIsFirst = false;
		FloatByReference liveness_score = new FloatByReference();
		mLivenessState = new IntByReference();
		long startTime = System.currentTimeMillis();
		rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_liveness_detect(
				mLivenessHandle, colorImage,cvImageFormat, imageWidth, imageHeight,
				imageStride, mFaceArray[mCurrentFaceID], liveness_score, mLivenessState);
		long endTime = System.currentTimeMillis();
		Log.i("Test", "liveness detect time: "+(endTime-startTime)+"ms");
		if (rst != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_face_liveness_detect() method failed! ResultCode="
							+ rst);
		}
					
		CvFaceApiBridge.FACESDK_INSTANCE.cv_face_release_tracker_result(
				ptrToArray.getValue(), ptrToSize.getValue());
		livenessResult.mStatus = mLivenessState == null ? CvLivenessResult.CVLIVENESSRESULT_STATUS_NULL : mLivenessState.getValue();
		livenessResult.mScore = liveness_score.getValue();	
		return livenessResult;
	}
}
