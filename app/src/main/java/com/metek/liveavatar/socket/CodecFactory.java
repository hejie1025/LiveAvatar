package com.metek.liveavatar.socket;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import java.nio.charset.Charset;

public class CodecFactory implements ProtocolCodecFactory{
    private final Encoder encoder;
    private final Decoder decoder;

    public CodecFactory() {
        this.encoder = new Encoder(Charset.defaultCharset());
        this.decoder = new Decoder(Charset.defaultCharset());
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }
}
