package com.metek.liveavatar.socket.send;

import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.NetConst;

import org.json.JSONException;
import org.json.JSONObject;

public class MsgLogin extends MsgData {

    public MsgLogin(String userid) {
        super(NetConst.CODE_LOGIN);
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_USERID, userid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        size = json.toString().getBytes().length;
        body = json.toString().getBytes();
    }
}
