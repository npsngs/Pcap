package com.grumpycat.pcap.tools;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by cc.he on 2018/12/3
 */
public class AppConfigs {
    private static SharedPreferences sp;
    public static void init(Context context){
        sp = context.getSharedPreferences("APP_CONFIGS", Context.MODE_PRIVATE);
    }


    public static boolean isShowFloating(){
       return sp.getBoolean("is_show_floating", false);
    }

    public static void setShowFloating(boolean showFloating){
        sp.edit().putBoolean("is_show_floating", showFloating).apply();
    }

    public static boolean isFilterUdp(){
        return sp.getBoolean("is_filter_udp", false);
    }

    public static void setFilterUdp(boolean isFilterUdp){
        sp.edit().putBoolean("is_filter_udp", isFilterUdp).apply();
    }

    public static boolean isCrackTls(){
        return sp.getBoolean("is_crack_tls", true);
    }

    public static void setCrackTls(boolean isCrackTls){
        sp.edit().putBoolean("is_crack_tls", isCrackTls).apply();
    }

    public static void setFloatBtnLocation(int y, boolean isLeftSide){
        sp.edit()
                .putBoolean("isLeftSide", isLeftSide)
                .putInt("float_btn_y", y)
                .apply();
    }

    public static int getFloatBtnY(int defValue){
        return sp.getInt("float_btn_y", defValue);
    }

    public static boolean isFloatBtnLeftSide(){
        return sp.getBoolean("isLeftSide", true);
    }

    public static void setFloatPageLocation(int x, int y){
        sp.edit()
                .putInt("float_page_x", x)
                .putInt("float_page_y", y)
                .apply();
    }

    public static int getFloatPageX(int defValue){
        return sp.getInt("float_page_x", defValue);
    }
    public static int getFloatPageY(int defValue){
        return sp.getInt("float_page_y", defValue);
    }


    public static void setFloatPageSize(int x, int y){
        sp.edit()
                .putInt("float_page_sx", x)
                .putInt("float_page_sy", y)
                .apply();
    }

    public static int getFloatPageWidth(int defValue){
        return sp.getInt("float_page_sx", defValue);
    }
    public static int getFloatPageHeight(int defValue){
        return sp.getInt("float_page_sy", defValue);
    }

}
