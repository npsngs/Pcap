package com.grumpycat.pcap.ui.base;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcap.R;

/**
 * Created by cc.he on 2018/12/6
 */
public class TitleBar extends UiWidget implements View.OnClickListener{
    private TextView tv_title;
    public TitleBar(Activity activity) {
        super(activity);
        tv_title = findViewById(R.id.tv_title);
        findViewById(R.id.iv_title_left).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_title_left:
                onLeftClick();
                break;
        }
    }

    protected void onLeftClick(){
        getActivity().finish();
    }

    public void setTitleStr(@StringRes int resId){
        tv_title.setText(resId);
    }
    public void setTitleStr(String str){
        tv_title.setText(str);
    }
}
