package com.grumpycat.pcap.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.base.BaseActi;
import com.grumpycat.pcap.model.SessionSet;
import com.grumpycat.pcap.tools.AppConfigs;
import com.grumpycat.pcap.tools.Config;
import com.grumpycat.pcap.ui.floatwin.FloatingService;
import com.grumpycat.pcaplib.VpnController;
import com.grumpycat.pcaplib.VpnMonitor;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcaplib.util.CommonUtil;


/**
 * Created by cc.he on 2018/11/27
 */
public class MainActivity extends BaseActi implements Toolbar.OnMenuItemClickListener {
    private DrawerLayout dl;
    private View menu;
    private MenuItem startBtn;
    private AppInfoBar appInfoBar;
    private CaptureList captureList;
    private SideMenu sideMenu;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_main);

        getToolbar().setNavigationIcon(R.drawable.sl_ic_menu);
        getToolbar().setNavigationOnClickListener(v -> toggleMenu());

        getToolbar().inflateMenu(R.menu.title_main);
        startBtn = getToolbar().getMenu().findItem(R.id.it_start_record);
        getToolbar().setOnMenuItemClickListener(this);
        appInfoBar = new AppInfoBar(this);

        captureList = new CaptureList(this);

        dl = findViewById(R.id.dl);
        menu = findViewById(R.id.menu);
        sideMenu = new SideMenu(this);
        AppManager.init(this);
        loadConfigs();
        FloatingService.closeFloatingWindow(this);
        VpnMonitor.setStatusListener(statusListener);
        SessionSet.startObserve();
        if (VpnMonitor.isVpnRunning()) {
            startBtn.setIcon(R.drawable.sl_ic_stop_record);
        }else{
            startBtn.setIcon(R.drawable.sl_ic_start_record);
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
        if(uids == null || uids.length == 0){
            VpnMonitor.setAllowUids(null);
        }else{
            VpnMonitor.setAllowUids(uids);
        }

        runOnUiThread(()->{
            hideProgressBar();
            appInfoBar.setAppUid(uids);
            if(VpnMonitor.isSingleApp()){
                captureList.showSingleApp(VpnMonitor.getSingleAppUid());
            }else{
                captureList.showMultiApp();
            }
        });
    }


    private final int REQUEST_PACKAGE = 1024;
    private void gotoSelectAppPage(){
        Intent intent = new Intent(this, AppListActivity.class);
        startActivityForResult(intent, REQUEST_PACKAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
           switch (requestCode){
               case REQUEST_PACKAGE:
                   int[] uids = data.getIntArrayExtra("app_uid");
                   Config.saveSelectApps(uids);
                   if(uids == null || uids.length == 0){
                       VpnMonitor.setAllowUids(null);
                   }else{
                       VpnMonitor.setAllowUids(uids);
                   }
                   appInfoBar.setAppUid(uids);
                   if(VpnMonitor.isSingleApp()){
                       captureList.showSingleApp(VpnMonitor.getSingleAppUid());
                   }else{
                       captureList.showMultiApp();
                   }
                   break;
               case CommonUtil.REQUEST_EXTERNAL_STORAGE:
                   startVPN();
                   break;
           }
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.it_start_record:
                if(VpnMonitor.isVpnRunning()){
                    VpnController.stopVpn(this);
                    startBtn.setIcon(R.drawable.sl_ic_start_record);
                }else {
                    boolean hasPermit = CommonUtil.checkPermission(this,
                            "android.permission.WRITE_EXTERNAL_STORAGE");
                    if(!hasPermit){
                        return false;
                    }

                    startVPN();
                }
                return true;
            case R.id.it_select_apps:
                gotoSelectAppPage();
                return true;
            case R.id.it_toggle_floating:
                FloatingService.showFloatingWindow(this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startVPN() {
        VpnController.setIsUdpNeedSave(!AppConfigs.isFilterUdp());
        VpnController.setIsCrackTLS(AppConfigs.isCrackTls());

        Intent intent = VpnController.startVpn(this);
        if (intent != null) {
            startActivityForResult(intent, 1023);
        }
        SessionSet.clear();
        startBtn.setIcon(R.drawable.sl_ic_stop_record);
    }

    private VpnMonitor.StatusListener statusListener = new VpnMonitor.StatusListener() {
        @Override
        public void onVpnStart() {
            runOnUiThread(()-> startBtn.setIcon(R.drawable.sl_ic_stop_record));
        }

        @Override
        public void onVpnStop() {
            runOnUiThread(()-> startBtn.setIcon(R.drawable.sl_ic_start_record));
        }
    };
}
