package com.grumpycat.pcap;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.ThreadProxy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/5.
 *         Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class HistoryFragment extends BaseFragment {

    private SwipeRefreshLayout refreshContainer;
    private RecyclerView timeList;
    private String[] list;
    private Handler handler;
    private HistoryListAdapter historyListAdapter;
    private String[] rawList;

    @Override
    int getLayout() {
        return R.layout.fragment_history;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshContainer = view.findViewById(R.id.refresh_container);
        timeList = view.findViewById(R.id.time_list);
        timeList.setLayoutManager(new LinearLayoutManager(getActivity()));
        getDataAndRefreshView();
        refreshContainer.setEnabled(true);
        refreshContainer.setOnRefreshListener(() -> getDataAndRefreshView());
        handler = new Handler();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void getDataAndRefreshView() {
        ThreadProxy.getInstance().execute(() -> {
            File file = new File(Const.CONFIG_DIR);
            File[] files = file.listFiles();
            if(files==null||files.length==0){
                list=null;
                refreshView();
                return;
            }
            List<File> fileList = new ArrayList<>();
            Collections.addAll(fileList, files);
            Collections.sort(fileList, (o1, o2) -> (int) (o2.lastModified() - o1.lastModified()));
            list = new String[fileList.size()];
            rawList=new String[fileList.size()];
            for (int i = 0; i < list.length; i++) {
                String name = fileList.get(i).getName();
                rawList[i]=name;
                list[i] =name.replace('_', ' ');
            }
            refreshView();
        });
    }

    private void refreshView() {
        if (handler == null) {
            return;
        }
        handler.post(() -> {
            if (historyListAdapter == null) {
                historyListAdapter = new HistoryListAdapter();
                timeList.setAdapter(historyListAdapter);
            } else if (timeList.getAdapter() == null) {
                timeList.setAdapter(historyListAdapter);
            } else {
                historyListAdapter.notifyDataSetChanged();
            }
            refreshContainer.setRefreshing(false);
        });
    }

    class HistoryListAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = View.inflate(getActivity(), R.layout.item_select_date, null);
            return new CommonHolder(inflate);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ((CommonHolder) holder).date.setText(list[position]);
            holder.itemView.setOnClickListener(v -> {
                String fileDir = Const.CONFIG_DIR + rawList[position];
                ConnectionListActivity.openActivity(getActivity(), fileDir);
            });
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.length;
        }

        class CommonHolder extends RecyclerView.ViewHolder {
            TextView date;

            public CommonHolder(View itemView) {
                super(itemView);
                date = itemView.findViewById(R.id.date);
            }
        }
    }


}
