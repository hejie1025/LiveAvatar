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
        String json = new String(data.body);
        JSONTokener jsonParser = new JSONTokener(json);
        try {
            mJson = (JSONObject) jsonParser.nextValue();
            friendid = mJson.getString(FRIENDID);
            userid = mJson.getString(USERID);
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
