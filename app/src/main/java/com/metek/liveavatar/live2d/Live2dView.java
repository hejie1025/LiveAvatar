package com.metek.liveavatar.live2d;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class Live2dView extends GLSurfaceView {
	private Live2dManager manager;
	private Live2dRenderer renderer;

	public Live2dView(Context context) {
		super(context);
	}

	public void setLive2DManager(Live2dManager manager) {
		this.manager = manager;
		this.renderer = new Live2dRenderer();
		this.renderer.setLive2DManager(manager);
		setRenderer(renderer);
	}
	
	public void setAction(String key, Float value) {
		manager.setAction(key, value);
	}
}
