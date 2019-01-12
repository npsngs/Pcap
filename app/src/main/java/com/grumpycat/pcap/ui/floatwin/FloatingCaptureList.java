package com.grumpycat.pcap.ui.floatwin;

import android.view.View;

import com.grumpycat.pcap.model.AppSessions;
import com.grumpycat.pcap.model.SessionListener;
import com.grumpycat.pcap.model.SessionSet;
import com.grumpycat.pcap.tools.AppSessionListener;
import com.grumpycat.pcap.tools.Util;
import com.grumpycat.pcap.ui.adapter.FloatingAppSessionAdapter;
import com.grumpycat.pcap.ui.adapter.FloatingSessionsAdapter;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.SingleList;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;

/**
 * Created by cc.he on 2018/11/27
 */
public class FloatingCaptureList {
    private SessionManager sm;
    private FloatingSessionsAdapter captureAdapter;
    private FloatingAppSessionAdapter appSessionAdapter;
    private BaseAdapter adapter;
    private SingleList singleList;
    private PageHome home;
    public FloatingCaptureList(View root, PageHome home) {
        singleList = new SingleList(root);
        singleList.showDivider(
                Util.dp2px(root.getContext(), 0.5f),0,
                0xff787878);
        captureAdapter = new FloatingSessionsAdapter(){
            @Override
            protected void onJump(NetSession session) {
                /*AppInfo appInfo = AppManager.getApp(session.getUid());
                String appName = appInfo != null
                        ?appInfo.name
                        :root.getContext().getString(R.string.unknow);
                SessionDetailActi.goLaunch(root.getContext(),
                        appName,
                        session.getProtocol(),
                        StrUtil.formatYYMMDD_HHMMSS(session.getVpnStartTime()),
                        session.hashCode());*/
            }
        };
        appSessionAdapter = new FloatingAppSessionAdapter(){
            @Override
            protected void onJump(int uid) {
                //SessionListActivity.gotoActi(getActivity(), uid);
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
            if(session.getUid() != FloatingCaptureList.this.uid){
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
