package com.grumpycat.pcap.ui.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.BaseHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcaplib.protocol.HttpHeader;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.StrUtil;

/**
 * Created by cc.he on 2018/12/6
 */
public class SessionsAdapter extends BaseAdapter<NetSession> {

    @Override
    protected int getItemLayoutRes() {
        return R.layout.item_session;
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

        public SessionItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void initWithView(View itemView) {
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
                        if (httpHeader.url != null) {
                            tv_address.setText(httpHeader.url);
                        } else if(httpHeader.host != null){
                            tv_address.setText(httpHeader.host);
                        }else{
                            tv_address.setText(String.format(Const.LOCALE,
                                    "%s:%d",
                                    StrUtil.ip2Str(session.getRemoteIp()),
                                    session.getRemotePort()));
                        }
                    }
                }break;
                case Const.TCP:
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

            itemView.setOnClickListener((view)->{
                /*String dir = Const.DATA_DIR
                        + StrUtil.formatYYMMDDHHMMSS(session.getVpnStartTime())
                        + "/"
                        + session.hashCode();*/
                onJump(session);
            });
        }
    }

    protected void onJump(NetSession session){}
}

