package com.grumpycat.pcaplib.util;

import java.io.Closeable;
import java.io.File;

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

    public static void safeDelete(String dir){
        try {
            safeDelete(new File(dir));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void safeDelete(File file){
        try {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    safeDelete(f);
                }
                file.delete();
            } else if (file.exists()) {
                file.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
