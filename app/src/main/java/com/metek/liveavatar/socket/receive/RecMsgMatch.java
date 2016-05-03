package com.metek.liveavatar.socket.receive;

import com.metek.liveavatar.socket.MsgData;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RecMsgMatch extends MsgData {
    private String friendid;
    private String userid;

    public RecMsgMatch(MsgData data) {
        super(data.code, data.size, data.body);
        String jsonBody = new String(data.body);
        JSONTokener jsonParser = new JSONTokener(jsonBody);
        try {
            JSONObject json = (JSONObject) jsonParser.nextValue();
            friendid = json.getString(KEY_FRIENDID);
            userid = json.getString(KEY_USERID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getFriendid() {
        return friendid;
    }

    public String getUserid() {
        return userid;
    }
}
