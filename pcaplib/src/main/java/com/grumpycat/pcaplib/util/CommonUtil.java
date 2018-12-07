package com.grumpycat.pcaplib.util;

/**
 * Created by cc.he on 2018/12/7
 */
public class CommonUtil {
    public static byte[] copyByte(byte[] src, int offset, int size){
        byte[] dst = new byte[size];
        System.arraycopy(src, offset, dst, 0, size);
        return dst;
    }
}
