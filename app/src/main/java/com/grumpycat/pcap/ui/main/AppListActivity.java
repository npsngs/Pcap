package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcaplib.appinfo.AppInfo;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcap.ui.base.BaseRecyclerAdapter;
import com.grumpycat.pcap.ui.base.BaseRecyclerViewHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcap.ui.base.ListDividerDrawable;
import com.grumpycat.pcap.tools.Util;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by cc.he on 2018/11/13
 */
public class AppListActivity extends Activity{
    private AppListAdapter appListAdapter;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_app_list);
        progressBar = findViewById(R.id.pb);
        RecyclerView rcv = findViewById(R.id.rcv);
        rcv.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration did = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        did.setDrawable(new ListDividerDrawable(
                Util.dp2px(this, 1f),
                0xffeeeeee));
        rcv.addItemDecoration(did);

        appListAdapter = new AppListAdapter();
        rcv.setAdapter(appListAdapter);

        if(AppManager.isFinishLoad()){
            appListAdapter.setData(AppManager.getApps());
            selectRecords = new boolean[appListAdapter.getItemCount()];
        }else{
            progressBar.setVisibility(View.VISIBLE);
            AppManager.setFinishListener(() -> {
                progressBar.setVisibility(View.GONE);
                appListAdapter.setData(AppManager.getApps());
                selectRecords = new boolean[appListAdapter.getItemCount()];
            });
        }

        findViewById(R.id.tv_ok).setOnClickListener(v -> {
            returnSelectApps();
        });

        findViewById(R.id.tv_all).setOnClickListener(v -> {
            selectAll();
        });

        findViewById(R.id.back).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }


    private boolean[] selectRecords;
    private void returnSelectApps(){
        Intent intent = new Intent();
        List<Integer> uids = new ArrayList<>();
        for(int i=0;i<selectRecords.length;i++){
            if(selectRecords[i]){
                AppInfo appInfo = appListAdapter.getItemData(i);
                uids.add(appInfo.uid);
            }
        }


        if(uids.size() > 0){
            int size = uids.size();
            int[] ids = new int[size];
            for(int i=0; i<size; i++ ){
                ids[i] = uids.get(i);
            }
            if(ids.length < appListAdapter.getItemCount()){
                intent.putExtra("app_uid",ids);
            }
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private void selectAll(){
        for(int i=0;i<selectRecords.length;i++){
            selectRecords[i] = true;
        }
        appListAdapter.notifyDataSetChanged();
    }

    private void selectPosition(int position){
        selectRecords[position] = !selectRecords[position];
        appListAdapter.notifyItemChanged(position);
    }

    private class AppListAdapter extends BaseRecyclerAdapter<AppInfo> {

        @Override
        public View createItemView(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return inflater.inflate(R.layout.item_app_list, parent, false);
        }

        @Override
        public BaseRecyclerViewHolder<AppInfo> onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AppItemHolder(createItemView(parent, viewType));
        }
    }

    private class AppItemHolder extends BaseRecyclerViewHolder<AppInfo>{
        private ImageView appIcon;
        private TextView appName;
        private TextView appType;
        private TextView appUid;

        public AppItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void initWithView(View itemView) {
            itemView.setOnClickListener(v -> selectPosition(getAdapterPosition()));
            appIcon = itemView.findViewById(R.id.iv_icon);
            appName = itemView.findViewById(R.id.tv_name);
            appUid = itemView.findViewById(R.id.tv_uid);
            appType = itemView.findViewById(R.id.tv_type);
        }

        @Override
        protected void onBindData(BindDataGetter<AppInfo> dataGetter) {
            int pos = getAdapterPosition();
            AppInfo appInfo = dataGetter.getItemData(pos);
            appIcon.setImageDrawable(appInfo.icon);
            appName.setText(appInfo.name);
            appUid.setText("uid:"+appInfo.uid);
            appType.setText(appInfo.isSystem?"System":"Normal");
            if(selectRecords[pos]){
                itemView.setBackgroundColor(0xffc3c3c3);
            }else{
                itemView.setBackgroundColor(0xffffffff);
            }
        }
    }
}
