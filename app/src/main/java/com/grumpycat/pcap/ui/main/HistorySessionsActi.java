package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.grumpycat.pcap.PacketDetailActivity;
import com.grumpycat.pcap.R;
import com.grumpycat.pcap.model.SessionSet;
import com.grumpycat.pcap.ui.adapter.HistorySessionsAdapter;
import com.grumpycat.pcap.ui.base.SingleList;
import com.grumpycat.pcap.ui.base.TitleBar;
import com.grumpycat.pcaplib.util.StrUtil;


/**
 * Created by cc.he on 2018/12/6
 */
public class HistorySessionsActi extends Activity {
    public static void goLaunch(Activity from, long vpnStartTime){
        Intent intent = new Intent(from, HistorySessionsActi.class);
        intent.putExtra("vpnStartTime", vpnStartTime);
        from.startActivity(intent);
    }

    private SingleList singleList;
    private HistorySessionsAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_historys);
        long vpnStartTime = getIntent().getLongExtra("vpnStartTime", 0);
        TitleBar titleBar = new TitleBar(this);
        titleBar.setTitleStr(StrUtil.formatYYMMDDHHMMSS(vpnStartTime));
        singleList = new SingleList(this);
        adapter = new HistorySessionsAdapter(){
            @Override
            protected void onJump(String dir) {
                PacketDetailActivity.startActivity(HistorySessionsActi.this, dir);
            }
        };
        singleList.setAdapter(adapter);
        adapter.setData(SessionSet.getHistory(vpnStartTime));
    }
}
