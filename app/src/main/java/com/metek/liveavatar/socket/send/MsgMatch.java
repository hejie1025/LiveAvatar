package com.metek.liveavatar.socket.send;

import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.NetConst;

import org.json.JSONException;

public class MsgMatch extends MsgData {

    public MsgMatch() {
        super(NetConst.CODE_MATCH);
    }

    public MsgMatch(String friendid, String userid) {
        super(NetConst.CODE_MATCH);
        try {
            mJson.put(FRIENDID, friendid);
            mJson.put(USERID, userid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        size = mJson.toString().getBytes().length;
        body = mJson.toString().getBytes();
    }
}
