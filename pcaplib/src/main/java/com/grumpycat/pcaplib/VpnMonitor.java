package com.grumpycat.pcaplib;

import android.content.Context;
import android.net.VpnService;

import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcaplib.util.StrUtil;

import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.util.List;

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
    private static List<String> allowPackages;
    private static int localIp;
    private static long vpnStartTime;
    private static String vpnStartTimeStr;
    private static StatusListener statusListener;
    private static VpnService vpnService;
    private static Context context;
    private static int tcpProxyPort;
    private static boolean isSingleApp;
    private static int singleAppUid;
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


    public static List<String> getAllowPackages() {
        return allowPackages;
    }

    public static int getSingleAppUid() {
        return singleAppUid;
    }

    public static void setAllowUids(int[] uids) {
        VpnMonitor.allowPackages = AppManager.queryPackages(uids);
        if(allowPackages != null && allowPackages.size() == 1){
            singleAppUid = uids[0];
            isSingleApp = true;
        }else{
            isSingleApp = false;
        }
    }

    public static int getLocalIp() {
        return localIp;
    }

    public static void setLocalIp(int localIp) {
        VpnMonitor.localIp = localIp;
    }

    public static boolean isSingleApp() {
        return isSingleApp;
    }

    public static String getVpnStartTimeStr() {
        return vpnStartTimeStr;
    }

    public static long getVpnStartTime() {
        return vpnStartTime;
    }
    public static void setVpnStartTime(long vpnStartTime) {
        VpnMonitor.vpnStartTime = vpnStartTime;
        vpnStartTimeStr = StrUtil.formatYYMMDDHHMMSS(vpnStartTime);
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
        String pwd = "123456";
        String file = "server.bks";
        char[] passArray = pwd.toCharArray();
        SSLContext sslContext = SSLContext.getInstance("TLS");
        String type = KeyStore.getDefaultType();
        KeyStore ks = KeyStore.getInstance(type);
        //加载keytool 生成的文件
        InputStream is = context.getResources().getAssets().open(file);
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

    public static void setTcpProxyPort(int tcpProxyPort) {
        VpnMonitor.tcpProxyPort = tcpProxyPort;
    }

    public static int getTcpProxyPort() {
        return tcpProxyPort;
    }
}
