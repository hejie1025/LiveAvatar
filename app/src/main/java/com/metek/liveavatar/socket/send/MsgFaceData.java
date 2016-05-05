package com.metek.liveavatar.socket.send;

import com.metek.liveavatar.live2d.FaceData;
import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.NetConst;

import org.json.JSONException;
import org.json.JSONObject;

public class MsgFaceData extends MsgData {

    public MsgFaceData(String friendId, FaceData faceData) {
        super(NetConst.CODE_SEND_FACE_DATA);
        JSONObject json = new JSONObject();
        JSONObject jsonFaceData = new JSONObject(faceData);
        try {
            json.put(KEY_FRIENDID, friendId);
            json.put("data", jsonFaceData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        body = json.toString().getBytes();
        size = body.length;
    }
}
