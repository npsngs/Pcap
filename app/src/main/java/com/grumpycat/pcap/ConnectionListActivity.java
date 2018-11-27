package com.grumpycat.pcap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.grumpycat.pcaplib.appinfo.AppInfo;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcaplib.data.FileCache;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.StrUtil;
import com.grumpycat.pcaplib.util.ThreadProxy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/6.
 *         Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class ConnectionListActivity extends Activity {

    private RecyclerView recyclerView;
    public static final String FILE_DIRNAME = "file_dirname";
    private String fileDir;
    private ArrayList<NetSession> baseNetSessions;
    private Handler handler;
    private ConnectionAdapter connectionAdapter;
    private PackageManager packageManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_list);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(ConnectionListActivity.this));
        fileDir = getIntent().getStringExtra(FILE_DIRNAME);
        handler = new Handler();
        getDataAndRefreshView();

        packageManager = getPackageManager();

    }

    private void getDataAndRefreshView() {
        ThreadProxy.getInstance().execute(() -> {
            baseNetSessions = new ArrayList<>();
            File file = new File(fileDir);
            FileCache aCache = FileCache.get(file);
            String[] list = file.list();
            if (list == null || list.length == 0) {
                refreshView();
                return;
            }
            SharedPreferences sp = getSharedPreferences(Const.VPN_SP_NAME, Context.MODE_PRIVATE);
            boolean isShowUDP = sp.getBoolean(Const.IS_UDP_SHOW, false);
            for (String fileName : list) {

                NetSession netConnection = (NetSession) aCache.getAsObject(fileName);
                if (netConnection.isUdp() && !isShowUDP) {
                    continue;
                }
                baseNetSessions.add(netConnection);
            }
            Collections.sort(baseNetSessions, new NetSession.SessionComparator());

            refreshView();

        });

    }

    private void refreshView() {
        runOnUiThread(() -> {
            if (baseNetSessions == null || baseNetSessions.size() == 0) {
                Toast.makeText(ConnectionListActivity.this, getString(R.string.no_data), Toast.LENGTH_SHORT).show();
                finish();
            }
            connectionAdapter = new ConnectionAdapter();
            recyclerView.setAdapter(connectionAdapter);
        });

    }

    public static void openActivity(Activity activity, String dir) {
        Intent intent = new Intent(activity, ConnectionListActivity.class);
        intent.putExtra(FILE_DIRNAME, dir);
        activity.startActivity(intent);

    }

    class ConnectionAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = View.inflate(ConnectionListActivity.this, R.layout.item_connection, null);
            return new ConnectionHolder(inflate);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final NetSession connection = baseNetSessions.get(position);
            ConnectionHolder connectionHolder = (ConnectionHolder) holder;
            Drawable icon;
            AppInfo appInfo = AppManager.getApp(connection.getUid());
            if (appInfo != null) {
                icon = appInfo.icon;
            } else {
                icon = getResources().getDrawable(R.drawable.sym_def_app_icon);
            }

            connectionHolder.icon.setImageDrawable(icon);
            if (appInfo != null) {
                connectionHolder.processName.setText(appInfo.name);
            } else {
                connectionHolder.processName.setText(getString(R.string.unknow));
            }

            if(connection.getHttpHeader() != null && connection.getHttpHeader().host != null){
                connectionHolder.hostName.setVisibility(View.VISIBLE);
                connectionHolder.hostName.setText(connection.getHttpHeader().host);
            }else{
                connectionHolder.hostName.setVisibility(View.GONE);
            }

            connectionHolder.isSSL.setVisibility(connection.getProtocol() == Const.HTTPS ? View.VISIBLE : View.GONE);


            connectionHolder.refreshTime.setText(StrUtil.formatHHMMSS(connection.lastActiveTime));
            int sumByte = (int) (connection.sendByte + connection.receiveByte);

            String showSum;
            if (sumByte > 1000000) {
                showSum = String.valueOf((int) (sumByte / 1000000.0 + 0.5)) + "mb";
            } else if (sumByte > 1000) {
                showSum = String.valueOf((int) (sumByte / 1000.0 + 0.5)) + "kb";
            } else {
                showSum = String.valueOf(sumByte) + "b";
            }

            connectionHolder.size.setText(showSum);
            connectionHolder.itemView.setOnClickListener(v -> {
                if (baseNetSessions.get(position).getProtocol() == Const.HTTPS) {
                    return;
                }
                startPacketDetailActivity(baseNetSessions.get(position));
            });


        }

        @Override
        public int getItemCount() {
            return baseNetSessions == null ? 0 : baseNetSessions.size();
        }

        class ConnectionHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView processName;
            TextView netState;
            TextView refreshTime;
            TextView size;
            TextView isSSL;
            TextView hostName;

            public ConnectionHolder(View view) {
                super(view);
                icon = view.findViewById(R.id.iv_icon);
                refreshTime = view.findViewById(R.id.refresh_time);
                size = view.findViewById(R.id.net_size);
                isSSL = view.findViewById(R.id.is_ssl);
                processName = view.findViewById(R.id.app_name);
                netState = view.findViewById(R.id.net_state);
                hostName = view.findViewById(R.id.url);
            }
        }
    }

    private void startPacketDetailActivity(NetSession connection) {
        String dir = Const.DATA_DIR
                + connection.getVpnStartTime()
                + "/"
                + connection.hashCode();
        PacketDetailActivity.startActivity(this, dir);

    }
}
