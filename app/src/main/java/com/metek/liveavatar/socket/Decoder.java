package com.metek.liveavatar.socket;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class Decoder extends CumulativeProtocolDecoder {
    private final Charset charset;

    public Decoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    protected boolean doDecode(IoSession ioSession, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        in.order(ByteOrder.LITTLE_ENDIAN);
        if (in.remaining() < MsgData.MSG_HEAD_SIZE) {
            return false;
        }
        in.mark();
        int cmdCode = in.getInt(0);
        int bodySize = in.getInt(4);
        if (bodySize > in.remaining() - MsgData.MSG_HEAD_SIZE) {
            in.reset();
            return false;
        } else {
            in.getInt();
            in.getInt();
            byte[] body = new byte[bodySize];
            in.get(body);
            MsgData data = new MsgData(cmdCode, bodySize, body);
            out.write(data);
            return in.remaining() <= 0;
        }
    }
}
