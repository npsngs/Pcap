package com.grumpycat.pcap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.grumpycat.pcap.appinfo.AppInfo;
import com.grumpycat.pcap.appinfo.AppManager;
import com.grumpycat.pcap.ui.base.BaseRecyclerAdapter;
import com.grumpycat.pcap.ui.base.BaseRecyclerViewHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcap.ui.base.ListDividerDrawable;
import com.grumpycat.pcap.ui.base.Util;


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
        findViewById(R.id.back).setOnClickListener(v -> onItemClick(-1));
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
        }else{
            progressBar.setVisibility(View.VISIBLE);
            AppManager.setFinishListener(() -> {
                progressBar.setVisibility(View.GONE);
                appListAdapter.setData(AppManager.getApps());
            });
        }
    }

    private void onItemClick(int position){
        Intent intent = new Intent();
        if (position >= 0){
            AppInfo appInfo = appListAdapter.getItemData(position);
            intent.putExtra("app_uid", appInfo.uid);
        }
        setResult(RESULT_OK, intent);
        finish();
    }


    private class AppListAdapter extends BaseRecyclerAdapter<AppInfo> {

        @Override
        public View createItemView(@NonNull ViewGroup parent, int viewType) {
            return View.inflate(AppListActivity.this, R.layout.item_app_list, null);
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
            itemView.setOnClickListener(v -> onItemClick(getAdapterPosition()));

            appIcon = itemView.findViewById(R.id.iv_icon);
            appName = itemView.findViewById(R.id.tv_name);
            appUid = itemView.findViewById(R.id.tv_uid);
            appType = itemView.findViewById(R.id.tv_type);
        }

        @Override
        protected void onBindData(BindDataGetter<AppInfo> dataGetter) {
            AppInfo appInfo = dataGetter.getItemData(getAdapterPosition());
            appIcon.setImageDrawable(appInfo.icon);
            appName.setText(appInfo.name);
            appUid.setText("uid:"+appInfo.uid);
            appType.setText(appInfo.isSystem?"System":"Normal");
        }
    }
}
