package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grumpycat.pcap.PacketDetailActivity;
import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.SessionSet;
import com.grumpycat.pcap.tools.SessionListener;
import com.grumpycat.pcap.ui.base.BaseRecyclerAdapter;
import com.grumpycat.pcap.ui.base.BaseRecyclerViewHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcap.ui.base.ListDividerDrawable;
import com.grumpycat.pcap.ui.base.UiWidget;
import com.grumpycat.pcap.ui.base.Util;
import com.grumpycat.pcaplib.protocol.HttpHeader;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.session.SessionObserver;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.StrUtil;

/**
 * Created by cc.he on 2018/11/27
 */
public class CaptureList extends UiWidget implements SessionListener{
    private RecyclerView rcv;
    private SessionManager sm;
    private CaptureAdapter adapter;
    public CaptureList(Activity activity) {
        super(activity);
        rcv = findViewById(R.id.rcv);
        rcv.setLayoutManager(new LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration did = new DividerItemDecoration(activity,
                DividerItemDecoration.VERTICAL);
        did.setDrawable(new ListDividerDrawable(
                Util.dp2px(activity, 1f),
                0xffeeeeee));
        rcv.addItemDecoration(did);
        adapter = new CaptureAdapter();
        rcv.setAdapter(adapter);

        sm = SessionManager.getInstance();
        sm.init(activity.getApplication());
        SessionSet.setSessionListener(this);
        sm.setObserver((session, event) -> {
            if(event != SessionObserver.EVENT_CLOSE){
                activity.runOnUiThread(()-> SessionSet.insertOrUpdate(session));
            }
            Log.e("udi", ""+session.getUid());
        });
    }

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


    private class CaptureAdapter extends BaseRecyclerAdapter<NetSession>{
        @Override
        public View createItemView(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return inflater.inflate(R.layout.item_session, parent, false);
        }

        @NonNull
        @Override
        public BaseRecyclerViewHolder<NetSession> onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new SessionItemHolder(createItemView(viewGroup, i));
        }
    }

    private class SessionItemHolder extends BaseRecyclerViewHolder<NetSession>{
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
                    "s:%db   r:%db",session.sendByte, session.receiveByte));

            itemView.setOnClickListener((view)->{
                String dir = Const.DATA_DIR
                        + StrUtil.formatYYMMDDHHMMSS(session.getVpnStartTime())
                        + "/"
                        + session.hashCode();
                PacketDetailActivity.startActivity(getActivity(), dir);
            });
        }
    }

}
