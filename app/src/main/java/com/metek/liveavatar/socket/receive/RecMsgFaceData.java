package com.metek.liveavatar.socket.receive;

import com.metek.liveavatar.live2d.FaceData;
import com.metek.liveavatar.socket.MsgData;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RecMsgFaceData extends MsgData {
    private String friendId;
    private FaceData faceData;

    public RecMsgFaceData(MsgData data) {
        super(data.code, data.size, data.body);
        String jsonBody = new String(data.body);
        JSONTokener jsonParser = new JSONTokener(jsonBody);
        try {
            JSONObject json = (JSONObject) jsonParser.nextValue();
            friendId = json.getString(KEY_FRIENDID);
            JSONObject jsonFaceData = json.getJSONObject("data");
            faceData = new FaceData(jsonFaceData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getFriendId() {
        return friendId;
    }

    public FaceData getFaceData() {
        return faceData;
    }
}
