package com.grumpycat.pcaplib;

import android.content.Context;
import android.net.VpnService;

import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

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
    private static Context context;
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
        if(vpnService != null){
            context = vpnService.getApplicationContext();
        }else{
            context = null;
        }
    }

    public static boolean protect(Socket socket){
        if (vpnService != null){
            return vpnService.protect(socket);
        }
        return false;
    }

    public static Context getContext() {
        return context;
    }
    private static SSLContext sslContext;
    private static SSLContext createSslContext() throws Exception {
        char[] passArray = "123456".toCharArray();
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        String type = KeyStore.getDefaultType();
        KeyStore ks = KeyStore.getInstance(type);
        //加载keytool 生成的文件
        InputStream is = context.getResources().getAssets().open("demo.ks");
        ks.load(is, passArray);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, passArray);
        sslContext.init(kmf.getKeyManagers(), null, null);
        is.close();
        return sslContext;
    }


    public static SSLEngine createSslEngine(boolean isClientMode) throws Exception{
        if(sslContext == null && context != null){
            sslContext = createSslContext();
        }
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(isClientMode);
        return sslEngine;
    }
}
