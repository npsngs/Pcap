package com.grumpycat.pcaplib.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cc.he on 2018/11/14
 */
public class StrUtil {
    public static String ip2Str(int ip) {
        return String.format("%s.%s.%s.%s",
                (ip >> 24) & 0x00FF,
                (ip >> 16) & 0x00FF,
                (ip >> 8) & 0x00FF,
                (ip & 0x00FF));
    }

    public static String ip2Str(byte[] ip) {
        return String.format("%s.%s.%s.%s",
                ip[0] & 0x00FF,
                ip[1] & 0x00FF,
                ip[2] & 0x00FF,
                ip[3] & 0x00FF);
    }

    public static int str2Ip(String ip) {
        String[] str = ip.split("\\.");
        return (Integer.parseInt(str[0]) << 24)
                | (Integer.parseInt(str[1]) << 16)
                | (Integer.parseInt(str[2]) << 8)
                | (Integer.parseInt(str[3]));
    }

    public static String convertHexStr(byte[] data, int offset, int len){
        StringBuilder sb = new StringBuilder();
        int index = offset;
        while (index < len){
            sb.append(HexStr.byte2Hex(data[index]));
            index++;
            if(index%4 == 0){
                sb.append("\t");
            }
            if(index%16 == 0){
                sb.append("\n");
            }
        }
        return sb.toString();
    }


    private static DateFormat HHMMSS = new SimpleDateFormat("HH:mm:ss", Const.LOCALE);
    private static DateFormat YYMMDD_HHMMSS = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss",  Const.LOCALE);
    private static DateFormat YYMMDDHHMMSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",  Const.LOCALE);
    public static String formatHHMMSS(long time) {
        Date date = new Date(time);
        return HHMMSS.format(date);
    }
    public static String formatYYMMDD_HHMMSS(long time) {
        Date date = new Date(time);
        return YYMMDD_HHMMSS.format(date);
    }

    public static String formatYYMMDDHHMMSS(long time) {
        Date date = new Date(time);
        return YYMMDDHHMMSS.format(date);
    }
}
