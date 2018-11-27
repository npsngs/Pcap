package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcaplib.appinfo.AppInfo;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcap.tools.Config;
import com.grumpycat.pcap.ui.base.UiWidget;
import com.grumpycat.pcaplib.VpnMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cc.he on 2018/11/27
 */
public class AppInfoBar extends UiWidget{
    private ImageView iv_icon;
    private TextView tv_app_name;
    private TextView tv_info;

    public AppInfoBar(Activity activity) {
        super(activity);
        iv_icon = findViewById(R.id.iv_icon);
        tv_app_name = findViewById(R.id.tv_app_name);
        tv_info = findViewById(R.id.tv_info);
    }

    public void setAppUid(int[] uids){
        Config.saveSelectApps(uids);
        if(uids == null || uids.length == 0){
            iv_icon.setBackgroundColor(0xff556677);
            tv_app_name.setText("All App");
            VpnMonitor.setAllowPackages(null);
        }else{

            if(uids.length == 1){
                AppInfo appInfo = AppManager.getApp(uids[0]);
                iv_icon.setImageDrawable(appInfo.icon);
                tv_app_name.setText(appInfo.name);
            }else{
                iv_icon.setBackgroundColor(0xff556677);
                tv_app_name.setText("Multi App");
            }

            List<String> allowApps = new ArrayList<>(uids.length);
            for(int uid:uids){
                AppInfo appInfo = AppManager.getApp(uid);
                allowApps.add(appInfo.pkgName);
            }
            VpnMonitor.setAllowPackages(allowApps);
        }
    }
}
