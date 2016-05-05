package com.metek.liveavatar.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.metek.liveavatar.R;
import com.metek.liveavatar.face.FaceOverlapFragment;
import com.metek.liveavatar.live2d.FaceData;
import com.metek.liveavatar.live2d.FileManager;
import com.metek.liveavatar.live2d.Live2dView;
import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.NetConst;
import com.metek.liveavatar.socket.TCPManager;
import com.metek.liveavatar.socket.receive.RecMsgFaceData;
import com.metek.liveavatar.socket.receive.RecMsgMatch;
import com.metek.liveavatar.socket.send.MsgFaceData;
import com.metek.liveavatar.socket.send.MsgMatch;

public class ChatActivity extends AppCompatActivity implements FaceOverlapFragment.onActionChangeListener {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final int STATE_MIRROR = 1;
    private static final int STATE_MATCH = 2;
    private int state = STATE_MIRROR;

    private ImageView ivBoard;
    private ImageView ivButton;
    private ImageView ivProgress;
    private EditText etFriendId;
    private Live2dView view;

    private String friendId;
    private boolean isChatting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        FileManager.init(this.getApplicationContext());

        FaceOverlapFragment fragment = (FaceOverlapFragment) getFragmentManager()
                .findFragmentById(R.id.face_overlap_fragment);
        fragment.setOnActionChangeListener(this);

        ivBoard = (ImageView) findViewById(R.id.board);
        ivButton = (ImageView) findViewById(R.id.button);
        ivProgress = (ImageView) findViewById(R.id.progress);
        etFriendId = (EditText) findViewById(R.id.friend_id);
        view = (Live2dView) findViewById(R.id.model);
        view.isInEditMode();

        TCPManager.getManager().setConnectListener(tcpListener);
    }

    @Override
    public void onActionChange(FaceData data) {
        if (isChatting) {
            MsgFaceData msgFaceData = new MsgFaceData(friendId, data);
            TCPManager.getManager().send(msgFaceData);
        }
//        view.setAction(data);
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
                    friendId = recMsgMatch.getFriendid();
                    isChatting = true;
                    break;
                case NetConst.CODE_SEND_FACE_DATA:
                    RecMsgFaceData recMsgFaceData = new RecMsgFaceData(data);
                    view.setAction(recMsgFaceData.getFaceData());
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
    }
}
