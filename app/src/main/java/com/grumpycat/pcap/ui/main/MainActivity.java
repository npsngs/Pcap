package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ImageView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.AppConfigs;
import com.grumpycat.pcap.model.SessionSet;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcap.tools.Config;
import com.grumpycat.pcaplib.VpnController;
import com.grumpycat.pcaplib.VpnMonitor;

/**
 * Created by cc.he on 2018/11/27
 */
public class MainActivity extends Activity implements View.OnClickListener{
    private DrawerLayout dl;
    private View menu;
    private ImageView btn_toggle;
    private AppInfoBar appInfoBar;
    private CaptureList captureList;
    private SideMenu sideMenu;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_main);
        dl = findViewById(R.id.dl);
        menu = findViewById(R.id.menu);
        btn_toggle = findViewById(R.id.btn_toggle);
        findViewById(R.id.btn_menu).setOnClickListener(this);
        findViewById(R.id.tv_select_app).setOnClickListener(this);
        btn_toggle.setOnClickListener(this);
        appInfoBar = new AppInfoBar(this);
        captureList = new CaptureList(this);
        sideMenu = new SideMenu(this);
        AppManager.asyncLoadAppInfo(this, () -> loadConfigs());
        VpnMonitor.setStatusListener(statusListener);
        SessionSet.startObserve();
    }

    @Override
    protected void onResume() {
        super.onResume();
        captureList.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        captureList.onStop();
    }

    private void loadConfigs(){
        int[] uids = Config.getSelectApps();
        runOnUiThread(()->{
            appInfoBar.setAppUid(uids);
            if(VpnMonitor.isSingleApp()){
                captureList.showSingleApp(VpnMonitor.getSingleAppUid());
            }else{
                captureList.showMultiApp();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_menu:
                toggleMenu();
                break;
            case R.id.tv_select_app:
                gotoSelectAppPage();
                break;
            case R.id.btn_toggle:
                if(VpnMonitor.isVpnRunning()){
                    VpnController.stopVpn(this);
                }else {
                    VpnController.setIsUdpNeedSave(!AppConfigs.isFilterUdp());
                    VpnController.setIsCrackTLS(AppConfigs.isCrackTls());

                    Intent intent = VpnController.startVpn(this);
                    if (intent != null) {
                        startActivityForResult(intent, 1024);
                    }
                }
                break;
        }
    }

    private final int REQUEST_PACKAGE = 1024;
    private void gotoSelectAppPage(){
        Intent intent = new Intent(this, AppListActivity.class);
        startActivityForResult(intent, REQUEST_PACKAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PACKAGE && resultCode == RESULT_OK) {
            int[] uids = data.getIntArrayExtra("app_uid");
            Config.saveSelectApps(uids);
            appInfoBar.setAppUid(uids);
            if(VpnMonitor.isSingleApp()){
                captureList.showSingleApp(VpnMonitor.getSingleAppUid());
            }else{
                captureList.showMultiApp();
            }
        }
    }

    private void toggleMenu(){
        if(dl.isDrawerOpen(menu)){
            dl.closeDrawer(menu);
        }else{
            dl.openDrawer(menu);
            sideMenu.onOpen();
        }
    }


    private VpnMonitor.StatusListener statusListener = new VpnMonitor.StatusListener() {
        @Override
        public void onVpnStart() {
            runOnUiThread(()-> btn_toggle.setImageResource(R.mipmap.ic_stop));
        }

        @Override
        public void onVpnStop() {
            runOnUiThread(()-> btn_toggle.setImageResource(R.mipmap.ic_start));
        }
    };
}
