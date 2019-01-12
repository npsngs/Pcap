package com.grumpycat.pcap.tools;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcaplib.util.Const;

public class CommonTool {
    public static void setProtocolTag(int protocol, TextView tv_tag){
        Resources res = tv_tag.getResources();
        switch (protocol) {
            case Const.HTTP:
                tv_tag.setVisibility(View.VISIBLE);
                tv_tag.setBackgroundResource(R.drawable.sp_http_tag_bg);
                tv_tag.setTextColor(res.getColor(R.color.http_tag));
                tv_tag.setText("HTTP");
                break;
            case Const.HTTPS:
                tv_tag.setVisibility(View.VISIBLE);
                tv_tag.setBackgroundResource(R.drawable.sp_ssl_tag_bg);
                tv_tag.setTextColor(res.getColor(R.color.ssl_tag));
                tv_tag.setText("SSL");
                break;
            case Const.TCP:
                tv_tag.setVisibility(View.VISIBLE);
                tv_tag.setBackgroundResource(R.drawable.sp_tcp_tag_bg);
                tv_tag.setTextColor(res.getColor(R.color.tcp_tag));
                tv_tag.setText("TCP");
                break;
            case Const.UDP:
                tv_tag.setVisibility(View.VISIBLE);
                tv_tag.setBackgroundResource(R.drawable.sp_udp_tag_bg);
                tv_tag.setTextColor(res.getColor(R.color.udp_tag));
                tv_tag.setText("UDP");
                break;
            default:
                tv_tag.setVisibility(View.GONE);
                break;
        }
    }
}
