package com.grumpycat.pcap.ui.floatwin;

import android.annotation.SuppressLint;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.ActionDetector;

/**
 * Created by cc.he on 2018/12/6
 */
public abstract class PageTitle implements View.OnClickListener{
    private ImageView iv_left;
    private ImageView iv_right;
    private TextView tv_title;

    @SuppressLint("ClickableViewAccessibility")
    public PageTitle(View root) {
        this.iv_left = root.findViewById(R.id.iv_left);
        this.iv_right = root.findViewById(R.id.iv_right);
        this.tv_title = root.findViewById(R.id.tv_title);
        iv_left.setOnClickListener(this);
        iv_right.setOnClickListener(this);
        tv_title.setOnTouchListener(new ActionDetector(-1) {
            @Override
            protected void onMove(float dx, float dy) {
                onPageMove(dx, dy);
            }
            @Override
            protected void onMoveEnded() {
                onPageMoveEnded();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_left:
                onLeftClick();
                break;
            case R.id.iv_right:
                onRightClick();
                break;
        }
    }

    public void setLeftBtn(@DrawableRes int resId){
        iv_left.setImageResource(resId);
    }

    public void setRightBtn(@DrawableRes int resId){
        iv_right.setImageResource(resId);
    }
    public void setTitleStr(@StringRes int resId){
        tv_title.setText(resId);
    }
    public void setTitleStr(String str){
        tv_title.setText(str);
    }

    protected abstract void onLeftClick();
    protected abstract void onRightClick();
    protected abstract void onPageMove(float dx, float dy);
    protected abstract void onPageMoveEnded();

}
