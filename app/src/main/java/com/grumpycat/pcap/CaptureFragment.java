package com.grumpycat.pcap;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.grumpycat.pcaplib.appinfo.AppInfo;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcaplib.VpnMonitor;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.ThreadProxy;

import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/5.
 *         Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class CaptureFragment extends BaseFragment {
    private static final String TAG = "CaptureFragment";

    private ScheduledExecutorService timer;
    private Handler handler;
    //private TextView summerState;
    private ConnectionAdapter connectionAdapter;
    private ListView channelList;

    private List<NetSession> allNetConnection;
    private Context context;

    @Override
    int getLayout() {
        return R.layout.fragment_capture;
    }


    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        super.onDestroyView();
        cancelTimer();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        context = getContext();


        handler = new Handler();
        channelList = view.findViewById(R.id.channel_list);

        channelList.setOnItemClickListener((parent, view1, position, id) -> {
            if (allNetConnection == null) {
                return;
            }
            if (position > allNetConnection.size() - 1) {
                return;
            }
            NetSession connection = allNetConnection.get(position);
            /*if (connection.getProtocol() == Const.HTTPS || connection.isUdp()) {
                return;
            }*/
            if (connection.isUdp()) {
                return;
            }

            String dir = Const.DATA_DIR
                    + VpnMonitor.getVpnStartTime()
                    + "/"
                    + connection.hashCode();
            PacketDetailActivity.startActivity(getActivity(), dir);
        });
       /* LocalBroadcastManager.getInstance(getContext()).registerReceiver(vpnStateReceiver,
                new IntentFilter(LocalVPNService.BROADCAST_VPN_STATE));*/
        startTimer();

        getDataAndRefreshView();

    }

    private void getDataAndRefreshView() {
        if (!VpnMonitor.isVpnRunning()) {
            return;
        }
        ThreadProxy.getInstance().execute(() -> {
            allNetConnection = SessionManager.loadAllSession();
            if (allNetConnection == null) {
                handler.post(() -> refreshView(allNetConnection));
                return;
            }

            if(allNetConnection.size() == 0){
                return;
            }


            Iterator<NetSession> iterator = allNetConnection.iterator();
            String packageName = context.getPackageName();

            SharedPreferences sp = getContext().getSharedPreferences(Const.VPN_SP_NAME, Context.MODE_PRIVATE);
            boolean isShowUDP = sp.getBoolean(Const.IS_UDP_SHOW, false);
            String selectPackage = sp.getString(Const.DEFAULT_PACKAGE_ID, null);
            while (iterator.hasNext()) {
                NetSession next = iterator.next();
                if (next.sendByte == 0 && next.receiveByte == 0) {
                    iterator.remove();
                    continue;
                }
                if (next.isUdp() && !isShowUDP) {
                    iterator.remove();
                    continue;
                }

                int uid = next.getUid();
                AppInfo appInfo = AppManager.getApp(uid);
                if (appInfo != null) {
                    if (packageName.equals(appInfo.pkgName) ) {
                        iterator.remove();
                        continue;
                    }
                    if((selectPackage != null && !selectPackage.equals(appInfo.pkgName))){
                        iterator.remove();
                    }
                }
            }
            if (handler == null) {
                return;
            }
            handler.post(() -> refreshView(allNetConnection));
        });
    }


    private void startTimer() {
        timer = Executors.newSingleThreadScheduledExecutor();

        timer.scheduleAtFixedRate(new TimerTask() {


            @Override
            public void run() {
                getDataAndRefreshView();
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void refreshView(List<NetSession> allNetConnection) {
        if (connectionAdapter == null) {
            connectionAdapter = new ConnectionAdapter(context, allNetConnection);
            channelList.setAdapter(connectionAdapter);
        } else {
            connectionAdapter.setNetConnections(allNetConnection);
            if (channelList.getAdapter() == null) {
                channelList.setAdapter(connectionAdapter);
            }
            connectionAdapter.notifyDataSetChanged();
        }


    }

    private void cancelTimer() {
        if (timer == null) {
            return;
        }
        timer.shutdownNow();
        timer = null;
    }


}

