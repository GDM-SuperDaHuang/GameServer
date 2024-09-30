package com.slg.module.message;

public class PbMessage {
    private long sessiomId;
    private int protocolId;
    private byte[] data;

    public PbMessage() {
    }

    public PbMessage(long sessiomId, int protocolId, byte[] data) {
        this.sessiomId = sessiomId;
        this.protocolId = protocolId;
        this.data = data;
    }

    public long getSessiomId() {
        return sessiomId;
    }

    public void setSessiomId(long sessiomId) {
        this.sessiomId = sessiomId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }
}
