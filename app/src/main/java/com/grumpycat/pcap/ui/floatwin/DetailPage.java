package com.grumpycat.pcap.ui.floatwin;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.Config;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.BaseHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcap.ui.base.SingleList;
import com.grumpycat.pcap.ui.detail.SessionDetailActi;
import com.grumpycat.pcaplib.data.DataCacheHelper;
import com.grumpycat.pcaplib.data.ParseMeta;
import com.grumpycat.pcaplib.data.ParseResult;
import com.grumpycat.pcaplib.util.IOUtils;
import com.grumpycat.pcaplib.util.ThreadPool;


import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public class DetailPage extends PageUnit {
    DetailPage(PageHome home) {
        super(home);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.page_single_list;
    }

    private String appName;
    private String vpnStartTime;
    private int sessionId;
    private int protocol;
    private SingleList singleList;
    private DataAdapter adapter;
    @Override
    public void onViewCreate(View pageRoot) {
        super.onViewCreate(pageRoot);
        singleList = new SingleList(pageRoot);
        adapter = new DataAdapter();
        singleList.setAdapter(adapter);
        Bundle params = getParams();
        if(params != null){
            appName = params.getString("name");
            protocol = params.getInt("protocol");
            vpnStartTime = params.getString("vpnStartTime");
            sessionId = params.getInt("sessionId");
            ThreadPool.runUIWorker(()->{
                ParseResult parseResult = DataCacheHelper.parseSession(vpnStartTime, sessionId);
                runOnUiThread(()-> adapter.setData(parseResult.getParseMetas()));
            });
        }else{
            exit();
        }
    }

    @Override
    public void onStart() {
        hideRightBtns();
        setRightBtn(R.drawable.sl_ic_fullscreen, 0);
        setTitleStr(appName);
        super.onStart();
    }

    @Override
    public void onRightClick(int num) {
        if(num == 0){
            home.setVisibility(View.INVISIBLE);
            SessionDetailActi.goLaunch(getContext(),
                    appName,
                    protocol,
                    vpnStartTime,
                    sessionId,
                    true);
        }
    }

    private class DataAdapter extends BaseAdapter<ParseMeta> {

        @Override
        protected int getItemLayoutRes(int viewType) {
            return R.layout.item_floating_detail;
        }

        @NonNull
        @Override
        public BaseHolder<ParseMeta> onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            return new DetailHolder(createItemView(viewGroup, viewType));
        }
    }

    private class DetailHolder extends BaseHolder<ParseMeta>{
        private ImageView iv_tag;
        private TextView tv_data;
        public DetailHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void initWithView(View itemView) {
            tv_data = findViewById(R.id.tv_data);
            iv_tag = findViewById(R.id.iv_tag);
        }

        @Override
        protected void onBindData(BindDataGetter<ParseMeta> dataGetter) {
            ParseMeta meta = dataGetter.getItemData(getAdapterPosition());
            if(meta.isSend()){
                iv_tag.setImageResource(R.drawable.ic_tag_arraw_up);
                tv_data.setBackgroundResource(R.color.side_menu_bg);
                tv_data.setTextColor(0xFFFBFCFE);
            }else{
                iv_tag.setImageResource(R.drawable.ic_tag_arraw_down);
                tv_data.setBackgroundColor(0xffededed);
                tv_data.setTextColor(0xff5f5f5f);

            }
            ThreadPool.runUIWorker(new ParseAction(getAdapterPosition(), meta));
        }

        class ParseAction implements Runnable{
            private int pos;
            private ParseMeta meta;

            public ParseAction(int pos, ParseMeta meta) {
                this.pos = pos;
                this.meta = meta;
            }


            private void parseAsRaw()throws Exception{
                byte[] data = new byte[(int) Config.MAX_LOAD];
                Source source = Okio.source(meta.getDataFile());
                BufferedSource bs = Okio.buffer(source);
                int size = bs.read(data);
                IOUtils.safeClose(bs);
                String str = new String(data, 0 , size);
                tv_data.post(() ->{
                    if(pos == getAdapterPosition()) {
                        tv_data.setText(str);
                    }
                });
            }


            @Override
            public void run() {
                try {
                    parseAsRaw();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
