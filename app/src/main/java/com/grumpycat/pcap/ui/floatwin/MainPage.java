package com.grumpycat.pcap.ui.floatwin;

import android.view.View;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.model.SessionSet;
import com.grumpycat.pcap.tools.AppConfigs;
import com.grumpycat.pcap.tools.Config;
import com.grumpycat.pcaplib.VpnController;
import com.grumpycat.pcaplib.VpnMonitor;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcaplib.util.CommonUtil;

/**
 * Created by cc.he on 2018/12/6
 */
public class MainPage extends PageUnit {
    MainPage(PageHome home) {
        super(home);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.page_single_list;
    }

    private FloatingCaptureList captureList;
    @Override
    public void onViewCreate(View pageRoot) {
        super.onViewCreate(pageRoot);
        captureList = new FloatingCaptureList(pageRoot, this);
    }

    @Override
    public void onStart() {
        setRightBtn(R.drawable.sl_ic_close, 0);
        setToggleBtn();
        int[] uids = Config.getSelectApps();
        if(uids == null || uids.length == 0){
            setTitleStr(R.string.all_app);
        }else{
            AppManager.asyncLoad(uids[0], appInfo->{
                if(uids.length == 1){
                    setTitleStr(appInfo.name);
                }else{
                    setTitleStr(appInfo.name + "...");
                }
            });
        }

        if(VpnMonitor.isSingleApp()){
            captureList.showSingleApp(VpnMonitor.getSingleAppUid());
        }else{
            captureList.showMultiApp();
        }
    }

    private void setToggleBtn(){
        if(VpnMonitor.isVpnRunning()){
            setRightBtn(R.drawable.sl_ic_stop_record, 1);
        }else{
            setRightBtn(R.drawable.sl_ic_start_record, 1);
        }
    }

    @Override
    public void onStop() {
        captureList.onStop();
    }

    @Override
    public void onRightClick(int num) {
        if(num == 0){
            AppConfigs.setShowFloating(false);
            home.getService().stopSelf();
        }else{
            if(VpnMonitor.isVpnRunning()){
                VpnController.stopVpn(home.getService());
                setRightBtn(R.drawable.sl_ic_start_record, 1);
            }else {
                boolean hasPermit = CommonUtil.checkPermission(home.getService(),
                        "android.permission.WRITE_EXTERNAL_STORAGE");
                if(hasPermit){
                    VpnController.setIsUdpNeedSave(!AppConfigs.isFilterUdp());
                    VpnController.setIsCrackTLS(AppConfigs.isCrackTls());

                    VpnController.startVpn(home.getService());
                    setRightBtn(R.drawable.sl_ic_stop_record, 1);
                    SessionSet.clear();
                }
            }
        }
    }
}
