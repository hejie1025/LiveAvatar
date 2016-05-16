package com.metek.liveavatar.face.sensetime;

import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.metek.liveavatar.R;

import java.util.List;

@SuppressWarnings("deprecation")
public class CameraOverlapFragment extends Fragment {
	private static final String TAG = "CameraOverlapFragment";

	protected Camera camera = null;
	protected int cameraInit = 0;
	private Camera.PreviewCallback previewCallback;

	protected SurfaceView surfaceOverlap = null;
	protected SurfaceView surfaceCamera = null;
	protected SurfaceHolder surfaceHolder = null;

	private Matrix matrix = new Matrix();
	protected static final int PREVIEW_WIDTH = 640;
	protected static final int PREVIEW_HEIGHT = 480;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_camera, container, false);

		surfaceCamera = (SurfaceView) view.findViewById(R.id.surface_camera);
//		surfaceCamera.setZOrderOnTop(true);
		surfaceHolder = surfaceCamera.getHolder();
		surfaceHolder.addCallback(new SurfaceHolder.Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				if (camera != null) {
					camera.setPreviewCallback(null);
					camera.stopPreview();
					camera.release();
					camera = null;
				}
				cameraInit = 0;
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				camera = null;
				openCamera();
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				matrix.setScale(width / (float) PREVIEW_HEIGHT, height / (float) PREVIEW_WIDTH);
				initCamera();
			}
		});

		surfaceOverlap = (SurfaceView) view.findViewById(R.id.surface_overlap);
		surfaceOverlap.setZOrderOnTop(true);
		surfaceOverlap.getHolder().setFormat(PixelFormat.TRANSLUCENT);

		return view;
	}
	
	public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
		this.previewCallback = previewCallback;
		if (camera != null) {
			camera.setPreviewCallback(previewCallback);
		}
	}
	
	public Matrix getMatrix() {
		return matrix;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (cameraInit == 1 && camera == null) {
			openCamera();
		}
	}

	@Override
	public void onPause() {
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		super.onPause();
	}

	private void initCamera() {
		cameraInit = 1;
		if (camera != null) {
			try {
				Camera.Parameters params = camera.getParameters();
				params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				params.setPictureFormat(ImageFormat.JPEG);
				params.setPreviewFormat(ImageFormat.NV21);
				List<Camera.Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();
				List<Camera.Size> pictureSizes = camera.getParameters().getSupportedPictureSizes();

				for (Camera.Size size : previewSizes) {
					Log.i(TAG, "initCamera: PreviewSize width:" + size.width + " height:" + size.height);
				}
				params.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);

				Camera.Size tempSize = null;
				for (Camera.Size size : pictureSizes) {
//					if (tempSize == null && size.width >= 1280) {
					if (tempSize == null && size.width >= 320) {
						tempSize = size;
					}
					Log.i(TAG, "initCamera: PictrueSize width:" + size.width + " height:" + size.height);
				}
				params.setPictureSize(tempSize.width, tempSize.height);

				if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
					params.set("orientation", "portrait");
					params.set("rotation", 90);
					camera.setDisplayOrientation(90);
					Log.d(TAG, "orientation: portrait");
				} else {
					params.set("orientation", "landscape");
					camera.setDisplayOrientation(0);
					Log.d(TAG, "orientation: landscape");
				}

				camera.setParameters(params);
				camera.setPreviewCallback(previewCallback);
				camera.startPreview();

				Camera.Size size = camera.getParameters().getPreviewSize();
				Log.i(TAG, "initCamera After setting, previewSize width: " + size.width + " height: " + size.height);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void openCamera() {
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}

		Camera.CameraInfo info = new Camera.CameraInfo();
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, info);
			if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					camera = Camera.open(i);
				} catch (RuntimeException e) {
					e.printStackTrace();
					camera = null;
					continue;
				}
				break;
			}
		}

		try {
			camera.setPreviewDisplay(surfaceHolder);
			initCamera();
		} catch (Exception e) {
			if (camera != null) {
				camera.release();
				camera = null;
			}
		}
	}

}
