package com.metek.liveavatar.live2d;

import android.opengl.GLSurfaceView.Renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Live2dRenderer implements Renderer {
    private Live2dManager manager;

    public void setLive2DManager(Live2dManager manager) {
        this.manager = manager;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        manager.createModel(gl);
        gl.glClearColor(0, 0, 0, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        float modelWidth = manager.getModel().getCanvasWidth();
        float aspect = (float) width / height;

        gl.glOrthof(0, modelWidth, modelWidth / aspect, 0, 0.5f, -0.5f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        manager.update(gl);
    }
}
