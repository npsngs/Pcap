package com.grumpycat.pcaplib.data;

/**
 * Created by cc.he on 2018/11/15
 */
public class DataBuffer {
    public boolean isIdle;
    public int serialNum;
    public int limit;
    public byte[] data;
    public int size;
    public DataBuffer next;
    public DataBuffer pre;
    public DataBuffer(int limit) {
        this.limit = limit;
        size = 0;
        isIdle = true;
        data = new byte[limit];
    }

    public int leftSpace(){
        return limit - size;
    }

    public boolean putData(byte[] inputData, int offset, int len){
        if(leftSpace() < len){
            return false;
        }
        System.arraycopy(inputData, offset, this.data, size, len);
        size += len;
        return true;
    }
    public void reset(){
        isIdle = true;
        serialNum = -1;
        size = 0;
    }
}
