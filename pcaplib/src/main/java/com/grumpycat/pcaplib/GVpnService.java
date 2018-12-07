package com.grumpycat.pcaplib;

import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import com.grumpycat.pcaplib.data.DataManager;
import com.grumpycat.pcaplib.util.CommonMethods;
import com.grumpycat.pcaplib.util.Const;

import java.util.List;


/**
 * Created by cc.he on 2018/11/13
 */
public class GVpnService extends VpnService {
    private static final String VPN_ROUTE = "0.0.0.0";
    private static final String GOOGLE_DNS_FIRST = "8.8.8.8";
    private static final String GOOGLE_DNS_SECOND = "8.8.4.4";
    private static final String AMERICA = "208.67.222.222";
    private static final String HK_DNS_SECOND = "205.252.144.228";
    private static final String CHINA_DNS_FIRST = "114.114.114.114";

    private VpnHelper vpnHelper;
    @Override
    public void onCreate() {
        super.onCreate();
        vpnHelper = new VpnHelper(this);
        VpnMonitor.setVpnService(this);
        DataManager.getInstance().launch();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (VpnController.fetchOP(intent)){
            case VpnController.OP_START:
                if (VpnMonitor.isVpnRunning()){
                    break;
                }
                vpnHelper.launchVpnThread();
                break;
            case VpnController.OP_STOP:
                if (!VpnMonitor.isVpnRunning()){
                    break;
                }
                vpnHelper.stopVpn();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }


    public ParcelFileDescriptor establishVpn(){
        Builder builder = new Builder();
        builder.setMtu(Const.MUTE_SIZE);

        VpnMonitor.setLocalIp(Const.VPN_IP);
        VpnMonitor.setVpnStartTime(System.currentTimeMillis());
        DataManager.getInstance().setCurDir(VpnMonitor.getVpnStartTimeStr());
        builder.addAddress("10.8.0.2", 32);
        builder.addRoute(VPN_ROUTE, 0);

        builder.addDnsServer(GOOGLE_DNS_FIRST);
        builder.addDnsServer(CHINA_DNS_FIRST);
        builder.addDnsServer(GOOGLE_DNS_SECOND);
        builder.addDnsServer(HK_DNS_SECOND);
        builder.addDnsServer(AMERICA);
        try {
            List<String> packages = VpnMonitor.getAllowPackages();
            if (packages != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                for(String pkg:packages){
                    builder.addAllowedApplication(pkg);
                }
                builder.addAllowedApplication(getPackageName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        builder.setSession("GrumpyPcap");
        return builder.establish();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        VpnMonitor.setVpnService(null);
    }
}
