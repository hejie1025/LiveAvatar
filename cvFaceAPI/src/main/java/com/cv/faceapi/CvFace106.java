package com.cv.faceapi;

import android.graphics.PointF;
import android.graphics.Rect;

import com.cv.faceapi.CvFaceApiBridge.*;

/**
 * CvFaceSDK API: CvFace
 * Created by Guangli W on 9/7/15.
 */
public class CvFace106 extends cv_face_106_t{

    public CvFace106() {
    }

    /**
     * Get face Rect 
     * @return Rect
     */
    public Rect getRect() {
    	Rect r = new Rect();
		r.bottom = rect.bottom;
		r.top    = rect.top;
		r.left   = rect.left;
		r.right  = rect.right;
    	return r;
    }

    /**
     * Get face Points Array 
     * @return Points Array, please check the length of array
     */
    public PointF[] getPointsArray() {
    	PointF[] ans = new PointF[points_count];
		for (int i = 0; i < points_count; i++) {
			ans[i] = new PointF();
			ans[i].x = points_array[2 * i];
			ans[i].y = points_array[2 * i + 1];
		}
    	return ans;
    }
    
    public CvFace106(cv_face_106_t origin) {
		rect.bottom = origin.rect.bottom;
		rect.top    = origin.rect.top;
		rect.left   = origin.rect.left;
		rect.right  = origin.rect.right;

		score = origin.score;
		points_count = origin.points_count;
		yaw = origin.yaw;
		pitch = origin.pitch;
		roll = origin.roll;
		eye_dist = origin.eye_dist;
		ID = origin.ID;

		for (int i = 0; i < points_array.length; i++) {
			points_array[i] = origin.points_array[i];
		}
    }
    
    @Override
    public String toString() {
		return "CvFace(" + getRect() + ", " + score + ")";
    }
}
