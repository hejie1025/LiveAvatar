package com.metek.liveavatar.live2d;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;

public class Live2dModel {
	private Live2DModelAndroid live2DModel;
	private Live2dInterpolator inter;
	
	public Live2dModel() {
		inter = new Live2dInterpolator();
	}

	public void update(GL10 gl) {
		inter.update();
		
		live2DModel.setParamFloat("PARAM_ANGLE_X", inter.getValue(FaceData.P_ANGLE_X));
		live2DModel.setParamFloat("PARAM_ANGLE_Y", inter.getValue(FaceData.P_ANGLE_Y));
		live2DModel.setParamFloat("PARAM_ANGLE_Z", inter.getValue(FaceData.P_ANGLE_Z));
		live2DModel.setParamFloat("PARAM_MOUTH_OPEN_Y", inter.getValue(FaceData.P_MOUTH_OPEN));
		live2DModel.setParamFloat("PARAM_MOUTH_FORM", inter.getValue(FaceData.P_MOUTH_FORM));
		live2DModel.setParamFloat("PARAM_EYE_L_OPEN", inter.getValue(FaceData.P_EYE_L_OPEN));
		live2DModel.setParamFloat("PARAM_EYE_R_OPEN", inter.getValue(FaceData.P_EYE_R_OPEN));
		live2DModel.setParamFloat("PARAM_BROW_L_Y", inter.getValue(FaceData.P_BROW_L_Y));
		live2DModel.setParamFloat("PARAM_BROW_R_Y", inter.getValue(FaceData.P_BROW_R_Y));

		live2DModel.setGL(gl);
		live2DModel.update();
		live2DModel.draw();
	}

	public void release() {
		if (live2DModel != null) {
			live2DModel.deleteTextures();
		}
	}

	public void load(GL10 gl) {
		final String MODEL_PATH = "haru/haru.moc";
		final String TEXTURE_PATHS[] = {
				"haru/haru.1024/texture_00.png",
				"haru/haru.1024/texture_01.png",
				"haru/haru.1024/texture_02.png" };
		try {
			InputStream in = FileManager.open(MODEL_PATH);
			live2DModel = Live2DModelAndroid.loadModel(in);
			in.close();

			for (int i = 0; i < TEXTURE_PATHS.length; i++) {
				InputStream tin = FileManager.open(TEXTURE_PATHS[i]);
				int texNo = UtOpenGL.loadTexture(gl, tin, true);
				live2DModel.setTexture(i, texNo);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public float getCanvasWidth() {
		return live2DModel.getCanvasWidth();
	}
	
	public void setTargetAction(FaceData data) {
		inter.setTargetValue(data);
	}
}
