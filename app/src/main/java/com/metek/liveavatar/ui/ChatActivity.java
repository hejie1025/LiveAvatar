package com.metek.liveavatar.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.metek.liveavatar.R;
import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.NetConst;
import com.metek.liveavatar.socket.NetUtils;
import com.metek.liveavatar.socket.TCPManager;
import com.metek.liveavatar.socket.UDPClient;
import com.metek.liveavatar.socket.Utilities;
import com.metek.liveavatar.socket.receive.RecMsgMatch;
import com.metek.liveavatar.socket.receive.RecMsgRemote;
import com.metek.liveavatar.socket.send.MsgMatch;

import java.net.InetSocketAddress;

public class ChatActivity extends AppCompatActivity implements Utilities.ActivityInterface {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int STATE_MIRROR = 1;
    private static final int STATE_MATCH = 2;
    private int state = STATE_MIRROR;

    private ImageView ivButton;
    private ImageView ivProgress;
    private EditText etFriendId;
    private EditText etPort;

    private UDPClient udpClient;
    private RecMsgRemote recMsgRemote = null;

    private final BroadcastReceiver handleMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            MsgData data = (MsgData) intent.getExtras().getSerializable(EXTRA_MESSAGE);

            if (data.code == NetConst.CODE_REC_ADDR) {
                Log.v(TAG, "获取远程对象地址成功");
                recMsgRemote = new RecMsgRemote(data);

                Log.v(TAG, "发送心跳包");
                MsgData heart = new MsgData(NetConst.CODE_HEART, NetUtils.getLocalIPAddress());
                udpClient.doSend(new InetSocketAddress(recMsgRemote.getFIP(), recMsgRemote.getFPort()), heart);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        registerReceiver(handleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));

        ivButton = (ImageView) findViewById(R.id.button);
        ivProgress = (ImageView) findViewById(R.id.progress);
        etFriendId = (EditText) findViewById(R.id.friend_id);
        etPort = (EditText) findViewById(R.id.port);

        TCPManager.getManager().setConnectListener(tcpListener);
        udpClient = new UDPClient(this);
    }

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

    TCPManager.ConnectListener tcpListener = new TCPManager.ConnectListener() {
        @Override
        public void onConnect(int state, MsgData data) {
            if (state != CONN_OK) return;
            if (data == null) return;

            switch (data.code) {
                case NetConst.CODE_MATCH:
                    Log.v(TAG, "匹配成功");
                    RecMsgMatch recMsgMatch = new RecMsgMatch(data);

                    Log.v(TAG, "获取远程对象地址");
                    udpClient.start(NetConst.Host, NetConst.UdpPort, Integer.parseInt(etPort.getText().toString()));
                    MsgMatch msgMatch = new MsgMatch(etFriendId.getText().toString(), recMsgMatch.getUserid());
                    udpClient.doSend(msgMatch);
                    break;
            }
        }
    };

    public void heart(View view) {
        Log.v(TAG, "发送心跳包");
        MsgData heart = new MsgData(NetConst.CODE_HEART, NetUtils.getLocalIPAddress());
        udpClient.doSend(new InetSocketAddress(recMsgRemote.getFIP(), recMsgRemote.getFPort()), heart);
    }
}
