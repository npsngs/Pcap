package com.grumpycat.pcap.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.base.BaseActi;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.BaseHolder;
import com.grumpycat.pcap.ui.base.SingleList;
import com.grumpycat.pcap.ui.floatwin.FloatingService;
import com.grumpycat.pcaplib.data.DataCacheHelper;
import com.grumpycat.pcaplib.data.ParseMeta;
import com.grumpycat.pcaplib.data.ParseResult;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.ThreadPool;


/**
 * Created by cc.he on 2018/12/7
 */
public class SessionDetailActi extends BaseActi{
    public static void goLaunch(Context from,
                                String appName,
                                int protocol,
                                String vpnStartTime,
                                int sessionId,
                                boolean isFromFloating){
        Intent intent = new Intent(from, SessionDetailActi.class);
        intent.putExtra("appName", appName);
        intent.putExtra("vpnStartTime", vpnStartTime);
        intent.putExtra("protocol", protocol);
        intent.putExtra("sessionId", sessionId);
        intent.putExtra("isFromFloating",isFromFloating);
        from.startActivity(intent);
    }
    public static void goLaunch(Context from,
                                String appName,
                                int protocol,
                                String vpnStartTime,
                                int sessionId){
        Intent intent = new Intent(from, SessionDetailActi.class);
        intent.putExtra("appName", appName);
        intent.putExtra("vpnStartTime", vpnStartTime);
        intent.putExtra("protocol", protocol);
        intent.putExtra("sessionId", sessionId);
        intent.putExtra("isFromFloating",false);
        from.startActivity(intent);
    }


    private String vpnStartTime;
    private int sessionId;
    private int protocol;
    private SingleList singleList;
    private DataAdapter adapter;
    private boolean isFromFloating;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_single_list);
        vpnStartTime = getIntent().getStringExtra("vpnStartTime");
        sessionId = getIntent().getIntExtra("sessionId", 0);
        protocol = getIntent().getIntExtra("protocol", Const.IP);
        isFromFloating = getIntent().getBooleanExtra("isFromFloating",false);
        showProtocol = protocol;
        String title = getIntent().getStringExtra("appName");
        getToolbar().setTitle(title);

        singleList = new SingleList(this);
        adapter = new DataAdapter();
        singleList.setAdapter(adapter);
        showProgressBar();

        ThreadPool.runUIWorker(()->{
            parseResult = DataCacheHelper.parseSession(vpnStartTime, sessionId);
            runOnUiThread(()->{
                adapter.setData(parseResult.getParseMetas());
                hideProgressBar();
            });
        });

        SessionDetailHolder.requestTextColor = getResources().getColor(R.color.white_text);
        SessionDetailHolder.responseTextColor = getResources().getColor(R.color.dark_gray_text);
    }


    @Override
    protected void onStop() {
        if (isFromFloating){
            super.onStop();
            FloatingService.setVisible(this);
            finish();
        }else{
            super.onStop();
        }
    }

    private ParseResult parseResult;
    private int showProtocol;
    private class DataAdapter extends BaseAdapter<ParseMeta>{

        @Override
        protected int getItemLayoutRes(int viewType) {
            return R.layout.item_session_detail;
        }

        @NonNull
        @Override
        public BaseHolder<ParseMeta> onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            SessionDetailHolder holder =  new SessionDetailHolder(createItemView(viewGroup, viewType));
            holder.setShowProtocol(showProtocol);
            return holder;
        }
    }
}
