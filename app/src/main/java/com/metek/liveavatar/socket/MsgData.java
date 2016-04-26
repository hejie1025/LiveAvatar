package com.metek.liveavatar.socket;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MsgData implements Serializable {
    public static final int MSG_HEAD_SIZE = 8;
    private MsgHead head;
    private byte[] body;

    public MsgData(int cmdCode, String body) {
        this(cmdCode, body.getBytes().length, body.getBytes());
    }

    public MsgData(int cmdCode, int bodySize, byte[] body) {
        this.body = body;
        this.head = new MsgHead(cmdCode, bodySize);
    }

    public MsgHead getHead() {
        return head;
    }

    public void setHead(MsgHead head) {
        this.head = head;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] toByte() {
        ByteBuffer buffer = ByteBuffer.allocate(MSG_HEAD_SIZE + body.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(0, head.cmdCode);
        buffer.putInt(4, head.bodySize);
        buffer.put(body, 0, body.length);
        return buffer.array();
    }

    public String toLogString() {
        return "(" + this.getHead().cmdCode
                + ", " + this.getHead().bodySize
                + ", " + new String(this.getBody()) +")";
    }

    public String toHex() {
        byte[] bytes = this.toByte();
        String result =  "";
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result += hex.toUpperCase() + " ";
        }
        return result;
    }

    public static class MsgHead implements Serializable {
        public int cmdCode;
        public int bodySize;

        public MsgHead(int cmdCode, int bodySize) {
            this.cmdCode = cmdCode;
            this.bodySize = bodySize;
        }
    }
}
