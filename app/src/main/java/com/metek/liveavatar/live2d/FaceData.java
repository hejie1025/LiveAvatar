package com.metek.liveavatar.live2d;

import org.json.JSONObject;

import java.util.HashMap;

public class FaceData extends HashMap<String, Float> {
    /** 面部朝向X轴方向幅度 [-30, 30] */
    public static final String P_ANGLE_X = "yaw";

    /** 面部朝向Y轴方向幅度 [-30, 30] */
    public static final String P_ANGLE_Y = "pitch";

    /** 面部朝向Z轴方向幅度 [-30, 30] */
    public static final String P_ANGLE_Z = "roll";

    /** 嘴巴张开的幅度 [0, 1] */
    public static final String P_MOUTH_OPEN = "mouth";

    /** 嘴巴形状的幅度 [-1, 1] */
    public static final String P_MOUTH_FORM = "mouthform";

    /** 左眼张开的幅度[0, 2] */
    public static final String P_EYE_L_OPEN = "lefteye";

    /** 右眼张开的幅度[0, 2] */
    public static final String P_EYE_R_OPEN = "righteye";

    /** 左眉Y轴方向的幅度[-1, 1] */
    public static final String P_BROW_L_Y = "leftbrow";

    /** 右眉Y轴方向的幅度[-1, 1] */
    public static final String P_BROW_R_Y = "rightbrow";

    public FaceData(JSONObject json) {
        put(P_ANGLE_X, new Float(json.optDouble(P_ANGLE_X)));
        put(P_ANGLE_Y, new Float(json.optDouble(P_ANGLE_Y)));
        put(P_ANGLE_Z, new Float(json.optDouble(P_ANGLE_Z)));
        put(P_MOUTH_OPEN, new Float(json.optDouble(P_MOUTH_OPEN)));
        put(P_MOUTH_FORM, new Float(json.optDouble(P_MOUTH_FORM)));
        put(P_EYE_L_OPEN, new Float(json.optDouble(P_EYE_L_OPEN)));
        put(P_EYE_R_OPEN, new Float(json.optDouble(P_EYE_R_OPEN)));
        put(P_BROW_L_Y, new Float(json.optDouble(P_BROW_L_Y)));
        put(P_BROW_R_Y, new Float(json.optDouble(P_BROW_R_Y)));
    }

    public FaceData() {
        super();
    }
}

