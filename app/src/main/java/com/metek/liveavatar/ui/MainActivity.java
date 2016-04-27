package com.metek.liveavatar.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.metek.liveavatar.R;
import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.NetConst;
import com.metek.liveavatar.socket.TCPManager;
import com.metek.liveavatar.socket.send.MsgLogin;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private EditText etUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etUserId = (EditText) findViewById(R.id.user_id);
    }

    TCPManager.ConnectListener tcpListener = new TCPManager.ConnectListener() {
        @Override
        public void onConnect(int state, MsgData data) {
            if (state != CONN_OK) return;
            if (data == null) return;

            switch (data.code) {
                case NetConst.CODE_LOGIN:
                    Log.v(TAG, "TCP连接服务器成功");
                    MsgLogin msgLogin = new MsgLogin(etUserId.getText().toString());
                    TCPManager.getManager().send(msgLogin);
                    startActivity(new Intent(MainActivity.this, ChatActivity.class));
                    break;
            }
        }
    };

    public void connect(View view) {
        TCPManager.getManager().connect();
        TCPManager.getManager().setConnectListener(tcpListener);
    }
}
