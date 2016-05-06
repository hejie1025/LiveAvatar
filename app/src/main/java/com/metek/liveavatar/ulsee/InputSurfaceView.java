package com.metek.liveavatar.ulsee;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

public class InputSurfaceView extends SurfaceView {
    public interface OnInputSurfaceEventHandler {
        void onInputSurfaceSingleTapHandler(MotionEvent event);
    }
    public OnInputSurfaceEventHandler mEventHandler;

    public InputSurfaceView(Context context, AttributeSet set) {
        super(context, set);
    }

    public void setInputEventHandler(OnInputSurfaceEventHandler handler) { mEventHandler = handler; }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mEventHandler != null) {
                mEventHandler.onInputSurfaceSingleTapHandler(event);
            }
        }
        return true;
    }
}
