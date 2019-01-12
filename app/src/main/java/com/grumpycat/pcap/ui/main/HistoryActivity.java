package com.grumpycat.pcap.ui.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.base.BaseActi;
import com.grumpycat.pcap.model.HistoryInfo;
import com.grumpycat.pcap.model.SessionSet;
import com.grumpycat.pcap.tools.Util;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.BaseHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcap.ui.base.SingleList;
import com.grumpycat.pcaplib.VpnMonitor;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.util.StrUtil;
import com.grumpycat.pcaplib.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cc.he on 2018/12/6
 */
public class HistoryActivity extends BaseActi {
    private SingleList singleList;
    private HistoryAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_session_list);
        getToolbar().setTitle(R.string.history);
        singleList = new SingleList(this);
        singleList.showDivider(
                Util.dp2px(this, 0.5f),
                Util.dp2px(this, 8f),
                0xff787878);
        adapter = new HistoryAdapter();
        singleList.setAdapter(adapter);

        ThreadPool.execute(() -> loadHistory());
    }

    private void loadHistory(){
        List<NetSession> sessions = SessionManager.getInstance().loadHistory();
        SessionSet.setHistory(sessions);
        List<HistoryInfo> historys = new ArrayList<>();
        HistoryInfo cur = null;
        long nowVpnTime = VpnMonitor.isVpnRunning()?VpnMonitor.getVpnStartTime():0;
        for(NetSession session:sessions){
            long vpnStartTime = session.getVpnStartTime();
            if(vpnStartTime == nowVpnTime){
                continue;
            }

            if(cur == null){
                cur = new HistoryInfo();
                historys.add(cur);
                cur.addCount(1);
                cur.setTimeStamp(vpnStartTime);
            }else{
                if(cur.getTimeStamp() == vpnStartTime){
                    cur.addCount(1);
                }else{
                    cur = new HistoryInfo();
                    cur.setTimeStamp(vpnStartTime);
                    historys.add(cur);
                }
            }
        }

        runOnUiThread(()-> adapter.setData(historys));
    }


    private class HistoryAdapter extends BaseAdapter<HistoryInfo> {

        @Override
        protected int getItemLayoutRes(int viewType) {
            return R.layout.item_history;
        }
        @NonNull
        @Override
        public BaseHolder<HistoryInfo> onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new HistoryHolder(createItemView(viewGroup, i));
        }
    }

    private class HistoryHolder extends BaseHolder<HistoryInfo> {
        private TextView tv_time, tv_count;
        public HistoryHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void initWithView(View itemView) {
            tv_time = findViewById(R.id.tv_time);
            tv_count = findViewById(R.id.tv_count);
        }

        @Override
        protected void onBindData(BindDataGetter<HistoryInfo> dataGetter) {
            int pos = getAdapterPosition();
            HistoryInfo bean = dataGetter.getItemData(pos);
            tv_time.setText(StrUtil.formatYYMMDD_HHMMSS(bean.getTimeStamp()));
            tv_count.setText(String.format("%d", bean.getSessionCount()));
            itemView.setOnClickListener((view)-> HistorySessionsActi.goLaunch(
                    HistoryActivity.this,
                    bean.getTimeStamp()));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SessionSet.setHistory(null);
    }
}
