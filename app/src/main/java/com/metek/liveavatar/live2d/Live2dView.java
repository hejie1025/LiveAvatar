package com.metek.liveavatar.live2d;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class Live2dView extends GLSurfaceView {
	private Live2dManager manager;
	private Live2dRenderer renderer;

	public Live2dView(Context context) {
		this(context, null);
	}

	public Live2dView(Context context, AttributeSet attrs) {
		super(context, attrs);

		isInEditMode();

		this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		manager = new Live2dManager();
		renderer = new Live2dRenderer();
		renderer.setLive2DManager(manager);
		this.setRenderer(renderer);
		this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		this.setZOrderOnTop(true);
	}
	
	public void setAction(FaceData data) {
		manager.setAction(data);
	}
}
