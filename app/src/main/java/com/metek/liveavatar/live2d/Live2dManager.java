package com.metek.liveavatar.live2d;

import android.app.Activity;

import javax.microedition.khronos.opengles.GL10;

import jp.live2d.Live2D;

public class Live2dManager {
	private Live2dView view;
	private Live2dModel model;

	public Live2dManager() {
		Live2D.init();
	}

	public void update(GL10 gl) {
		model.update(gl);
	}

	public void setAction(String key, Float value) {
		model.setTargetAction(key, value);
	}
	
	public Live2dView createView(Activity activity) {
		view = new Live2dView(activity);
		view.setLive2DManager(this);
		return view;
	}

	public Live2dModel getModel() {
		return model;
	}

	public void createModel(GL10 gl) {
		releaseModel();
		model = new Live2dModel();
		model.load(gl);
	}

	public void releaseModel() {
		if (model != null) {
			model.release();
		}
	}
}
