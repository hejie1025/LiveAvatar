package com.metek.liveavatar.socket.send;

import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.NetConst;

import org.json.JSONException;
import org.json.JSONObject;

public class MsgMatch extends MsgData {

    public MsgMatch() {
        super(NetConst.CODE_MATCH);
    }

    public MsgMatch(String friendid, String userid) {
        super(NetConst.CODE_MATCH);
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_FRIENDID, friendid);
            json.put(KEY_USERID, userid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        size = json.toString().getBytes().length;
        body = json.toString().getBytes();
    }
}
