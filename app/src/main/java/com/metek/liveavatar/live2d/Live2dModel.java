package com.metek.liveavatar.live2d;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;

public class Live2dModel {
	/** 面部朝向X轴方向幅度 [-30, 30] */
	public static final String P_ANGLE_X = "PARAM_ANGLE_X";
	
	/** 面部朝向Y轴方向幅度 [-30, 30] */
	public static final String P_ANGLE_Y = "PARAM_ANGLE_Y";
	
	/** 面部朝向Z轴方向幅度 [-30, 30] */
	public static final String P_ANGLE_Z = "PARAM_ANGLE_Z";
	
	/** 嘴巴张开的幅度 [0, 1] */
	public static final String P_MOUTH_OPEN = "PARAM_MOUTH_OPEN_Y";
	
	/** 嘴巴形状的幅度 [-1, 1] */
	public static final String P_MOUTH_FORM = "PARAM_MOUTH_FORM";
	
	/** 左眼张开的幅度[0, 2] */
	public static final String P_EYE_L_OPEN = "PARAM_EYE_L_OPEN";
	
	/** 右眼张开的幅度[0, 2] */
	public static final String P_EYE_R_OPEN = "PARAM_EYE_R_OPEN";
	
	/** 左眉X轴方向的幅度[-1, 1] */
	public static final String P_BROW_L_X = "PARAM_BROW_L_X";
	
	/** 左眉Y轴方向的幅度[-1, 1] */
	public static final String P_BROW_L_Y = "PARAM_BROW_L_Y";
	
	/** 左眉倾斜角度的幅度[-1, 1] */
	public static final String P_BROW_L_ANGLE = "PARAM_BROW_L_ANGLE";
	
	/** 右眉X轴方向的幅度[-1, 1] */
	public static final String P_BROW_R_X = "PARAM_BROW_R_X";
	
	/** 右眉Y轴方向的幅度[-1, 1] */
	public static final String P_BROW_R_Y = "PARAM_BROW_R_Y";
	
	/** 右眉倾斜角度的幅度[-1, 1] */
	public static final String P_BROW_R_ANGLE = "PARAM_BROW_R_ANGLE";
	
	
	private Live2DModelAndroid live2DModel;
	private Live2dInterpolator inter;
	
	public Live2dModel() {
		inter = new Live2dInterpolator();
	}

	public void update(GL10 gl) {
		inter.update();
		
		live2DModel.setParamFloat(P_ANGLE_X, inter.getValue(P_ANGLE_X));
		live2DModel.setParamFloat(P_ANGLE_Y, inter.getValue(P_ANGLE_Y));
		live2DModel.setParamFloat(P_ANGLE_Z, inter.getValue(P_ANGLE_Z));
		live2DModel.setParamFloat(P_MOUTH_OPEN, inter.getValue(P_MOUTH_OPEN));
		live2DModel.setParamFloat(P_MOUTH_FORM, inter.getValue(P_MOUTH_FORM));
		live2DModel.setParamFloat(P_EYE_L_OPEN, inter.getValue(P_EYE_L_OPEN));
		live2DModel.setParamFloat(P_EYE_R_OPEN, inter.getValue(P_EYE_R_OPEN));
		live2DModel.setParamFloat(P_BROW_L_X, inter.getValue(P_BROW_L_X));
		live2DModel.setParamFloat(P_BROW_L_Y, inter.getValue(P_BROW_L_Y));
		live2DModel.setParamFloat(P_BROW_L_ANGLE, inter.getValue(P_BROW_L_ANGLE));
		live2DModel.setParamFloat(P_BROW_R_X, inter.getValue(P_BROW_R_X));
		live2DModel.setParamFloat(P_BROW_R_Y, inter.getValue(P_BROW_R_Y));
		live2DModel.setParamFloat(P_BROW_R_ANGLE, inter.getValue(P_BROW_R_ANGLE));

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
	
	public void setTargetAction(String key, Float value) {
		inter.setTargetValue(key, value);
	}
}
