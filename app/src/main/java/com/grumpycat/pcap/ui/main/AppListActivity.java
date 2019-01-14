package com.grumpycat.pcap.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.base.BaseActi;
import com.grumpycat.pcap.tools.Util;
import com.grumpycat.pcap.ui.base.SingleList;
import com.grumpycat.pcaplib.appinfo.AppInfo;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.BaseHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by cc.he on 2018/11/13
 */
public class AppListActivity extends BaseActi implements Toolbar.OnMenuItemClickListener {
    private AppListAdapter appListAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_app_list);

        getToolbar().setTitle(R.string.select_apps);
        getToolbar().inflateMenu(R.menu.title_app_list);
        getToolbar().setOnMenuItemClickListener(this);
        SingleList singleList = new SingleList(this);
        singleList.showDivider(
                Util.dp2px(this, 0.5f),
                Util.dp2px(this, 12f),
                0xff787878);
        appListAdapter = new AppListAdapter();
        singleList.setAdapter(appListAdapter);
        showProgressBar();
        AppManager.asyncLoadAll(ret->{
            hideProgressBar();
            appListAdapter.setData(filter(ret));
            selectRecords = new boolean[appListAdapter.getItemCount()];
        });
    }

    private List<AppInfo> filter(List<AppInfo> apps){
        if(apps == null || apps.size() < 1) {
            return null;
        }

        List<AppInfo> app = new ArrayList<>();
        for(AppInfo item:apps){
            if(item.hasPermission){
                app.add(item);
            }
        }

        Collections.sort(app , (o1, o2) -> {
            if (o1.isSystem && !o2.isSystem){
                return 1;
            }else if(!o1.isSystem && o2.isSystem){
                return -1;
            }
            return o1.name.compareTo(o2.name);
        });

        return app;
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

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.it_select_all:
                selectAll();
                return true;
            case R.id.it_select:
                returnSelectApps();
                return true;
        }
        return false;
    }

    private class AppListAdapter extends BaseAdapter<AppInfo> {

        @Override
        protected int getItemLayoutRes(int viewType) {
            return R.layout.item_app_list;
        }

        @Override
        public BaseHolder<AppInfo> onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AppItemHolder(createItemView(parent, viewType));
        }
    }

    private class AppItemHolder extends BaseHolder<AppInfo> {
        private ImageView appIcon;
        private TextView appName;
        private TextView tv_info;
        private ImageView iv_select;

        public AppItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void initWithView(View itemView) {
            itemView.setOnClickListener(v -> selectPosition(getAdapterPosition()));
            appIcon = itemView.findViewById(R.id.iv_icon);
            appName = itemView.findViewById(R.id.tv_name);
            tv_info = itemView.findViewById(R.id.tv_info);
            iv_select = itemView.findViewById(R.id.iv_select);
        }

        @Override
        protected void onBindData(BindDataGetter<AppInfo> dataGetter) {
            int pos = getAdapterPosition();
            AppInfo appInfo = dataGetter.getItemData(pos);
            appIcon.setImageDrawable(appInfo.icon);
            appName.setText(appInfo.name);
            tv_info.setText(String.format("[UID:%d] %s",appInfo.uid, appInfo.pkgName));

            iv_select.setVisibility(selectRecords[pos]?View.VISIBLE:View.GONE);
            if(selectRecords[pos]){
                iv_select.setVisibility(View.VISIBLE);
                itemView.setBackgroundColor(0x22000000);
            }else{
                iv_select.setVisibility(View.GONE);
                itemView.setBackgroundColor(0x00000000);
            }
        }
    }
}
