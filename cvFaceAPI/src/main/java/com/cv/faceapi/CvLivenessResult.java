package com.cv.faceapi;

public class CvLivenessResult {
	public static final int CVLIVENESSRESULT_STATUS_NULL = -1;
	public static final int CVLIVENESSRESULT_STATUS_RESET = -2;
	public static final int CVLIVENESSRESULT_STATUS_BLINK = 1;
	public static final int CVLIVENESSRESULT_STATUS_MOUTH = 2;
	public static final int CVLIVENESSRESULT_STATUS_SHUT = 4;
	public static final int CVLIVENESSRESULT_STATUS_NOD = 8;
	public static final int CVLIVENESSRESULT_STATUS_NOTHING = 1024;
	public static final int CVLIVENESSRESULT_STATUS_SUCCESS = 2048;
	
	public int mStatus;
	public float mScore;
	public CvFace21 mFaceRect;
}
