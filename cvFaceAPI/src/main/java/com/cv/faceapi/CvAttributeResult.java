package com.cv.faceapi;

import android.util.Log;

import com.cv.faceapi.CvFaceAttribute.AttributeType;


public class CvAttributeResult {
	
	private static final boolean DEBUG = true;
	private static final String TAG = "CvAttributeResult";
	
	private int mAttributeType;
	private int[] mAttribute;
	private boolean mHasFace;
	
	public class Feature {
		public static final int CV_FEATURE_AGE = 0;	///< Real age 0 - 100
		public static final int CV_FEATURE_GENDER_MALE = 1;	
		public static final int CV_FEATURE_ATTRACTIVE = 2;	 ///< 魅力 0 - 100
		public static final int CV_FEATURE_EYEGLASS = 3;	
		public static final int CV_FEATURE_SUNGLASS = 4;	///< 太阳镜 false 0, true 1
		public static final int CV_FEATURE_SMILE = 5;		
		public static final int CV_FEATURE_MASK = 6;	///< 面具 false 0, true 1	
		public static final int CV_FEATURE_RACE = 7;	///< 种族 Yellow 0, Black 1,White 2	
		public static final int CV_FEATURE_EYE_OPEN = 8;	///< 眼睛睁开 false 0, true 1	
		public static final int CV_FEATURE_MOUTH_OPEN = 9;	///< 嘴巴张开 false 0, true 1	
		public static final int CV_FEATURE_BEARD = 10;		///< 有胡子 false 0, true 1
		public static final int CV_FEATURE_LENGTH = 11;		///< attribute feature length
	} 

	public class Emotion {
		public static final int CV_EMOTION_ANGRY = 0;
		public static final int CV_EMOTION_CALM = 1;
		public static final int CV_EMOTION_CONFUSED = 2;
		public static final int CV_EMOTION_DISGUST = 3;
		public static final int CV_EMOTION_HAPPY = 4;
		public static final int CV_EMOTION_SAD = 5;
		public static final int CV_EMOTION_SCARED = 6;
		public static final int CV_EMOTION_SUPRISED = 7;
		public static final int CV_EMOTION_SQUINT = 8;
		public static final int CV_EMOTION_SCREAM = 9;
		public static final int CV_EMOTION_LENGTH = 10;
	}
	
	public CvAttributeResult(int type){
		if(type == 0){
			mAttributeType = AttributeType.CV_ATTR_FEATURE;
			mAttribute = new int[Feature.CV_FEATURE_LENGTH]; 
		}else{
			mAttributeType = 1;
			mAttribute = new int[Emotion.CV_EMOTION_LENGTH]; 
		}
	}
	
	public int[] getAttribute(){
		return mAttribute;
	}
	
	public void setAttribute(int[] attr){
		if(attr == null || attr.length == 0 || attr.length != attr.length){
			Log.d(TAG, "setAttribute param is inlegal");
			return;
		}
		System.arraycopy(attr, 0, mAttribute, 0, attr.length);
	}
	
	public int getAttributeSize(){
		return mAttribute.length;
	}

	public int getAttributeType() {
		return mAttributeType;
	}
	
	public void setHasFace(boolean hasFace){
		mHasFace = hasFace;
	}
	
	public int getAge(){
		if(mAttributeType == AttributeType.CV_ATTR_EMOTION){
			return 0;
		}
		return mAttribute[Feature.CV_FEATURE_AGE];
	}
	
	public boolean isMale(){
		if(mAttributeType == AttributeType.CV_ATTR_EMOTION){
			return false;
		}
		return mAttribute[Feature.CV_FEATURE_GENDER_MALE] > 0;
	}
	
	public int getAttrActive(){
		if(mAttributeType == AttributeType.CV_ATTR_EMOTION){
			return 0;
		}
		return mAttribute[Feature.CV_FEATURE_ATTRACTIVE];
	}
	
	public boolean isEyeGlass(){
		if(mAttributeType == AttributeType.CV_ATTR_EMOTION){
			return false;
		}
		return mAttribute[Feature.CV_FEATURE_EYEGLASS] > 0;
	}
	
