package com.grumpycat.pcaplib.util;

import java.io.Closeable;

/**
 * Created by cc.he on 2018/11/13
 */
public class IOUtils {
    public static void safeClose(Closeable closeable){
        if (closeable == null)return;
        try {
            closeable.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
