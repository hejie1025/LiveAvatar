package com.metek.liveavatar.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cv.faceapi.Accelerometer;
import com.metek.liveavatar.R;
import com.metek.liveavatar.face.FaceOverlapFragment;
import com.metek.liveavatar.live2d.FileManager;
import com.metek.liveavatar.live2d.Live2dManager;
import com.metek.liveavatar.live2d.Live2dView;
import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.NetConst;
import com.metek.liveavatar.socket.NetUtils;
import com.metek.liveavatar.socket.TCPManager;
import com.metek.liveavatar.socket.receive.RecMsgMatch;
import com.metek.liveavatar.socket.send.MsgMatch;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatActivity extends AppCompatActivity implements FaceOverlapFragment.onActionChangeListener {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final int STATE_MIRROR = 1;
    private static final int STATE_MATCH = 2;
    private int state = STATE_MIRROR;

    private ImageView ivButton;
    private ImageView ivProgress;
    private EditText etFriendId;
    private EditText etPort;

    public static Accelerometer acc;
    private Live2dView view;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        FileManager.init(this.getApplicationContext());

        view = new Live2dManager().createView(this);
        ViewGroup live2dFrame = (ViewGroup) findViewById(R.id.live2d_frame);
        live2dFrame.addView(view);

        FaceOverlapFragment fragment = (FaceOverlapFragment) getFragmentManager()
                .findFragmentById(R.id.face_overlap_fragment);
        fragment.setOnActionChangeListener(this);

        acc = new Accelerometer(this);
        acc.start();


        ivButton = (ImageView) findViewById(R.id.button);
        ivProgress = (ImageView) findViewById(R.id.progress);
        etFriendId = (EditText) findViewById(R.id.friend_id);
        etPort = (EditText) findViewById(R.id.port);

        TCPManager.getManager().setConnectListener(tcpListener);
    }

    @Override
    public void onActionChange(String actionKey, float actionValue) {
        view.setAction(actionKey, actionValue);
    }

    TCPManager.ConnectListener tcpListener = new TCPManager.ConnectListener() {
        @Override
        public void onConnect(int state, MsgData data) {
            if (state != CONN_OK) return;
            if (data == null) return;

            switch (data.code) {
                case NetConst.CODE_MATCH:
                    Log.v(TAG, "匹配成功");
                    RecMsgMatch recMsgMatch = new RecMsgMatch(data);
                    break;
                case NetConst.CODE_SEND_DATA:
                    Log.v(TAG, "接受心跳包");
                    break;
            }
        }
    };

    public void match(View view) {
        if (state == STATE_MIRROR) {
            Log.v(TAG, "开始匹配");
            MsgMatch msgMatch = new MsgMatch();
            TCPManager.getManager().send(msgMatch);

            ivButton.setImageResource(R.drawable.selector_cancel);
            ivProgress.setVisibility(View.VISIBLE);
            state = STATE_MATCH;
        } else if (state == STATE_MATCH) {
            Log.v(TAG, "取消匹配");
            ivButton.setImageResource(R.drawable.selector_match);
            ivProgress.setVisibility(View.GONE);
            state = STATE_MIRROR;
        }
    }

    public void heart(View view) {
        JSONObject json = new JSONObject();
        try {
            json.put("friendid", etFriendId.getText().toString());
            json.put("data", NetUtils.getLocalIPAddress());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MsgData heart = new MsgData(NetConst.CODE_SEND_DATA, json.toString());
        TCPManager.getManager().send(heart);
    }
}
