package com.grumpycat.pcap.ui.floatwin;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

/**
 * Created by cc.he on 2018/11/29
 */
public class FloatingService extends Service {
    private PageHome floatingPage;
    private FloatingBtn floatingBtn;
    @Override
    public void onCreate() {
        super.onCreate();
        init();
        show();
    }

    private void init(){
        floatingPage = new PageHome(this);
        floatingBtn = new FloatingBtn(this);
        floatingBtn.setFloatingPage(floatingPage);
        floatingPage.setFloatingBtn(floatingBtn);
    }

    private void show(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)){
            return;
        }
        floatingPage.show();
    }

    private void close(){
        floatingBtn.close();
        floatingPage.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public static void showFloatingWindow(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(activity)) {

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, 0);
        } else {
            activity.startService(new Intent(activity, FloatingService.class));
        }
    }

    public static void closeFloatingWindow(Activity activity){
        activity.stopService(new Intent(activity, FloatingService.class));
    }


}
