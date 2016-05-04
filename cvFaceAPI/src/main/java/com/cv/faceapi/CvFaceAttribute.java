package com.cv.faceapi;

import android.R.integer;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.cv.faceapi.CvFaceApiBridge.ResultCode;
import com.cv.faceapi.CvFaceApiBridge.cv_face_t;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class CvFaceAttribute {
	
	public class AttributeType{
		public static final int CV_ATTR_FEATURE = 0;
		public static final int CV_ATTR_EMOTION = 1;
	}
	
	private static final String MODEL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/attribute.model";
	
	private Pointer mAttributeHandle;
	private Pointer mTrackerHandle;
	private cv_face_t[] mFaceArray;
	private boolean mIsFirst;
	private int mLastFaceID;
	private int mCurrentFaceIdx = 0;
	private int mAttributeType;
	
	public CvFaceAttribute(int type){
		init(MODEL_PATH ,null ,type);
	}
	
	public CvFaceAttribute(String attrPath,String trackPath,int type){
		init(attrPath, trackPath, type);
	}
	
	private void init(String attrPath,String trackPath,int type){
		if(type == 0){
			mAttributeHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_create_attribute_detector(attrPath,AttributeType.CV_ATTR_FEATURE);
			mAttributeType = AttributeType.CV_ATTR_FEATURE;
		}else{
			mAttributeHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_create_attribute_detector(attrPath,AttributeType.CV_ATTR_EMOTION);
			mAttributeType = AttributeType.CV_ATTR_EMOTION;
		}
		mTrackerHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_create_detector(null, CvFaceApiBridge.CV_DETECT_SKIP_BELOW_THRESHOLD | CvFaceApiBridge.CV_DETECT_ENABLE_ALIGN);
	}

	@Override
	protected void finalize() throws Throwable {
		long start_destroy = System.currentTimeMillis();
		CvFaceApiBridge.FACESDK_INSTANCE.cv_face_destroy_attribute_detector(mAttributeHandle);
		//CvFaceApiBridge.FACESDK_INSTANCE.cv_face_destroy_tracker(mTrackerHandle);
		long end_destroy = System.currentTimeMillis();
		Log.i("attribute", "destroy cost "+(end_destroy - start_destroy)+" ms");
	}
	
	/**
	 * Given the Image by Bitmap to attribute face
	 * 
	 * @param image : Input image
	 * @return CvAttributeResult attribute result
	 */
	public CvAttributeResult attribute(Bitmap image) {
		return attribute(image, 0);
	}
	
	
	/**
	 * Given the Image by Bitmap to attribute face
	 * 
	 * @param image : Input image
	 * @return CvAttributeResult attribute result
	 */
	public CvAttributeResult attribute(Bitmap image,int rotation) {
		if(image == null || image.isRecycled()){
			return null;
		}
		Bitmap bitmap = image;
		if(rotation != 0){
			bitmap = CvUtils.getRotateBitmap(image, rotation);
		}
		int[] colorImage = CvUtils.getBGRAImageByte(bitmap);
		return attribute(colorImage,CVImageFormat.CV_PIX_FMT_BGRA8888, bitmap.getWidth(), bitmap.getHeight(),
				bitmap.getWidth());
	}
	
	/**
	 * Given the Image by Byte Array to track and attribute face
	 * 
	 * @param image  Input image
	 * 
	 * @return CvAttributeResult 
	 */
	public CvAttributeResult attribute(int[] colorImage,int cvImageFormat, int imageWidth, int imageHeight,
			int imageStride) {
		//int i = 0;
		CvAttributeResult attributeResult = new CvAttributeResult(mAttributeType);		
		PointerByReference ptrToArray = new PointerByReference();
		IntByReference ptrToSize = new IntByReference();
		int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_detect(
				mTrackerHandle, colorImage,cvImageFormat, imageWidth, imageHeight, imageStride,
				0, ptrToArray, ptrToSize);
		if (rst != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_face_detect() method failed! ResultCode="
							+ rst);
		}
		if (ptrToSize.getValue() == 0) {
			return attributeResult;
		}
		cv_face_t arrayRef = new cv_face_t(ptrToArray.getValue());
		arrayRef.read();
		mFaceArray = cv_face_t.arrayCopy((cv_face_t[]) arrayRef.toArray(ptrToSize
				.getValue()));
		if(mFaceArray.length < 0){
			return attributeResult;
		}
		
//		if (mIsFirst) {
//			mLastFaceID = mFaceArray[0].ID;
//			mCurrentFaceIdx = 0;
//
//		} else {
//			for (i = 0; i < mFaceArray.length; i++) {
//				if (mLastFaceID == mFaceArray[i].ID) {
//					mCurrentFaceIdx = i;
//					break;
//				}
//			}
//			if(i == mFaceArray.length){
//				mCurrentFaceIdx = 0;
//			}
//		}
		
//		mIsFirst = false;
		//IntByReference[] result = new IntByReference[attributeResult.getAttributeSize()];
		int[] result = new int[attributeResult.getAttributeSize()];
		rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_attribute_detect(mAttributeHandle, colorImage, cvImageFormat, imageWidth, imageHeight, imageStride, mFaceArray[mCurrentFaceIdx], result);
		attributeResult.setAttribute(result);
		attributeResult.setHasFace(true);
		if (rst != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_face_attribute_detect() method failed! ResultCode="
							+ rst);
		}				
		return attributeResult;
	}	
}
