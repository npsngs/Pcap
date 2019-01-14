package com.grumpycat.pcap.ui.main;

import android.app.Activity;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.AppSessionListener;
import com.grumpycat.pcap.model.AppSessions;
import com.grumpycat.pcap.model.SessionSet;
import com.grumpycat.pcap.model.SessionListener;
import com.grumpycat.pcap.ui.adapter.AppSessionAdapter;
import com.grumpycat.pcap.ui.adapter.SessionsAdapter;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.SingleList;
import com.grumpycat.pcap.ui.base.UiWidget;
import com.grumpycat.pcap.tools.Util;
import com.grumpycat.pcap.ui.detail.SessionDetailActi;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.util.StrUtil;

/**
 * Created by cc.he on 2018/11/27
 */
public class CaptureList extends UiWidget{
    private SessionManager sm;
    private SessionsAdapter captureAdapter;
    private AppSessionAdapter appSessionAdapter;
    private BaseAdapter adapter;
    private SingleList singleList;
    public CaptureList(Activity activity) {
        super(activity);
        singleList = new SingleList(activity);
        singleList.showDivider(
                Util.dp2px(activity, 0.5f),0,
                0xff787878);
        captureAdapter = new SessionsAdapter(){
            @Override
            protected void onJump(NetSession session) {
                AppManager.asyncLoad(session.getUid(), appInfo -> {
                    String appName = appInfo != null
                            ?appInfo.name
                            :activity.getString(R.string.unknow);
                    SessionDetailActi.goLaunch(getActivity(),
                            appName,
                            session.getProtocol(),
                            StrUtil.formatYYMMDD_HHMMSS(session.getVpnStartTime()),
                            session.hashCode());
                });
            }
        };
        appSessionAdapter = new AppSessionAdapter(){
            @Override
            protected void onJump(int uid) {
                SessionListActivity.gotoActi(getActivity(), uid);
            }
        };

        sm = SessionManager.getInstance();
        sm.init(activity.getApplication());
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
            if(session.getUid() != CaptureList.this.uid){
                return;
            }
            int sn = session.getSerialNumber();
            int size = captureAdapter.getItemCount();
            captureAdapter.notifyItemChanged(size - sn -1);
        }

        @Override
        public void onNewAdd(NetSession session) {
            if(session.getUid() != CaptureList.this.uid){
                return;
            }
            captureAdapter.add(session, 0);
            trySrollToFirst();
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
            trySrollToFirst();
        }

        @Override
        public void onClear() {
            appSessionAdapter.removeAll();
        }
    };

    private void trySrollToFirst(){
        int firstPos = singleList.getLm().findFirstCompletelyVisibleItemPosition();
        if(firstPos == 0){
            singleList.getRcv().scrollToPosition(0);
        }
    }
}
