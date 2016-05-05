package com.metek.liveavatar.live2d;

import javax.microedition.khronos.opengles.GL10;

import jp.live2d.Live2D;

public class Live2dManager {
	private Live2dModel model;

	public Live2dManager() {
		Live2D.init();
	}

	public void update(GL10 gl) {
		model.update(gl);
	}

	public void setAction(FaceData data) {
		model.setTargetAction(data);
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
