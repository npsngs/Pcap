package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.BaseHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcap.ui.base.SingleList;
import com.grumpycat.pcap.ui.base.TitleBar;
import com.grumpycat.pcaplib.data.DataCacheHelper;
import com.grumpycat.pcaplib.data.DataParser;
import com.grumpycat.pcaplib.data.ParseMeta;
import com.grumpycat.pcaplib.data.ParseResult;

/**
 * Created by cc.he on 2018/12/7
 */
public class SessionDetailActi extends Activity implements DataParser.ParseCallback{
    public static void goLaunch(Activity from, String vpnStartTime, int sessionId){
        Intent intent = new Intent(from, SessionDetailActi.class);
        intent.putExtra("vpnStartTime", vpnStartTime);
        intent.putExtra("sessionId", sessionId);
        from.startActivity(intent);
    }

    private String vpnStartTime;
    private int sessionId;
    private DataParser parser;
    private SingleList singleList;
    private DataAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vpnStartTime = getIntent().getStringExtra("vpnStartTime");
        sessionId = getIntent().getIntExtra("sessionId", 0);
        setContentView(R.layout.acti_historys);
        TitleBar titleBar = new TitleBar(this);
        titleBar.setTitleStr(R.string.data);
        singleList = new SingleList(this);
        adapter = new DataAdapter();
        singleList.setAdapter(adapter);
        parser = DataCacheHelper.createParser(vpnStartTime, sessionId);
        parser.asyncParse(this);
        requestBg = getResources().getColor(R.color.colorAccent_light);
        requestTextColor = getResources().getColor(R.color.colorAccent);
        responseBg = getResources().getColor(R.color.colorPrimaryDark_light);
        responseTextColor = getResources().getColor(R.color.colorPrimaryDark);
    }


    @Override
    public void onParseFinish(ParseResult result) {
        runOnUiThread(()->adapter.setData(result.getParseMetas()));
    }

    @Override
    public void onParseFailed() {
        runOnUiThread(()->adapter.removeAll());
    }

    private class DataAdapter extends BaseAdapter<ParseMeta>{

        @Override
        protected int getItemLayoutRes() {
            return R.layout.item_data;
        }

        @NonNull
        @Override
        public BaseHolder<ParseMeta> onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new DataHolder(createItemView(viewGroup, i));
        }
    }
    private int requestBg;
    private int requestTextColor;
    private int responseBg;
    private int responseTextColor;

    private class DataHolder extends BaseHolder<ParseMeta>{
        private TextView tv;
        public DataHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void initWithView(View itemView) {
            tv = (TextView) itemView;
        }

        @Override
        protected void onBindData(BindDataGetter<ParseMeta> dataGetter) {
            ParseMeta meta = dataGetter.getItemData(getAdapterPosition());
            if(meta.isSend()){
                tv.setBackgroundColor(requestBg);
                tv.setTextColor(requestTextColor);
            }else{
                tv.setBackgroundColor(responseBg);
                tv.setTextColor(responseTextColor);
            }
            tv.setText(new String(meta.getData()));
        }
    }

}
