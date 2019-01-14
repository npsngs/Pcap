package com.grumpycat.pcap.ui.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.model.AppSessions;
import com.grumpycat.pcap.tools.CommonTool;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.BaseHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcaplib.session.NetSession;

/**
 * Created by cc.he on 2018/12/6
 */
public class FloatingAppSessionAdapter extends BaseAdapter<AppSessions> {
    @Override
    protected int getItemLayoutRes(int viewType) {
        return R.layout.item_app_sessions_floating;
    }

    @NonNull
    @Override
    public BaseHolder<AppSessions> onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new AppSessionHolder(createItemView(viewGroup, i));
    }

    private class AppSessionHolder extends BaseHolder<AppSessions> {
        private ImageView iv_icon;
        private TextView tv_name;
        private TextView tv_info, tv_tag;
        private ImageView iv_upload, iv_download;
        private TextView tv_upload, tv_download;


        public AppSessionHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void initWithView(View itemView) {
            iv_icon = findViewById(R.id.iv_icon);
            tv_name = findViewById(R.id.tv_name);
            tv_info = findViewById(R.id.tv_info);
            tv_tag = findViewById(R.id.tv_tag);
            tv_upload = findViewById(R.id.tv_upload);
            tv_download = findViewById(R.id.tv_download);
            iv_upload = findViewById(R.id.iv_upload);
            iv_download = findViewById(R.id.iv_download);
            iv_upload.setEnabled(false);
            iv_download.setEnabled(false);
            tv_tag.setVisibility(View.GONE);
        }

        @Override
        protected void onBindData(BindDataGetter<AppSessions> dataGetter) {
            int pos = getAdapterPosition();
            AppSessions appSessions = dataGetter.getItemData(pos);
            AppManager.asyncLoad(appSessions.getUid(), appInfo -> {
                if(appInfo != null){
                    iv_icon.setImageDrawable(appInfo.icon);
                    tv_name.setText(appInfo.name);
                }else{
                    iv_icon.setImageResource(R.drawable.sym_def_app_icon);
                    tv_name.setText(R.string.unknow);
                }
            });

            tv_upload.setText(appSessions.getSendBytes()+"B");
            tv_download.setText(appSessions.getRecvBytes()+"B");

            NetSession lastSession = appSessions.getLastSession();


            if(lastSession != null){
                tv_info.setText(lastSession.getBriefInfo());
                int protocol = lastSession.getProtocol();
                CommonTool.setProtocolTag(protocol, tv_tag);
            }else{
                tv_info.setText("no session");
                tv_tag.setVisibility(View.GONE);
            }
            itemView.setOnClickListener((view)->onJump(appSessions.getUid()));
        }
    }

    protected void onJump(int uid){}
}

