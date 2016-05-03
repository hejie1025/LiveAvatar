package com.metek.liveavatar.socket.receive;

import com.metek.liveavatar.socket.MsgData;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RecMsgRemote extends MsgData {
    private String fIP;
    private int fport;
    private String IP;
    private int port;

    public RecMsgRemote(MsgData data) {
        super(data.code, data.size, data.body);
        String jsonBody = new String(data.body);
        JSONTokener jsonParser = new JSONTokener(jsonBody);
        try {
            JSONObject json = (JSONObject) jsonParser.nextValue();
            fIP = json.getString(KEY_FIP);
            fport = Integer.parseInt(json.getString(KEY_FPORT));
            IP = json.getString(KEY_IP);
            port = Integer.parseInt(json.getString(KEY_PORT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getFIP() {
        return fIP;
    }

    public int getFPort() {
        return fport;
    }

    public String getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }
}
