package com.grumpycat.pcap.ui.floatwin;

import android.os.Bundle;
import android.view.View;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.model.AppSessions;
import com.grumpycat.pcap.model.SessionListener;
import com.grumpycat.pcap.model.SessionSet;
import com.grumpycat.pcap.tools.AppSessionListener;
import com.grumpycat.pcap.tools.Util;
import com.grumpycat.pcap.ui.adapter.FloatingAppSessionAdapter;
import com.grumpycat.pcap.ui.adapter.FloatingSessionsAdapter;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.SingleList;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.util.StrUtil;

/**
 * Created by cc.he on 2018/11/27
 */
public class FloatingCaptureList {
    private SessionManager sm;
    private FloatingSessionsAdapter captureAdapter;
    private FloatingAppSessionAdapter appSessionAdapter;
    private BaseAdapter adapter;
    private SingleList singleList;
    private PageUnit page;
    public FloatingCaptureList(View root, PageUnit page) {
        this.page = page;
        singleList = new SingleList(root);
        singleList.showDivider(
                Util.dp2px(root.getContext(), 0.5f),0,
                0xff787878);
        captureAdapter = new FloatingSessionsAdapter(){
            @Override
            protected void onJump(NetSession session) {
                AppManager.asyncLoad(session.getUid(), appInfo->{
                    String appName = appInfo != null
                            ?appInfo.name
                            :root.getContext().getString(R.string.unknown);
                    Bundle params = new Bundle();
                    params.putString("name", appName);
                    params.putString("vpnStartTime",StrUtil.formatYYMMDD_HHMMSS(session.getVpnStartTime()));
                    params.putInt("sessionId", session.hashCode());
                    PageUnit next = new DetailPage(page.home);
                    next.setParams(params);
                    page.startPage(next);
                });
            }
        };
        appSessionAdapter = new FloatingAppSessionAdapter(){
            @Override
            protected void onJump(int uid) {
                PageUnit next = new SessionsPage(page.home);
                Bundle params = new Bundle();
                params.putInt("uid", uid);
                AppManager.asyncLoad(uid, appInfo->{
                    String appName = appInfo != null
                            ?appInfo.name
                            :root.getContext().getString(R.string.unknown);
                    params.putString("name", appName);
                    next.setParams(params);
                    page.startPage(next);
                });
            }
        };

        sm = SessionManager.getInstance();
    }

    public void onStart(){
        if(adapter == null){
            return;
        }

        if(isSingleMode){
            adapter.setData(SessionSet.getSessionByUid(this.uid));
            SessionSet.addSessionListener(sessionListener);
        }else{
            adapter.setData(SessionSet.getAppSessions());
            SessionSet.addAppSessionListener(appSessionListener);
        }
    }


    public void onStop(){
        SessionSet.removeSessionListener(sessionListener);
        SessionSet.removeAppSessionListener(appSessionListener);
    }

    private int uid;
    private boolean isSingleMode;
    public void showSingleApp(int uid){
        isSingleMode = true;
        SessionSet.addSessionListener(sessionListener);
        SessionSet.removeAppSessionListener(appSessionListener);
        this.uid = uid;
        adapter = captureAdapter;
        singleList.setAdapter(adapter);
        captureAdapter.setData(SessionSet.getSessionByUid(uid));
    }

    public void showMultiApp(){
        isSingleMode = false;
        SessionSet.addAppSessionListener(appSessionListener);
        SessionSet.removeSessionListener(sessionListener);
        adapter = appSessionAdapter;
        singleList.setAdapter(adapter);
        appSessionAdapter.setData(SessionSet.getAppSessions());
    }


    private SessionListener sessionListener = new SessionListener() {
        @Override
        public void onUpdate(NetSession session) {
            if(uid > 0 && session.getUid() != uid){
                return;
            }
            int sn = session.getSerialNumber();
            int size = captureAdapter.getItemCount();
            captureAdapter.notifyItemChanged(size - sn -1);
        }

        @Override
        public void onNewAdd(NetSession session) {
            if(session.getUid() != FloatingCaptureList.this.uid){
                return;
            }
            captureAdapter.add(session, 0);
            singleList.tryScrollToFirst();
        }

        @Override
        public void onClear() {
            captureAdapter.removeAll();
        }
    };

    private AppSessionListener appSessionListener = new AppSessionListener() {
        @Override
        public void onUpdate(AppSessions session) {
            int sn = session.getSerialNumber();
            int size = appSessionAdapter.getItemCount();
            appSessionAdapter.notifyItemChanged(size - sn -1);
        }

        @Override
        public void onNewAdd(AppSessions session) {
            appSessionAdapter.add(session, 0);
            singleList.tryScrollToFirst();
        }

        @Override
        public void onClear() {
            appSessionAdapter.removeAll();
        }
    };
}
