package com.metek.liveavatar.face;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cv.faceapi.Accelerometer;
import com.cv.faceapi.CVImageFormat;
import com.cv.faceapi.CvFace106;
import com.cv.faceapi.CvFaceMultiTrack106;
import com.cv.faceapi.CvUtils;
import com.metek.liveavatar.live2d.Live2dModel;
import com.metek.liveavatar.ui.ChatActivity;

@SuppressWarnings("deprecation")
public class FaceOverlapFragment extends CameraOverlapFragment {
	private CvFaceMultiTrack106 tracker = null;
	private byte nv21[];
	private boolean killed = false;
	private Thread thread;
	
	private onActionChangeListener listener;
	public interface onActionChangeListener {
		public void onActionChange(String actionKey, float actionValue);
	}
	
	public void setOnActionChangeListener(onActionChangeListener listener) {
		this.listener = listener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
		nv21 = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
		setPreviewCallback(new Camera.PreviewCallback() {
			
			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				synchronized (nv21) {
					System.arraycopy(data, 0, nv21, 0, data.length);
				}
			}
		});
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (ChatActivity.acc != null)
			ChatActivity.acc.start();
		startTrack();
	}

	@Override
	public void onPause() {
		if (ChatActivity.acc != null)
			ChatActivity.acc.stop();
		stopTrack();
		super.onPause();
	}
	
	private void startTrack() {
		if (tracker == null) {
			tracker = new CvFaceMultiTrack106();
		}
		
		killed = false;
		final byte[] temp = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
		thread = new Thread() {

			@Override
			public void run() {
				while (!killed) {
					if (nv21 == null) {
						continue;
					}
					
					synchronized (nv21) {
						System.arraycopy(nv21, 0, temp, 0, nv21.length);
					}
					
					// 重力传感器返回的方向
					int direction = Accelerometer.getDirection();
					if ((direction & 1) == 1) {
						direction = (direction ^ 2);
					}
					
					// 调用实时人脸检测函数，返回当前人脸信息
					CvFace106[] faces = tracker.track(
							temp,
							CVImageFormat.CV_PIX_FMT_NV21,
							PREVIEW_WIDTH,
							PREVIEW_HEIGHT,
							PREVIEW_WIDTH,
							direction);
					
					// 绘制人脸框
					if (faces != null) {
						Canvas canvas = surfaceOverlap.getHolder().lockCanvas();
						if (canvas == null) {
							continue;
						}
						canvas.drawColor(0, PorterDuff.Mode.CLEAR);
						canvas.setMatrix(getMatrix());
						for (CvFace106 face : faces) {
							Rect rect = CvUtils.RotateDeg270(face.getRect(), PREVIEW_WIDTH, PREVIEW_HEIGHT);
							PointF[] points = face.getPointsArray();
							for (int i = 0; i < points.length; i++) {
								points[i] = CvUtils.RotateDeg270(points[i], PREVIEW_WIDTH, PREVIEW_HEIGHT);
							}
							
//							CvUtils.drawFaceRect(canvas, rect, PREVIEW_WIDTH, PREVIEW_HEIGHT, true);
							CvUtils.drawPoints(canvas, points, PREVIEW_HEIGHT, PREVIEW_WIDTH, true);
//							drawPath(canvas, points[97], points[98], points[99], points[101], points[102], points[103], points[97]);
//							drawPath(canvas, points[53], points[54], points[56], points[57], points[53]);
//							drawPath(canvas, points[59], points[60], points[62], points[63], points[59]);
//							drawPath(canvas, points, 0, 32);
//							drawPath(canvas, points, 84, 95);
//							drawPath(canvas, points[95], points[84]);
//							drawPath(canvas, points, 33, 37);
//							drawPath(canvas, points, 38, 42);
//							drawPath(canvas, points, 43, 46);
//							drawPath(canvas, points, 47, 51);
//							drawPath(canvas, points, 52, 57);
//							drawPath(canvas, points[57], points[52]);
//							drawPath(canvas, points, 58, 63);
//							drawPath(canvas, points[63], points[58]);

							listener.onActionChange(Live2dModel.P_ANGLE_X, face.yaw);
							listener.onActionChange(Live2dModel.P_ANGLE_Y, -face.pitch);
							listener.onActionChange(Live2dModel.P_ANGLE_Z, face.roll / Math.abs(face.roll) * (Math.abs(face.roll) - 90));

//							float mouthInWidth = Math.abs(points[97].x - points[99].x);
//							float mouthInHeight = Math.abs(points[97].y - points[103].y);
//							if (Float.compare(mouthInWidth, 0.0f) != 0) {
//								listener.onActionChange(Live2dModel.P_MOUTH_OPEN, mouthInHeight / mouthInWidth / 2);
//							}
//							
//							float mouthWidth = 0.0f;
//							if (15f - face.yaw > 0) {
//								mouthWidth = Math.abs(points[96].x -points[84].x);
//							} else {
//								mouthWidth = Math.abs(points[100].x -points[90].x);
//							}
//							float mouthHeight = points[96].y -points[84].y;
//							if (Float.compare(mouthWidth, 0.0f) != 0) {
//								float rate = mouthHeight / mouthWidth + 0.05f;
//								rate = rate < -0.1f ? -0.1f : rate;
//								rate = rate > 0.1f ? 0.1f : rate;
//								listener.onActionChange(Live2dModel.P_MOUTH_FORM, rate * 10);
//							}
//
//							float eyeRightWidth = Math.abs(points[53].x - points[54].x);
//							float eyeRightHeight = Math.abs(points[72].y - points[73].y);
//							if (Float.compare(mouthInWidth, 0.0f) != 0) {
//								float rate = eyeRightHeight / eyeRightWidth;
//								rate = rate < 0.8f ? 0.8f : rate;
//								rate = rate > 1.0f ? 1.0f : rate;
//								float rate0_2 = (rate - 0.8f) / 0.2f * 2;
//								listener.onActionChange(Live2dModel.P_EYE_L_OPEN, rate0_2);
//							}
//
//							float eyeLeftWidth = Math.abs(points[59].x - points[60].x);
//							float eyeLeftHeight = Math.abs(points[75].y - points[76].y);
//							if (Float.compare(mouthInWidth, 0.0f) != 0) {
//								float rate = eyeLeftHeight / eyeLeftWidth;
//								rate = rate < 0.8f ? 0.8f : rate;
//								rate = rate > 1.0f ? 1.0f : rate;
//								float rate0_2 = (rate - 0.8f) / 0.2f * 2;
//								listener.onActionChange(Live2dModel.P_EYE_R_OPEN, rate0_2);
//							}
//							
//							float nose = Math.abs(points[43].y - points[44].y);
//							float browRightY = Math.abs(points[65].y - points[72].y);
//							if (Float.compare(nose, 0.0f) != 0) {
//								float rate = browRightY / nose - 1.2f;
//								listener.onActionChange(Live2dModel.P_BROW_L_Y, rate * 2);
//							}
//							
//							float browLeftY = Math.abs(points[70].y - points[75].y);
//							if (Float.compare(nose, 0.0f) != 0) {
//								float rate = browLeftY / nose - 1.2f;
//								listener.onActionChange(Live2dModel.P_BROW_R_Y, rate * 2);
//							}
						}
						surfaceOverlap.getHolder().unlockCanvasAndPost(canvas);
					}
				}
			}
		};
		thread.start();
	}
	
	private void drawPath(Canvas canvas, PointF... fs) {
		Paint paint = new Paint(); 
		paint.setColor(Color.BLUE);
		paint.setStrokeWidth(3);
		paint.setStyle(Style.STROKE);
		
		Path path = new Path();
		for (int i = 0; i < fs.length; i++) {
			if (i == 0) {
				path.moveTo(fs[i].x, fs[i].y);
			}
			path.lineTo(fs[i].x, fs[i].y);
		}
		canvas.drawPath(path, paint);
	}
	
	private void drawPath(Canvas canvas, PointF[]fs, int start, int end) {
		Paint paint = new Paint(); 
		paint.setColor(Color.BLUE);
		paint.setStrokeWidth(3);
		paint.setStyle(Style.STROKE);
		
		Path path = new Path();
		for (int i = start; i <= end; i++) {
			if (i == start) {
				path.moveTo(fs[i].x, fs[i].y);
			}
			path.lineTo(fs[i].x, fs[i].y);
		}
		canvas.drawPath(path, paint);
	}
	
	private void stopTrack() {
		killed = true;
		if (thread != null)
			try {
				thread.join(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		if (tracker != null) {
			tracker = null;
		}
	}
}
