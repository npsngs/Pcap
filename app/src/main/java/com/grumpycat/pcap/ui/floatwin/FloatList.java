package com.grumpycat.pcap.ui.floatwin;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.SessionListener;
import com.grumpycat.pcap.tools.SessionSet;
import com.grumpycat.pcap.tools.Util;
import com.grumpycat.pcap.ui.base.BaseRecyclerAdapter;
import com.grumpycat.pcap.ui.base.BaseRecyclerViewHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcap.ui.base.ListDividerDrawable;
import com.grumpycat.pcaplib.protocol.HttpHeader;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.StrUtil;

/**
 * Created by cc.he on 2018/11/29
 */
public class FloatList implements View.OnClickListener{
    private Service service;
    private View root;
    private ImageView btn;
    private RecyclerView rcv;
    private SessionsAdapter adapter;
    public FloatList(Service service) {
        this.service = service;
        root = View.inflate(service, R.layout.float_session_list, null);
        int padding = Util.dp2px(service, 6.0f);
        root.setPadding(padding,padding,padding,padding);
        root.findViewById(R.id.iv_hide).setOnClickListener(this);
        root.findViewById(R.id.iv_close).setOnClickListener(this);

        rcv = root.findViewById(R.id.rcv);
        rcv.setLayoutManager(new LinearLayoutManager(service,
                LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration did = new DividerItemDecoration(service,
                DividerItemDecoration.VERTICAL);
        did.setDrawable(new ListDividerDrawable(
                Util.dp2px(service, 0.5f),
                0xffeeeeee));
        rcv.addItemDecoration(did);
        adapter = new SessionsAdapter();
        rcv.setAdapter(adapter);
    }

    public void show(){
        SessionSet.setSessionListener(sessionListener);
        SessionSet.setAppSessionListener(null);

        // 获取WindowManager服务
        WindowManager wm = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        // 设置LayoutParam
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags =  WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        DisplayMetrics dm = service.getResources().getDisplayMetrics();
        layoutParams.width = (int) (dm.widthPixels * 0.66f);
        layoutParams.height = (int) (dm.heightPixels * 0.4f);
        layoutParams.x = Util.dp2px(service, 10f);
        layoutParams.y = Util.dp2px(service, 30f);
        // 新建悬浮窗控件
        // 将悬浮窗控件添加到WindowManager
        wm.addView(root, layoutParams);
    }


    public void close(){
        SessionSet.setSessionListener(null);
        WindowManager wm = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        wm.removeView(root);
    }

    private void hide(){
        root.setVisibility(View.GONE);
        if(btn == null){
            btn = new ImageView(service);
            btn.setImageResource(R.drawable.ic_float_btn_sl);
            // 获取WindowManager服务
            WindowManager wm = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
            // 设置LayoutParam
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            layoutParams.flags =
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            layoutParams.width = Util.dp2px(service, 32f);
            layoutParams.height = Util.dp2px(service, 32f);
            layoutParams.x = Util.dp2px(service, -12f);
            layoutParams.y = Util.dp2px(service, 30f);
            // 新建悬浮窗控件
            // 将悬浮窗控件添加到WindowManager
            wm.addView(btn, layoutParams);
            btn.setOnClickListener(v -> {
                btn.setVisibility(View.GONE);
                root.setVisibility(View.VISIBLE);
            });

        }
        btn.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_hide:
                hide();
                break;
            case R.id.iv_close:
                service.stopSelf();
                break;
        }
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

    private class SessionsAdapter extends BaseRecyclerAdapter<NetSession> {
        @Override
        public View createItemView(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return inflater.inflate(R.layout.item_session_float, parent, false);
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
}
