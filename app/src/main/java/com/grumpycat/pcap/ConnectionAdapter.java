package com.grumpycat.pcap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcaplib.appinfo.AppInfo;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcaplib.protocol.HttpHeader;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.StrUtil;

import java.util.List;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/2/28.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class ConnectionAdapter extends BaseAdapter {
    private final Context context;
    private List<NetSession> netConnections;

    ConnectionAdapter(Context context, List<NetSession> netConnections) {
        this.context = context;
        this.netConnections = netConnections;
    }

    public void setNetConnections(List<NetSession> netConnections) {
        this.netConnections = netConnections;
    }

    @Override
    public int getCount() {
        return netConnections==null?0:netConnections.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_connection, null);
            holder = new Holder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        NetSession connection = netConnections.get(position);
        AppInfo appInfo = AppManager.getApp(connection.getUid());

        if (appInfo != null) {
            holder.processName.setText(appInfo.name);
            holder.icon.setImageDrawable(appInfo.icon);
        }else {
            holder.processName.setText(context.getString(R.string.unknow));
            holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.sym_def_app_icon));
        }
        holder.isSSL.setVisibility(View.GONE);
        holder.hostName.setText(null);
        holder.hostName.setVisibility(View.GONE);
        if (connection.isTcp()) {
            if (connection.getProtocol() == Const.HTTPS) {
                holder.isSSL.setVisibility(View.VISIBLE);
            }

            HttpHeader httpHeader = connection.getHttpHeader();
            if(httpHeader != null){
                if (httpHeader.url != null) {
                    holder.hostName.setText(httpHeader.url);
                } else {
                    holder.hostName.setText(httpHeader.host);
                }
                if(httpHeader.url != null || httpHeader.host != null){
                    holder.hostName.setVisibility(View.VISIBLE);
                }
            }
        }



        holder.netState.setText(StrUtil.ip2Str(connection.getRemoteIp())+":"+connection.getRemotePort());
        holder.refreshTime.setText(StrUtil.formatHHMMSS(connection.getStartTime()));
        int sumByte = (int) (connection.sendByte + connection.receiveByte);

        String showSum;
        if (sumByte > 1000000) {
            showSum = String.valueOf((int) (sumByte / 1000000.0 + 0.5)) + "mb";
        } else if (sumByte > 1000) {
            showSum = String.valueOf((int) (sumByte / 1000.0 + 0.5)) + "kb";
        } else {
            showSum = String.valueOf(sumByte) + "b";
        }

        holder.size.setText(showSum);

        return convertView;
    }

    class Holder {
        ImageView icon;
        TextView processName;
        TextView netState;
        TextView refreshTime;
        TextView size;
        TextView isSSL;
        TextView hostName;
        View baseView;

        Holder(View view) {
            baseView = view;
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
