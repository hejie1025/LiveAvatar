package com.metek.liveavatar.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.metek.liveavatar.R;
import com.metek.liveavatar.socket.NetConst;
import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.TCPManager;
import com.metek.liveavatar.socket.UDPManager;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int STATE_MIRROR = 1;
    private static final int STATE_MATCH = 2;
    private int state = STATE_MIRROR;

    private ImageView ivButton;
    private ImageView ivProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivButton = (ImageView) findViewById(R.id.button);
        ivProgress = (ImageView) findViewById(R.id.progress);

        connectTCP();
    }

    private void connectTCP() {
        TCPManager.getManager().connect();
        TCPManager.getManager().setConnectListener(new TCPManager.ConnectListener() {
            @Override
            public void onConnect(int state, MsgData data) {
                if (state == TCPManager.ConnectListener.CONN_OK) {
                    if (data == null) return;

                    if (data.code == NetConst.CODE_LOGIN) {
                        Log.v(TAG, "登陆成功");
                        MsgData sendData = new MsgData(NetConst.CODE_LOGIN, "{\"userid\":\"Cthulhu\"}");
                        Log.i(TAG, sendData.toLogString() + " " + sendData.toLogHex());
                        TCPManager.getManager().send(sendData);
                    } else if (data.code == NetConst.CODE_MATCH) {
                        try {
                            JSONObject json = new JSONObject(new String(data.body));
                            Log.v(TAG, "匹配成功");
                            connectUDP(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void connectUDP(final MsgData data) {
        UDPManager.getManager().connect();
        UDPManager.getManager().setConnectListener(new UDPManager.ConnectListener() {

            @Override
            public void onConnect(int state, MsgData data) {
                if (state == TCPManager.ConnectListener.CONN_OK) {
                    if (data == null) {
                        return;
                    }
                    Log.i(TAG, "连接成功");
                }
            }
        });
        UDPManager.getManager().send(data);
    }

    public void match(View view) {
        if (state == STATE_MIRROR) {
            MsgData sendData = new MsgData(NetConst.CODE_MATCH);
            TCPManager.getManager().send(sendData);
            ivButton.setImageResource(R.drawable.selector_cancel);
            ivProgress.setVisibility(View.VISIBLE);
            state = STATE_MATCH;
        } else if (state == STATE_MATCH) {
            ivButton.setImageResource(R.drawable.selector_match);
            ivProgress.setVisibility(View.GONE);
            state = STATE_MIRROR;
        }
    }
}
