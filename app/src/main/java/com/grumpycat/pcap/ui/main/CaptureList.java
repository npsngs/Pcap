package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcap.PacketDetailActivity;
import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.AppSessionListener;
import com.grumpycat.pcap.tools.AppSessions;
import com.grumpycat.pcap.tools.SessionSet;
import com.grumpycat.pcap.tools.SessionListener;
import com.grumpycat.pcap.ui.base.BaseRecyclerAdapter;
import com.grumpycat.pcap.ui.base.BaseRecyclerViewHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcap.ui.base.ListDividerDrawable;
import com.grumpycat.pcap.ui.base.UiWidget;
import com.grumpycat.pcap.tools.Util;
import com.grumpycat.pcaplib.appinfo.AppInfo;
import com.grumpycat.pcaplib.protocol.HttpHeader;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.StrUtil;

/**
 * Created by cc.he on 2018/11/27
 */
public class CaptureList extends UiWidget{
    private RecyclerView rcv;
    private SessionManager sm;
    private CaptureAdapter captureAdapter;
    private AppSessionAdapter appSessionAdapter;
    private RecyclerView.Adapter adapter;
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
        captureAdapter = new CaptureAdapter();
        appSessionAdapter = new AppSessionAdapter();

        sm = SessionManager.getInstance();
        sm.init(activity.getApplication());
        SessionSet.setSessionListener(sessionListener);
        SessionSet.setAppSessionListener(appSessionListener);
    }

    public void onResume(){
        SessionSet.setSessionListener(sessionListener);
        SessionSet.setAppSessionListener(appSessionListener);
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }


    public void showSingleApp(int uid){
        SessionSet.setSingleApp(uid);
        adapter = captureAdapter;
        rcv.setAdapter(adapter);
        captureAdapter.setData(SessionSet.getSessionByUid(uid));

    }

    public void showMultiApp(){
        SessionSet.setMultiApp();
        adapter = appSessionAdapter;
        rcv.setAdapter(adapter);
        appSessionAdapter.setData(SessionSet.getAppSessions());
    }


    private SessionListener sessionListener = new SessionListener() {
        @Override
        public void onUpdate(NetSession session) {
            int sn = session.getSerialNumber();
            int size = captureAdapter.getItemCount();
            captureAdapter.notifyItemChanged(size - sn -1);
        }

        @Override
        public void onNewAdd(NetSession session) {
            captureAdapter.add(session, 0);
        }

        @Override
        public void onClear() {
            captureAdapter.removeAll();
        }
    };

    private AppSessionListener appSessionListener = new AppSessionListener() {
        @Override
        public void onUpdate(AppSessions session) {
            int sn = session.getSerialNumber();
            int size = appSessionAdapter.getItemCount();
            appSessionAdapter.notifyItemChanged(size - sn -1);
        }

        @Override
        public void onNewAdd(AppSessions session) {
            appSessionAdapter.add(session, 0);
        }

        @Override
        public void onClear() {
            appSessionAdapter.removeAll();
        }
    };



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
                    "s:%db   r:%db   UID:%d",
                    session.sendByte,
                    session.receiveByte,
                    session.getUid()));

            itemView.setOnClickListener((view)->{
                String dir = Const.DATA_DIR
                        + StrUtil.formatYYMMDDHHMMSS(session.getVpnStartTime())
                        + "/"
                        + session.hashCode();
                PacketDetailActivity.startActivity(getActivity(), dir);
            });
        }
    }


    private class AppSessionAdapter extends BaseRecyclerAdapter<AppSessions>{
        @Override
        public View createItemView(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return inflater.inflate(R.layout.item_app_sessions, parent, false);
        }

        @NonNull
        @Override
        public BaseRecyclerViewHolder<AppSessions> onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new AppSessionHolder(createItemView(viewGroup, i));
        }
    }

    private class AppSessionHolder extends BaseRecyclerViewHolder<AppSessions>{
        private ImageView iv_icon;
        private TextView tv_name;
        private TextView tv_info;

        public AppSessionHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void initWithView(View itemView) {
            iv_icon = findViewById(R.id.iv_icon);
            tv_name = findViewById(R.id.tv_name);
            tv_info = findViewById(R.id.tv_info);
        }

        @Override
        protected void onBindData(BindDataGetter<AppSessions> dataGetter) {
            int pos = getAdapterPosition();
            AppSessions appSessions = dataGetter.getItemData(pos);
            AppInfo appInfo = appSessions.getAppInfo();
            if(appInfo != null){
                iv_icon.setImageDrawable(appSessions.getAppInfo().icon);
                tv_name.setText(appSessions.getAppInfo().name);
            }else{
                iv_icon.setImageResource(R.drawable.sym_def_app_icon);
                tv_name.setText(R.string.unknow);
            }

            tv_info.setText(String.format(Const.LOCALE, "s:%d  r:%d  session:%d",
                    appSessions.getSendBytes(),
                    appSessions.getRecvBytes(),
                    appSessions.getSessionCount()));
            itemView.setOnClickListener((view)->
                    SessionListActivity.gotoActi(getActivity(), appSessions.getUid()));
        }
    }



}
