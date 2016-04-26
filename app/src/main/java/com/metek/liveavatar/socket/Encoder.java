package com.metek.liveavatar.socket;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class Encoder implements ProtocolEncoder {
    private final Charset charset;

    public Encoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void encode(IoSession ioSession, Object object, ProtocolEncoderOutput out) throws Exception {
        CharsetEncoder ce = charset.newEncoder();
        MsgData data = (MsgData) object;
        int bodySize = data.getBody().length;
        IoBuffer buffer = IoBuffer.allocate(MsgData.MSG_HEAD_SIZE + bodySize).setAutoExpand(true);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(0, data.getHead().cmdCode);
        buffer.putInt(4, data.getHead().bodySize);
        for (int i = 0; i < bodySize; i++) {
            buffer.put(i + MsgData.MSG_HEAD_SIZE, data.getBody()[i]);
        }
        out.write(buffer);
    }

    @Override
    public void dispose(IoSession ioSession) throws Exception {

    }
}
