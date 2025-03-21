package com.slg.module.message;

import java.nio.ByteBuffer;

public class ByteBufferMessage {
    private long userId;
    private int cid;
    private int errorCode;
    private int protocolId;
    private ByteBuffer body;

    public ByteBufferMessage() {

    }

    public ByteBufferMessage(long userId, int cid, int errorCode, int protocolId, ByteBuffer body) {
        this.userId = userId;
        this.cid = cid;
        this.errorCode = errorCode;
        this.protocolId = protocolId;
        this.body = body;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public ByteBuffer getBody() {
        return body;
    }

    public void setBody(ByteBuffer body) {
        this.body = body;
    }
}
