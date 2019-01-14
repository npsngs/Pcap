package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.Util;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcap.ui.base.UiWidget;

/**
 * Created by cc.he on 2018/11/27
 */
public class AppInfoBar extends UiWidget{
    private ImageView iv_icon;
    private TextView tv_app_name;
    private LinearLayout ll_icons;

    public AppInfoBar(Activity activity) {
        super(activity);
        iv_icon = findViewById(R.id.iv_icon);
        tv_app_name = findViewById(R.id.tv_apps_title);
        ll_icons = findViewById(R.id.ll_icons);
    }


    public void setAppUid(int[] uids){
        if(uids == null || uids.length == 0){
            iv_icon.setImageResource(R.drawable.ic_widgets);
            tv_app_name.setText(R.string.all_app);
            ll_icons.setVisibility(View.GONE);
        }else{
            if(uids.length == 1){
                AppManager.asyncLoad(uids[0], appInfo -> {
                    iv_icon.setImageDrawable(appInfo.icon);
                    tv_app_name.setText(appInfo.name);
                    ll_icons.setVisibility(View.GONE);
                });
            }else{
                if(uids.length > 5){
                    tv_app_name.setText("...");
                }else{
                    tv_app_name.setText("");
                }
                AppManager.asyncLoad(uids[0], appInfo -> iv_icon.setImageDrawable(appInfo.icon));
                ll_icons.setVisibility(View.VISIBLE);
                ll_icons.removeAllViews();
                for(int i = 1;i<5 && i<uids.length ;i++){
                    ImageView iv = new ImageView(getActivity());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            Util.dp2px(getActivity(), 20f),
                            Util.dp2px(getActivity(), 20f));
                    lp.leftMargin = Util.dp2px(getActivity(), 6f);

                    AppManager.asyncLoad(uids[i], appInfo ->{
                        iv.setImageDrawable(appInfo.icon);
                        ll_icons.addView(iv, lp);
                    });
                }
            }
        }
    }
}
