package com.metek.liveavatar.socket;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.nio.ByteOrder;

public class CodecFactory implements ProtocolCodecFactory {
    private final Encoder encoder;
    private final Decoder decoder;

    public CodecFactory() {
        this.encoder = new Encoder();
        this.decoder = new Decoder();
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }


    /**
     * 编码器：数据转换成Socket消息
     */
    public class Encoder implements ProtocolEncoder {

        @Override
        public void encode(IoSession ioSession, Object object, ProtocolEncoderOutput out) throws Exception {
            MsgData data = (MsgData) object;
            IoBuffer buffer = IoBuffer.allocate(MsgData.HEAD_SIZE + data.size).setAutoExpand(true);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(0, data.code);
            buffer.putInt(4, data.size);
            for (int i = 0; i < data.size; i++) {
                buffer.put(i + MsgData.HEAD_SIZE, data.body[i]);
            }
            out.write(buffer);
        }

        @Override
        public void dispose(IoSession ioSession) throws Exception {
        }
    }

    /**
     * 解码器：Socket消息转数据类型
     */
    public class Decoder extends CumulativeProtocolDecoder {

        @Override
        protected boolean doDecode(IoSession ioSession, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
            in.order(ByteOrder.LITTLE_ENDIAN);
            if (in.remaining() < MsgData.HEAD_SIZE) {
                return false;
            }
            in.mark();
            int cmdCode = in.getInt(0);
            int bodySize = in.getInt(4);
            if (bodySize > in.remaining() - MsgData.HEAD_SIZE) {
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
}
