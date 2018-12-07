package com.grumpycat.pcaplib.data;

/**
 * Created by cc.he on 2018/12/7
 */
public class ParseMeta {
    private boolean isSend;
    private int ipOffset;
    private int ipLength;
    private int totalLen;
    private int protocol;
    private int dataOffset;
    private int dataLength;
    private byte[] data;/* max 2K */
    public int getIpOffset() {
        return ipOffset;
    }

    public void setIpOffset(int ipOffset) {
        this.ipOffset = ipOffset;
    }

    public int getIpLength() {
        return ipLength;
    }

    public void setIpLength(int ipLength) {
        this.ipLength = ipLength;
    }

    public int getTotalLen() {
        return totalLen;
    }

    public void setTotalLen(int totalLen) {
        this.totalLen = totalLen;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getDataOffset() {
        return dataOffset;
    }

    public void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
