package com.metek.liveavatar.live2d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import jp.live2d.util.UtSystem;

public class Live2dInterpolator {
	private HashMap<String, Float> targetValues;
	private HashMap<String, Float> currentValues;
	private HashMap<String, Float> tempValues;
	
	public Live2dInterpolator() {
		targetValues = new HashMap<String, Float>();
		currentValues = new HashMap<String, Float>();
		tempValues = new HashMap<String, Float>();
		
		targetValues.put(Live2dModel.P_ANGLE_X, 0f);
		targetValues.put(Live2dModel.P_ANGLE_Y, 0f);
		targetValues.put(Live2dModel.P_ANGLE_Z, 0f);
		targetValues.put(Live2dModel.P_MOUTH_OPEN, 0f);
		targetValues.put(Live2dModel.P_MOUTH_FORM, 0f);
		targetValues.put(Live2dModel.P_EYE_L_OPEN, 1f);
		targetValues.put(Live2dModel.P_EYE_R_OPEN, 1f);
		targetValues.put(Live2dModel.P_BROW_L_X, 0f);
		targetValues.put(Live2dModel.P_BROW_L_Y, 0f);
		targetValues.put(Live2dModel.P_BROW_L_ANGLE, 0f);
		targetValues.put(Live2dModel.P_BROW_R_X, 0f);
		targetValues.put(Live2dModel.P_BROW_R_Y, 0f);
		targetValues.put(Live2dModel.P_BROW_R_ANGLE, 0f);
		
		currentValues.put(Live2dModel.P_ANGLE_X, 0f);
		currentValues.put(Live2dModel.P_ANGLE_Y, 0f);
		currentValues.put(Live2dModel.P_ANGLE_Z, 0f);
		currentValues.put(Live2dModel.P_MOUTH_OPEN, 0f);
		currentValues.put(Live2dModel.P_MOUTH_FORM, 0f);
		currentValues.put(Live2dModel.P_EYE_L_OPEN, 1f);
		currentValues.put(Live2dModel.P_EYE_R_OPEN, 1f);
		currentValues.put(Live2dModel.P_BROW_L_X, 0f);
		currentValues.put(Live2dModel.P_BROW_L_Y, 0f);
		currentValues.put(Live2dModel.P_BROW_L_ANGLE, 0f);
		currentValues.put(Live2dModel.P_BROW_R_X, 0f);
		currentValues.put(Live2dModel.P_BROW_R_Y, 0f);
		currentValues.put(Live2dModel.P_BROW_R_ANGLE, 0f);
		
		tempValues.put(Live2dModel.P_ANGLE_X, 0f);
		tempValues.put(Live2dModel.P_ANGLE_Y, 0f);
		tempValues.put(Live2dModel.P_ANGLE_Z, 0f);
		tempValues.put(Live2dModel.P_MOUTH_OPEN, 0f);
		tempValues.put(Live2dModel.P_MOUTH_FORM, 0f);
		tempValues.put(Live2dModel.P_EYE_L_OPEN, 0f);
		tempValues.put(Live2dModel.P_EYE_R_OPEN, 0f);
		tempValues.put(Live2dModel.P_BROW_L_X, 0f);
		tempValues.put(Live2dModel.P_BROW_L_Y, 0f);
		tempValues.put(Live2dModel.P_BROW_L_ANGLE, 0f);
		tempValues.put(Live2dModel.P_BROW_R_X, 0f);
		tempValues.put(Live2dModel.P_BROW_R_Y, 0f);
		tempValues.put(Live2dModel.P_BROW_R_ANGLE, 0f);
	}
	
	public void setTargetValue(String key, float value) {
		if (key.equals(Live2dModel.P_ANGLE_X)
				|| key.equals(Live2dModel.P_ANGLE_Y)
				|| key.equals(Live2dModel.P_ANGLE_Z)) {			
//			if (Math.abs(tempValues.get(key) - value) < 3) {
//				return;
//			}
			if (value > 30) {
				value = 30;
			} else if (value < -30) {
				value = -30;
			}
		} else if (key.equals(Live2dModel.P_MOUTH_OPEN)) {
			if (Math.abs(targetValues.get(key) - value) < 0.1) {
				return;
			}
		} else {
			if (Math.abs(targetValues.get(key) - value) < 0.2) {
				return;
			}
		}
		targetValues.put(key, value);
	}
	
	public float getValue(String key) {
		return targetValues.get(key);
	}
	
	private long lastTimeSec = 0;
	
	public void update() {		
		if (lastTimeSec == 0) {
			lastTimeSec = UtSystem.getUserTimeMSec();
			return;
		}

		long curTimeSec = UtSystem.getUserTimeMSec();
		float deltaTimeWeight = (float) (curTimeSec - lastTimeSec) * 30f / 1000f;
		lastTimeSec = curTimeSec;
		
		Iterator<Entry<String, Float>> iter = targetValues.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Float> entry = (Entry<String, Float>) iter.next();
			String key = entry.getKey();
			
			float maxV = 2.0f;
			float maxA = deltaTimeWeight / 10f * maxV;
			
			float targetX = entry.getValue();
			float curX = currentValues.get(key);
			float curV = tempValues.get(key);
			
			float d = targetX - curX;
			float v = maxV * d / Math.abs(d);
			float a = v - curV;
			if (Math.abs(a) > maxA) {
				a = maxA * a / Math.abs(a);
			}
			curV += a;
			curX += curV;
			
			currentValues.put(key, curX);
			tempValues.put(key, curV);
			
		}
		
		{
			float x = currentValues.get(Live2dModel.P_ANGLE_X);
			float _x = currentValues.get(Live2dModel.P_ANGLE_X);
			float _v = tempValues.get(Live2dModel.P_ANGLE_X);
			
			float vmax = 2.0f;
			float amax = 0.5f;
			float df = deltaTimeWeight;
			float ds = x - _x;
			float f = (ds / Math.abs(ds) * vmax - _v) / amax;
			if (df < f) {
				float v = _v + amax * df;
				float s = _v * df + 0.5f * amax * df * df;
				if (s > ds) {
					_x = x;
					_v = 0;
				} else {
					_x = _x + s;
				}
			}
			
		}
	}
}
