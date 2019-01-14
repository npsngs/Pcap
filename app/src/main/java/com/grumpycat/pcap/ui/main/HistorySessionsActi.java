package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.base.BaseActi;
import com.grumpycat.pcap.model.SessionSet;
import com.grumpycat.pcap.tools.Util;
import com.grumpycat.pcap.ui.adapter.SessionsAdapter;
import com.grumpycat.pcap.ui.base.SingleList;
import com.grumpycat.pcap.ui.detail.SessionDetailActi;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.util.StrUtil;


/**
 * Created by cc.he on 2018/12/6
 */
public class HistorySessionsActi extends BaseActi {
    public static void goLaunch(Activity from, long vpnStartTime){
        Intent intent = new Intent(from, HistorySessionsActi.class);
        intent.putExtra("vpnStartTime", vpnStartTime);
        from.startActivity(intent);
    }

    private SingleList singleList;
    private SessionsAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_session_list);
        long vpnStartTime = getIntent().getLongExtra("vpnStartTime", 0);
        getToolbar().setTitle(StrUtil.formatYYMMDD_HHMMSS(vpnStartTime));
        singleList = new SingleList(this);
        singleList.showDivider(
                Util.dp2px(this, 0.5f),
                Util.dp2px(this, 8f),
                0xff787878);
        adapter = new SessionsAdapter(){
            @Override
            protected void onJump(NetSession session) {
                AppManager.asyncLoad(session.getUid(), appInfo->{
                    String appName = appInfo != null
                            ?appInfo.name
                            :HistorySessionsActi.this.getString(R.string.unknow);
                    SessionDetailActi.goLaunch(HistorySessionsActi.this,
                            appName,
                            session.getProtocol(),
                            StrUtil.formatYYMMDD_HHMMSS(session.getVpnStartTime()),
                            session.hashCode());
                });

            }
        };
        singleList.setAdapter(adapter);
        adapter.setData(SessionSet.getHistory(vpnStartTime));
    }
}
