package com.grumpycat.pcap.ui.floatwin;

import android.os.Bundle;
import android.view.View;

import com.grumpycat.pcap.R;

/**
 * Created by cc.he on 2018/12/6
 */
public class SessionsPage extends PageUnit {
    SessionsPage(PageHome home) {
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
        hideRightBtns();
        Bundle params = getParams();
        if(params!=null){
            setTitleStr(params.getString("name"));
            int uid = params.getInt("uid");
            captureList.showSingleApp(uid);
        }else{
            exit();
        }
    }

    @Override
    public void onStop() {
        captureList.onStop();
    }
}
