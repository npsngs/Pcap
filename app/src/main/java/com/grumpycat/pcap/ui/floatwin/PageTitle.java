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
    private ImageView iv_right1,iv_right2;
    private TextView tv_title;
    private View title_bar;
    @SuppressLint("ClickableViewAccessibility")
    public PageTitle(View root) {
        this.title_bar = root.findViewById(R.id.title_bar);
        this.iv_left = root.findViewById(R.id.iv_left);
        this.iv_right1 = root.findViewById(R.id.iv_right1);
        this.iv_right2 = root.findViewById(R.id.iv_right2);
        this.tv_title = root.findViewById(R.id.tv_title);
        iv_left.setOnClickListener(this);
        iv_right1.setOnClickListener(this);
        iv_right2.setOnClickListener(this);
        title_bar.setOnTouchListener(new ActionDetector(-1) {
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
            case R.id.iv_right1:
                onRightClick(0);
                break;
            case R.id.iv_right2:
                onRightClick(1);
                break;
        }
    }

    public void setLeftBtn(@DrawableRes int resId){
        iv_left.setImageResource(resId);
    }

    public void setRightBtn(@DrawableRes int resId, int num){
        if ( num == 0){
            iv_right1.setVisibility(View.VISIBLE);
            iv_right1.setImageResource(resId);
        }else{
            iv_right1.setVisibility(View.VISIBLE);
            iv_right2.setVisibility(View.VISIBLE);
            iv_right2.setImageResource(resId);
        }
    }

    public void hideRightBtns(){
        iv_right2.setVisibility(View.GONE);
        iv_right1.setVisibility(View.GONE);
    }

    public void setTitleStr(@StringRes int resId){
        tv_title.setText(resId);
    }
    public void setTitleStr(String str){
        tv_title.setText(str);
    }

    protected abstract void onLeftClick();
    protected abstract void onRightClick(int num);
    protected abstract void onPageMove(float dx, float dy);
    protected abstract void onPageMoveEnded();

}
