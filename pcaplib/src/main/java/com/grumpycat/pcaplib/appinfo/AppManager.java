package com.grumpycat.pcaplib.appinfo;


import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by cc.he on 2018/11/13
 */
public class AppManager {
    private static SparseArray<AppInfo> apps = new SparseArray<>();

    private static volatile boolean isFinishLoad = false;
    public static boolean isFinishLoad(){
        return isFinishLoad;
    }

    public static void asyncLoadAppInfo(Context context, OnLoadFinishListener finishListener){
        setFinishListener(finishListener);
        new Thread(){
            @Override
            public void run() {
                try {
                    loadAppInfo(context.getApplicationContext());
                }catch (Exception e){
                    e.printStackTrace();
                    isFinishLoad = true;
                }
            }
        }.start();
    }
    private static void loadAppInfo(Context context) {
        isFinishLoad = false;
        apps.clear();

        PackageManager pm = context.getPackageManager();
        List<PackageInfo> infoList = pm.getInstalledPackages(0);
        for (PackageInfo p:infoList) {
            AppInfo app = new AppInfo();
            String pkgName = p.applicationInfo.packageName;
            if(PackageManager.PERMISSION_GRANTED !=
                    pm.checkPermission(Manifest.permission.INTERNET, pkgName)){
                app.hasPermission = false;
            }else {
                app.hasPermission = true;
            }

            app.icon = p.applicationInfo.loadIcon(pm);
            app.name = pm.getApplicationLabel(p.applicationInfo).toString();
            app.pkgName = pkgName;
            app.uid = p.applicationInfo.uid;

            app.isSystem = (p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            apps.put(app.uid, app);
        }
        isFinishLoad = true;
        if (finishListener != null){
            finishListener.onFinish();
        }
    }

    public static List<AppInfo> getNetApps(){
        if(isFinishLoad){
            int len = apps.size();
            if(len > 0){
                List<AppInfo> app = new ArrayList<>();

                for(int i=0;i<len;i++){
                    AppInfo item = apps.valueAt(i);
                    if(item.hasPermission){
                        app.add(item);
                    }
                }

                Collections.sort(app , (o1, o2) -> {
                    if (o1.isSystem && !o2.isSystem){
                        return 1;
                    }else if(!o1.isSystem && o2.isSystem){
                        return -1;
                    }
                    return o1.name.compareTo(o2.name);
                });

                return app;
            }
        }
        return null;
    }

    public static List<String> queryPackages(int[] uids){
        if(!isFinishLoad || uids == null || uids.length == 0){
            return null;
        }

        List<String> ret = new ArrayList<>(uids.length);
        for(int uid:uids){
            AppInfo appInfo = apps.get(uid);
            ret.add(appInfo.pkgName);
        }
        return ret;
    }

    public static AppInfo getApp(int uid){
        if (isFinishLoad) {
            return apps.get(uid);
        }
        return null;
    }


    private static volatile OnLoadFinishListener finishListener;
    public static void setFinishListener(OnLoadFinishListener finishListener) {
        AppManager.finishListener = finishListener;
    }
}
