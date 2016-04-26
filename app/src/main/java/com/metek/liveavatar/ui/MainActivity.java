package com.metek.liveavatar.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.metek.liveavatar.R;
import com.metek.liveavatar.socket.Constant;
import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.TCPManager;
import com.metek.liveavatar.socket.UDPManager;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectTCP();
    }

    private void connectTCP() {
        TCPManager.getManager().connect();
        TCPManager.getManager().setConnectListener(new TCPManager.ConnectListener() {
            @Override
            public void onConnect(int state, MsgData data) {
                if (state == TCPManager.ConnectListener.CONN_OK) {
                    if (data == null) return;
                    int code = data.getHead().cmdCode;

                    if (code == Constant.CMD_LOGIN) {
                        Log.v(TAG, "登陆成功");
                        MsgData sendData = new MsgData(Constant.CMD_LOGIN, "{\"userid\":\"Cthulhu\"}");
                        TCPManager.getManager().send(sendData);
                    } else if (code == Constant.CMD_AGREE) {
                        try {
                            JSONObject json = new JSONObject(new String(data.getBody()));
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
                    if (data == null) return;
                }
            }
        });
        UDPManager.getManager().send(data);
    }

    public void match(View view) {
        MsgData sendData = new MsgData(Constant.CMD_AGREE, "");
        TCPManager.getManager().send(sendData);
    }
}