	public boolean isSunGlass(){
		if(mAttributeType == AttributeType.CV_ATTR_EMOTION){
			return false;
		}
		return mAttribute[Feature.CV_FEATURE_SUNGLASS] > 0;
	}
	
	public boolean isSmile(){
		if(mAttributeType == AttributeType.CV_ATTR_EMOTION){
			return false;
		}
		return mAttribute[Feature.CV_FEATURE_SMILE] > 0;
	}
	public boolean isMask(){
		if(mAttributeType == AttributeType.CV_ATTR_EMOTION){
			return false;
		}
		return mAttribute[Feature.CV_FEATURE_MASK] > 0;
	}
	public int getRace(){
		if(mAttributeType == AttributeType.CV_ATTR_EMOTION){
			return 0;
		}
		return mAttribute[Feature.CV_FEATURE_RACE];
	}
	public boolean isEyeOpen(){
		if(mAttributeType == AttributeType.CV_ATTR_EMOTION){
			return false;
		}
		return mAttribute[Feature.CV_FEATURE_EYE_OPEN] > 0;
	}
	public boolean isMoutnOpen(){
		if(mAttributeType == AttributeType.CV_ATTR_EMOTION){
			return false;
		}
		return mAttribute[Feature.CV_FEATURE_MOUTH_OPEN] > 0;
	}
	public boolean isBeard(){
		if(mAttributeType == AttributeType.CV_ATTR_EMOTION){
			return false;
		}
		return mAttribute[Feature.CV_FEATURE_BEARD] > 0;
	}
	
	
	//emotion
	public int isAngry(){
		if(mAttributeType == AttributeType.CV_ATTR_FEATURE){
			return 0;
		}
		return mAttribute[Emotion.CV_EMOTION_ANGRY];
	}
	public int isCalm(){
		if(mAttributeType == AttributeType.CV_ATTR_FEATURE){
			return 0;
		}
		return mAttribute[Emotion.CV_EMOTION_CALM];
	}
	public int isConfused(){
		if(mAttributeType == AttributeType.CV_ATTR_FEATURE){
			return 0;
		}
		return mAttribute[Emotion.CV_EMOTION_CONFUSED];
	}
	public int isDisgust(){
		if(mAttributeType == AttributeType.CV_ATTR_FEATURE){
			return 0;
		}
		return mAttribute[Emotion.CV_EMOTION_DISGUST];
	}
	public int isHappy(){
		if(mAttributeType == AttributeType.CV_ATTR_FEATURE){
			return 0;
		}
		return mAttribute[Emotion.CV_EMOTION_HAPPY];
	}
	public int isSad(){
		if(mAttributeType == AttributeType.CV_ATTR_FEATURE){
			return 0;
		}
		return mAttribute[Emotion.CV_EMOTION_SAD];
	}
	public int isScared(){
		if(mAttributeType == AttributeType.CV_ATTR_FEATURE){
			return 0;
		}
		return mAttribute[Emotion.CV_EMOTION_SCARED];
	}
	public int isSuprised(){
		if(mAttributeType == AttributeType.CV_ATTR_FEATURE){
			return 0;
		}
		return mAttribute[Emotion.CV_EMOTION_SUPRISED];
	}
	public int isSquint(){
		if(mAttributeType == AttributeType.CV_ATTR_FEATURE){
			return 0;
		}
		return mAttribute[Emotion.CV_EMOTION_SQUINT];
	}
	public int isScream(){
		if(mAttributeType == AttributeType.CV_ATTR_FEATURE){
			return 0;
		}
		return mAttribute[Emotion.CV_EMOTION_SCREAM];
	}
	
	

	@Override
	public String toString() {
		if(mAttributeType == AttributeType.CV_ATTR_EMOTION){
			return "mAttributeType : " + mAttributeType + " hasFace : " + mHasFace + " age : " + getAge() + " isMale : " + isMale()
					+ " active : " + getAttrActive() 
					+ " eyeglass : " + isEyeGlass() 
					+ " sunglass = " + isSunGlass() 
					+ " smile : " +isSmile()
					+ "mask : " + isMask();
		}else{
			return null;
		}
	}
	
}
