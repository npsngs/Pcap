package com.grumpycat.pcap.ui.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.model.AppSessions;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.BaseHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcaplib.appinfo.AppInfo;
import com.grumpycat.pcaplib.util.Const;

/**
 * Created by cc.he on 2018/12/6
 */
public class AppSessionAdapter extends BaseAdapter<AppSessions> {
    @Override
    protected int getItemLayoutRes() {
        return R.layout.item_app_sessions;
    }

    @NonNull
    @Override
    public BaseHolder<AppSessions> onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new AppSessionHolder(createItemView(viewGroup, i));
    }

    private class AppSessionHolder extends BaseHolder<AppSessions> {
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

            tv_info.setText(String.format(Const.LOCALE, "s:%d  r:%d  session:%d  uid:%d",
                    appSessions.getSendBytes(),
                    appSessions.getRecvBytes(),
                    appSessions.getSessionCount(),
                    appSessions.getUid()));
            itemView.setOnClickListener((view)->onJump(appSessions.getUid()));
        }
    }

    protected void onJump(int uid){}
}

