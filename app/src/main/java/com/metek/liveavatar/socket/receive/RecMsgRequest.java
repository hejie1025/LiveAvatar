package com.metek.liveavatar.socket.receive;

import com.metek.liveavatar.socket.MsgData;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RecMsgRequest extends MsgData {
    private String friendName;

    public RecMsgRequest(MsgData data) {
        super(data.code, data.size, data.body);
        String jsonBody = new String(data.body);
        JSONTokener jsonParser = new JSONTokener(jsonBody);
        try {
            JSONObject json = (JSONObject) jsonParser.nextValue();
            friendName = json.getString("username");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getFriendName() {
        return friendName;
    }
}
