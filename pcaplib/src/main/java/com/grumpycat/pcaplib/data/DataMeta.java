package com.grumpycat.pcaplib.data;

/**
 * Created by cc.he on 2018/11/15
 */
public class DataMeta{
    public static final int STATUS_IN_DISK =        1;
    public static final int STATUS_IN_MEMORY =      2;
    public static final int STATUS_READED =         3;
    public static final int STATUS_DELETED =        4;
    public static final int STATUS_ERROR =          5;

    private String timeDir;
    private int serialNum;
    private int offset;
    private int length;
    private boolean isOut;
    private byte[] data;
    private int dataStatus;

    public void setTimeDir(String timeDir) {
        this.timeDir = timeDir;
    }

    public void setSerialNum(int serialNum) {
        this.serialNum = serialNum;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setOut(boolean out) {
        isOut = out;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setDataStatus(int dataStatus) {
        this.dataStatus = dataStatus;
    }

    public String getTimeDir() {
        return timeDir;
    }

    public int getSerialNum() {
        return serialNum;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public boolean isOut() {
        return isOut;
    }

    public byte[] getData() {
        return data;
    }

    public int getDataStatus() {
        return dataStatus;
    }
}
