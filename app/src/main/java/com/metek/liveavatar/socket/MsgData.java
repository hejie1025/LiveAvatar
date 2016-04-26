package com.metek.liveavatar.socket;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MsgData implements Serializable {
    public static final int HEAD_SIZE = 8;
    public int code;
    public int size;
    public byte[] body;

    public MsgData(int code) {
        this(code, "");
    }

    public MsgData(int code, String message) {
        this(code, message.getBytes().length, message.getBytes());
    }

    public MsgData(int code, int size, byte[] body) {
        this.code = code;
        this.size = size;
        this.body = body;
    }

    /**
     * 转为byte数组格式
     * @return 返回byte数组格式
     */
    public byte[] toByte() {
        ByteBuffer buffer = ByteBuffer.allocate(HEAD_SIZE + size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(0, code);
        buffer.putInt(4, size);
        for (int i = 0; i < size; i++) {
            buffer.put(i + MsgData.HEAD_SIZE, body[i]);
        }
        return buffer.array();
    }

    /**
     * 转为Log信息
     * @return 返回(code, size, body)格式的Log信息
     */
    public String toLogString() {
        return "(" + code + ", " + size + ", " + new String(body) +")";
    }

    /**
     * 转为16进制Log信息
     * @return 返回16进制Log信息
     */
    public String toLogHex() {
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
}
