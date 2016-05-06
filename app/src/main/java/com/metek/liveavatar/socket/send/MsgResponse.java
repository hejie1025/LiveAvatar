package com.metek.liveavatar.socket.send;

import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.NetConst;

import org.json.JSONException;
import org.json.JSONObject;

public class MsgResponse extends MsgData {

    public MsgResponse(boolean result) {
        super(NetConst.CODE_REQUEST);
        JSONObject json = new JSONObject();
        try {
            json.put("result", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        body = json.toString().getBytes();
        size = body.length;
    }
}
