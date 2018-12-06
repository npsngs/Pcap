package com.grumpycat.pcap.ui.floatwin;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.model.SessionListener;
import com.grumpycat.pcap.model.SessionSet;
import com.grumpycat.pcap.tools.AppConfigs;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.BaseHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcap.ui.base.SingleList;
import com.grumpycat.pcaplib.protocol.HttpHeader;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.StrUtil;

/**
 * Created by cc.he on 2018/12/6
 */
public class SessionsPage extends PageUnit {
    SessionsPage(PageHome home) {
        super(home);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.page_single_list;
    }

    private SingleList singleList;
    private SessionsAdapter adapter;
    @Override
    public void onViewCreate(View pageRoot) {
        super.onViewCreate(pageRoot);
        home.setTitleStr(R.string.net_session);
        singleList = new SingleList(pageRoot);
        adapter = new SessionsAdapter();
        singleList.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        SessionSet.addSessionListener(sessionListener);

    }

    @Override
    public void onStop() {
        SessionSet.removeSessionListener(sessionListener);
    }

    private SessionListener sessionListener = new SessionListener() {
        @Override
        public void onUpdate(NetSession session) {
            int sn = session.getSerialNumber();
            int size = adapter.getItemCount();
            adapter.notifyItemChanged(size - sn -1);
        }

        @Override
        public void onNewAdd(NetSession session) {
            adapter.add(session, 0);
        }

        @Override
        public void onClear() {
            adapter.removeAll();
        }
    };


    private class SessionsAdapter extends BaseAdapter<NetSession> {
        @Override
        protected int getItemLayoutRes() {
            return R.layout.item_session_float;
        }

        @NonNull
        @Override
        public BaseHolder<NetSession> onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new SessionItemHolder(createItemView(viewGroup, i));
        }
    }


    private class SessionItemHolder extends BaseHolder<NetSession> {
        private TextView tv_address;
        private TextView tv_info;

        public SessionItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void initWithView(View itemView) {
            tv_address = findViewById(R.id.tv_address);
            tv_info = findViewById(R.id.tv_info);
        }

        @Override
        protected void onBindData(BindDataGetter<NetSession> dataGetter) {
            int pos = getAdapterPosition();
            NetSession session = dataGetter.getItemData(pos);
            int protocol = session.getProtocol();
            String protStr = "";
            switch (protocol){
                case Const.HTTP:
                case Const.HTTPS: {
                    protStr = protocol==Const.HTTP?"HTTP":"HTTPS";
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
                    protStr = "TCP";
                    tv_address.setText(String.format(Const.LOCALE,
                            "%s:%d",
                            StrUtil.ip2Str(session.getRemoteIp()),
                            session.getRemotePort()));
                    break;
            }
            tv_info.setText(String.format(Const.LOCALE,
                    "[%s] s:%db   r:%db   UID:%d",
                    protStr,
                    session.sendByte,
                    session.receiveByte,
                    session.getUid()));

            itemView.setOnClickListener((view)->{
                /*String dir = Const.DATA_DIR
                        + StrUtil.formatYYMMDDHHMMSS(session.getVpnStartTime())
                        + "/"
                        + session.hashCode();
                PacketDetailActivity.startActivity(s, dir);*/
            });
        }
    }

    @Override
    public void onRightClick() {
        AppConfigs.setShowFloating(false);
        home.getService().stopSelf();
    }
}
