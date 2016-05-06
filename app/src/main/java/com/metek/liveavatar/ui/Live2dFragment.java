package com.metek.liveavatar.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.metek.liveavatar.R;
import com.metek.liveavatar.live2d.FaceData;
import com.metek.liveavatar.live2d.Live2dView;

public class Live2dFragment extends Fragment {
    private Live2dView live2dView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live2d, container, false);
        live2dView = (Live2dView) view.findViewById(R.id.model);
        return view;
    }

    public void setLive2dAction(FaceData data) {
        live2dView.setAction(data);
    }
}
