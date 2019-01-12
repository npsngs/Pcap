package com.grumpycat.pcap.ui.adapter;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.BaseHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcaplib.appinfo.AppInfo;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcaplib.protocol.HttpHeader;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.StrUtil;

/**
 * Created by cc.he on 2018/12/6
 */
public class HistorySessionsAdapter extends BaseAdapter<NetSession> {

    @Override
    protected int getItemLayoutRes(int viewType) {
        return R.layout.item_history_session;
    }

    @NonNull
    @Override
    public BaseHolder<NetSession> onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SessionItemHolder(createItemView(viewGroup, i));
    }


    private class SessionItemHolder extends BaseHolder<NetSession> {
        private TextView tv_address;
        private TextView tv_info;
        private TextView tv_flag;
        private ImageView iv_icon;
        public SessionItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void initWithView(View itemView) {
            iv_icon = findViewById(R.id.iv_icon);
            tv_address = findViewById(R.id.tv_address);
            tv_info = findViewById(R.id.tv_info);
            tv_flag = findViewById(R.id.tv_flag);
        }

        @Override
        protected void onBindData(BindDataGetter<NetSession> dataGetter) {
            int pos = getAdapterPosition();
            NetSession session = dataGetter.getItemData(pos);
            int protocol = session.getProtocol();
            switch (protocol){
                case Const.HTTP:
                case Const.HTTPS: {
                    tv_flag.setText(protocol==Const.HTTP?"HTTP":"HTTPS");
                    HttpHeader httpHeader = session.getHttpHeader();
                    if(httpHeader != null){
                        if (!TextUtils.isEmpty(httpHeader.url)) {
                            tv_address.setText(httpHeader.url);
                        } else if(!TextUtils.isEmpty(httpHeader.host)){
                            tv_address.setText(httpHeader.host);
                        }else{
                            tv_address.setText(String.format(Const.LOCALE,
                                    "%s:%d",
                                    StrUtil.ip2Str(session.getRemoteIp()),
                                    session.getRemotePort()));
                        }
                    }else{
                        tv_address.setText(String.format(Const.LOCALE,
                                "%s:%d",
                                StrUtil.ip2Str(session.getRemoteIp()),
                                session.getRemotePort()));
                    }
                }break;
                default:
                    tv_flag.setText("TCP");
                    tv_address.setText(String.format(Const.LOCALE,
                            "%s:%d",
                            StrUtil.ip2Str(session.getRemoteIp()),
                            session.getRemotePort()));
                    break;
            }
            tv_info.setText(String.format(Const.LOCALE,
                    "s:%db   r:%db   UID:%d  ID:%d",
                    session.sendByte,
                    session.receiveByte,
                    session.getUid(),
                    session.hashCode()));
            AppInfo appInfo = AppManager.getApp(session.getUid());
            if(appInfo != null){
                iv_icon.setImageDrawable(appInfo.icon);
            }else{
                iv_icon.setImageResource(R.drawable.sym_def_app_icon);
            }

            itemView.setOnClickListener((view)->{
                String dir = Const.CACHE_DIR
                        + StrUtil.formatYYMMDD_HHMMSS(session.getVpnStartTime())
                        + "/"
                        + session.hashCode();
                onJump(dir);
            });
        }
    }

    protected void onJump(String dir){}
}

