package com.grumpycat.pcap.tools;

import android.content.Context;
import android.os.Looper;

/**
 * Created by cc.he on 2018/11/13
 */
public class Util {
    /**
     * dp转换成px
     */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转换成dp
     */
    public static int px2dp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * sp转换成px
     */
    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * px转换成sp
     */
    public static int px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static void assertRunInMainThread(){
        if (Looper.getMainLooper() != Looper.myLooper()){
            throw new IllegalThreadStateException("Can't Run In WorkThread");
        }
    }

    public static void assertRunInWorkThread(){
        if (Looper.getMainLooper() != Looper.myLooper()){
            throw new IllegalThreadStateException("Can't Run In WorkThread");
        }
    }
}
