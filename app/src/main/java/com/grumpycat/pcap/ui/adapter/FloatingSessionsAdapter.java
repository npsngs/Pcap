package com.grumpycat.pcap.ui.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.CommonTool;
import com.grumpycat.pcap.ui.base.BaseAdapter;
import com.grumpycat.pcap.ui.base.BaseHolder;
import com.grumpycat.pcap.ui.base.BindDataGetter;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.util.StrUtil;

/**
 * Created by cc.he on 2018/12/6
 */
public class FloatingSessionsAdapter extends BaseAdapter<NetSession> {

    @Override
    protected int getItemLayoutRes(int viewType) {
        return R.layout.item_session;
    }

    @NonNull
    @Override
    public BaseHolder<NetSession> onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SessionItemHolder(createItemView(viewGroup, i));
    }


    private class SessionItemHolder extends BaseHolder<NetSession> {
        private TextView tv_tag;
        private TextView tv_time;
        private TextView tv_info;

        public SessionItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void initWithView(View itemView) {
            tv_tag = findViewById(R.id.tv_tag);
            tv_info = findViewById(R.id.tv_info);
            tv_time = findViewById(R.id.tv_time);
        }

        @Override
        protected void onBindData(BindDataGetter<NetSession> dataGetter) {
            int pos = getAdapterPosition();
            NetSession session = dataGetter.getItemData(pos);
            int protocol = session.getProtocol();
            CommonTool.setProtocolTag(protocol, tv_tag);
            tv_time.setText(String.format("[ %s ]",
                    StrUtil.formatYYMMDDHHMMSS(session.lastActiveTime)));
            tv_info.setText(session.getBriefInfo());

            itemView.setOnClickListener((view)-> onJump(session));
        }
    }

    protected void onJump(NetSession session){}
}

