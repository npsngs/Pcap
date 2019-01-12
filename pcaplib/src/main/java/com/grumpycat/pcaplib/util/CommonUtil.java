package com.grumpycat.pcaplib.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by cc.he on 2018/12/7
 */
public class CommonUtil {
    public static byte[] copyByte(byte[] src, int offset, int size){
        byte[] dst = new byte[size];
        System.arraycopy(src, offset, dst, 0, size);
        return dst;
    }

    public static final int REQUEST_EXTERNAL_STORAGE = 1899;
    public static boolean checkPermission(Context context, String permission){
        try {
            //检测是否有写的权限
            int ret = ActivityCompat.checkSelfPermission(context,permission);
            if (ret != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                if(context instanceof Activity){
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[]{permission},REQUEST_EXTERNAL_STORAGE);

                }
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
