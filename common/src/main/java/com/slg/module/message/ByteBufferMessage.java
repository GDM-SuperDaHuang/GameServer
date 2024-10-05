package com.slg.module.message;

import java.nio.ByteBuffer;

public class ByteBufferMessage {
    private long sessiomId;
    private int protocolId;
    private ByteBuffer byteBuffer;

    public ByteBufferMessage() {
    }

    public long getSessiomId() {
        return sessiomId;
    }

    public void setSessiomId(long sessiomId) {
        this.sessiomId = sessiomId;
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public ByteBufferMessage(long sessiomId, int protocolId, ByteBuffer byteBuffer) {
        this.sessiomId = sessiomId;
        this.protocolId = protocolId;
        this.byteBuffer = byteBuffer;
    }
}
