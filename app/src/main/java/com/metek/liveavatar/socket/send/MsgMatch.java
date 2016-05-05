package com.metek.liveavatar.socket.send;

import com.metek.liveavatar.socket.MsgData;
import com.metek.liveavatar.socket.NetConst;

public class MsgMatch extends MsgData {

    public MsgMatch() {
        super(NetConst.CODE_MATCH);
    }
}
