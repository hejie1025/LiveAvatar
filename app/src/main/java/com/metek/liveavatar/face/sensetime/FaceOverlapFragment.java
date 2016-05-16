package com.metek.liveavatar.face.sensetime;

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

import com.cv.faceapi.CVImageFormat;
import com.cv.faceapi.CvFace106;
import com.cv.faceapi.CvFaceMultiTrack106;
import com.cv.faceapi.CvUtils;
import com.metek.liveavatar.live2d.FaceData;
import com.metek.liveavatar.live2d.FaceDataTransformer;

@SuppressWarnings("deprecation")
public class FaceOverlapFragment extends CameraOverlapFragment {
	private CvFaceMultiTrack106 tracker = null;
	private byte nv21[];
	private boolean killed = false;
	private Thread thread;
	
	private onActionChangeListener listener;
	public interface onActionChangeListener {
		public void onActionChange(FaceData data);
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
		startTrack();
	}

	@Override
	public void onPause() {
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
//					int direction = Accelerometer.getDirection();
//					if ((direction & 1) == 1) {
//						direction = (direction ^ 2);
//					}
					
					// 调用实时人脸检测函数，返回当前人脸信息
					CvFace106[] faces = tracker.track(
							temp,
							CVImageFormat.CV_PIX_FMT_NV21,
							PREVIEW_WIDTH,
							PREVIEW_HEIGHT,
							PREVIEW_WIDTH,
							3);
					
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

							FaceData faceData = FaceDataTransformer.transform(points, face.yaw, face.pitch, face.roll);
							listener.onActionChange(faceData);
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
