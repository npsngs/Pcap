package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.grumpycat.pcap.PacketDetailActivity;
import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.AppSessionListener;
import com.grumpycat.pcap.model.AppSessions;
import com.grumpycat.pcap.model.SessionSet;
import com.grumpycat.pcap.model.SessionListener;
import com.grumpycat.pcap.ui.adapter.AppSessionAdapter;
import com.grumpycat.pcap.ui.adapter.SessionsAdapter;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.ListDividerDrawable;
import com.grumpycat.pcap.ui.base.UiWidget;
import com.grumpycat.pcap.tools.Util;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.util.StrUtil;

/**
 * Created by cc.he on 2018/11/27
 */
public class CaptureList extends UiWidget{
    private RecyclerView rcv;
    private SessionManager sm;
    private SessionsAdapter captureAdapter;
    private AppSessionAdapter appSessionAdapter;
    private BaseAdapter adapter;
    public CaptureList(Activity activity) {
        super(activity);
        rcv = findViewById(R.id.rcv);
        rcv.setLayoutManager(new LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration did = new DividerItemDecoration(activity,
                DividerItemDecoration.VERTICAL);
        did.setDrawable(new ListDividerDrawable(
                Util.dp2px(activity, 1f),
                0xffeeeeee));
        rcv.addItemDecoration(did);
        captureAdapter = new SessionsAdapter(){
            @Override
            protected void onJump(NetSession session) {
                SessionDetailActi.goLaunch(getActivity(),
                        StrUtil.formatYYMMDDHHMMSS(session.getVpnStartTime()),
                        session.hashCode());
                //PacketDetailActivity.startActivity(getActivity(), dir);
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
        rcv.setAdapter(adapter);
        captureAdapter.setData(SessionSet.getSessionByUid(uid));
    }

    public void showMultiApp(){
        isSingleMode = false;
        SessionSet.addAppSessionListener(appSessionListener);
        SessionSet.removeSessionListener(sessionListener);
        adapter = appSessionAdapter;
        rcv.setAdapter(adapter);
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
        }

        @Override
        public void onClear() {
            appSessionAdapter.removeAll();
        }
    };

}
