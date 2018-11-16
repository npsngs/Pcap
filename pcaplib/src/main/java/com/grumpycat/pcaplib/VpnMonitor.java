package com.grumpycat.pcaplib;

import android.net.VpnService;

import java.net.Socket;

/**
 * Created by cc.he on 2018/11/13
 */
public class VpnMonitor {
    private static volatile boolean vpnRunning = false;
    private static int receiveBytes;
    private static int sendBytes;
    private static String allowPackageName;
    private static int localIp;
    private static String vpnStartTime;
    private static StatusListener statusListener;
    private static VpnService vpnService;
    public static boolean isVpnRunning() {
        return vpnRunning;
    }

    public static void setVpnRunning(boolean vpnRunning) {
        VpnMonitor.vpnRunning = vpnRunning;
        if (statusListener != null){
            if (vpnRunning){
                statusListener.onVpnStart();
            }else{
                vpnService = null;
                statusListener.onVpnStop();
            }
        }
    }

    public static void initStatistic(){
        receiveBytes = 0;
        sendBytes = 0;
        vpnService = null;
    }

    public static void addReceiveBytes(int bytes){
        receiveBytes += bytes;
    }

    public static void addSendBytes(int bytes){
        sendBytes += bytes;
    }

    public static int getReceiveBytes() {
        return receiveBytes;
    }

    public static int getSendBytes() {
        return sendBytes;
    }


    public static void setAllowPackageName(String allowPackageName) {
        VpnMonitor.allowPackageName = allowPackageName;
    }

    public static String getAllowPackageName() {
        return allowPackageName;
    }

    public static int getLocalIp() {
        return localIp;
    }

    public static void setLocalIp(int localIp) {
        VpnMonitor.localIp = localIp;
    }

    public static String getVpnStartTime() {
        return vpnStartTime;
    }

    public static void setVpnStartTime(String vpnStartTime) {
        VpnMonitor.vpnStartTime = vpnStartTime;
    }

    public static void setStatusListener(StatusListener statusListener) {
        VpnMonitor.statusListener = statusListener;
    }

    public interface StatusListener{
        void onVpnStart();
        void onVpnStop();
    }

    public static void setVpnService(VpnService vpnService) {
        VpnMonitor.vpnService = vpnService;
    }

    public static boolean protect(Socket socket){
        if (vpnService != null){
            return vpnService.protect(socket);
        }
        return false;
    }
}
