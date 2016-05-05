package com.metek.liveavatar.live2d;

import android.graphics.PointF;

public class FaceDataTransformer {
    public static FaceData transform(PointF[] points, float yaw, float pitch, float roll) {
        FaceData faceData = new FaceData();

        faceData.put(FaceData.P_ANGLE_X, yaw);
        faceData.put(FaceData.P_ANGLE_Y, -pitch);
        faceData.put(FaceData.P_ANGLE_Z, roll / Math.abs(roll) * (Math.abs(roll) - 90));

        float mouthInWidth = Math.abs(points[97].x - points[99].x);
        float mouthInHeight = Math.abs(points[97].y - points[103].y);
        if (Float.compare(mouthInWidth, 0.0f) != 0) {
            faceData.put(FaceData.P_MOUTH_OPEN, mouthInHeight / mouthInWidth / 2);
        } else {
            faceData.put(FaceData.P_MOUTH_OPEN, 0f);
        }

        float mouthWidth = 0.0f;
        if (15f - yaw > 0) {
            mouthWidth = Math.abs(points[96].x - points[84].x);
        } else {
            mouthWidth = Math.abs(points[100].x - points[90].x);
        }
        float mouthHeight = points[96].y - points[84].y;
        if (Float.compare(mouthWidth, 0.0f) != 0) {
            float rate = mouthHeight / mouthWidth + 0.05f;
            rate = rate < -0.1f ? -0.1f : rate;
            rate = rate > 0.1f ? 0.1f : rate;
            faceData.put(FaceData.P_MOUTH_FORM, rate * 10);
        } else {
            faceData.put(FaceData.P_MOUTH_FORM, 0f);
        }

        float eyeRightWidth = Math.abs(points[53].x - points[54].x);
        float eyeRightHeight = Math.abs(points[72].y - points[73].y);
        if (Float.compare(mouthInWidth, 0.0f) != 0) {
            float rate = eyeRightHeight / eyeRightWidth;
            rate = rate < 0.8f ? 0.8f : rate;
            rate = rate > 1.0f ? 1.0f : rate;
            float rate0_2 = (rate - 0.8f) / 0.2f * 2;
            faceData.put(FaceData.P_EYE_L_OPEN, rate0_2);
        } else {
            faceData.put(FaceData.P_EYE_L_OPEN, 1f);
        }

        float eyeLeftWidth = Math.abs(points[59].x - points[60].x);
        float eyeLeftHeight = Math.abs(points[75].y - points[76].y);
        if (Float.compare(mouthInWidth, 0.0f) != 0) {
            float rate = eyeLeftHeight / eyeLeftWidth;
            rate = rate < 0.8f ? 0.8f : rate;
            rate = rate > 1.0f ? 1.0f : rate;
            float rate0_2 = (rate - 0.8f) / 0.2f * 2;
            faceData.put(FaceData.P_EYE_R_OPEN, rate0_2);
        } else {
            faceData.put(FaceData.P_EYE_R_OPEN, 1f);
        }

        float nose = Math.abs(points[43].y - points[44].y);
        float browRightY = Math.abs(points[65].y - points[72].y);
        if (Float.compare(nose, 0.0f) != 0) {
            float rate = browRightY / nose - 1.2f;
            faceData.put(FaceData.P_BROW_L_Y, rate * 2);
        } else {
            faceData.put(FaceData.P_BROW_L_Y, 0f);
        }

        float browLeftY = Math.abs(points[70].y - points[75].y);
        if (Float.compare(nose, 0.0f) != 0) {
            float rate = browLeftY / nose - 1.2f;
            faceData.put(FaceData.P_BROW_R_Y, rate * 2);
        } else {
            faceData.put(FaceData.P_BROW_R_Y, 0f);
        }

        return faceData;
    }
}
