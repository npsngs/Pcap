package com.grumpycat.pcaplib.appinfo;


import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.SparseArray;

import com.grumpycat.pcaplib.util.ThreadPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cc.he on 2018/11/13
 */
public class AppManager {
    private static SparseArray<AppInfo> uidCache;
    private static Map<String, AppInfo> pkgCache;
    private static PackageManager pm;
    public static void init(Context context){
        synchronized (AppManager.class) {
            if (pm == null) {
                pm = context.getPackageManager();
                uidCache = new SparseArray<>();
                pkgCache = new HashMap<>();
            }
        }
    }

    public static void asyncLoad(int uid, LoadOneCallback cb){
        AppInfo ret = uidCache.get(uid);
        if(ret != null && cb != null){
            cb.onLoadFinished(ret);
        }

        ThreadPool.runUIWorker(() -> {
            AppInfo appInfo = load(uid);
            if(cb != null){
                ThreadPool.runOnUiThread(()->cb.onLoadFinished(appInfo));
            }
        });
    }

    public static void asyncLoad(String packageName, LoadOneCallback cb){
        AppInfo ret = pkgCache.get(packageName);
        if(ret != null && cb != null){
            cb.onLoadFinished(ret);
        }
        ThreadPool.runUIWorker(() -> {
            AppInfo appInfo = load(packageName);
            if(cb != null){
                ThreadPool.runOnUiThread(()->cb.onLoadFinished(appInfo));
            }
        });
    }

    public static void asyncLoadAll(LoadAllCallback cb){
        ThreadPool.runUIWorker(() -> {
            List<AppInfo> appInfos = load();
            if(cb != null){
                ThreadPool.runOnUiThread(()->cb.onLoadFinished(appInfos));
            }
        });
    }

    public static void asyncLoad(int[] uids, LoadAllCallback cb){
        ThreadPool.runUIWorker(() -> {
            List<AppInfo> appInfos = load(uids);
            if(cb != null){
                ThreadPool.runOnUiThread(()->cb.onLoadFinished(appInfos));
            }
        });
    }

    public interface LoadOneCallback{
        void onLoadFinished(AppInfo appInfo);
    }

    public interface LoadAllCallback{
        void onLoadFinished(List<AppInfo> apps);
    }

    private static AppInfo load(int uid){
        String[] pkgs = pm.getPackagesForUid(uid);
        if(pkgs != null && pkgs.length > 0){
            return load(pkgs[0]);
        }
        return null;
    }

    private static AppInfo load(String packageName){
        try {
            PackageInfo p = pm.getPackageInfo(packageName, 0);
            AppInfo app = transfer(p);
            uidCache.put(app.uid, app);
            pkgCache.put(app.pkgName, app);
            return app;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static AppInfo transfer(PackageInfo p){
        AppInfo app = new AppInfo();
        String pkgName = p.applicationInfo.packageName;
        app.icon = p.applicationInfo.loadIcon(pm);
        app.name = pm.getApplicationLabel(p.applicationInfo).toString();
        app.pkgName = pkgName;
        app.uid = p.applicationInfo.uid;
        app.isSystem = (p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        app.hasPermission = true;
        return app;
    }


    private static List<AppInfo> load(){
        List<AppInfo> ret = new ArrayList<>();
        List<PackageInfo> infoList = pm.getInstalledPackages(0);
        for (PackageInfo p:infoList) {
            AppInfo app = transfer(p);
            if(PackageManager.PERMISSION_GRANTED !=
                    pm.checkPermission(Manifest.permission.INTERNET, app.pkgName)){
                app.hasPermission = false;
            }else {
                app.hasPermission = true;
            }
            ret.add(app);
            uidCache.put(app.uid, app);
            pkgCache.put(app.pkgName, app);
        }
        return ret;
    }

    private static List<AppInfo> load(int[] uids){
        List<AppInfo> ret = new ArrayList<>();
        for(int uid:uids){
            AppInfo appInfo = uidCache.get(uid);
            if(appInfo == null){
                appInfo = load(uid);
            }
            if(appInfo != null) {
                ret.add(appInfo);
            }
        }
        return ret;
    }
}
