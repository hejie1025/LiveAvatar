package com.metek.liveavatar.socket.send;

import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.NetConst;

import org.json.JSONException;

public class MsgLogin extends MsgData {

    public MsgLogin(String userid) {
        super(NetConst.CODE_LOGIN);
        try {
            mJson.put(USERID, userid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        size = mJson.toString().getBytes().length;
        body = mJson.toString().getBytes();
    }
}
