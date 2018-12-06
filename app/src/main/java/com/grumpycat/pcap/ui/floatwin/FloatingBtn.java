package com.grumpycat.pcap.ui.floatwin;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.ActionDetector;
import com.grumpycat.pcap.tools.AppConfigs;
import com.grumpycat.pcap.tools.Util;

/**
 * Created by cc.he on 2018/12/3
 */
public class FloatingBtn {
    private Service service;
    private ImageView btn;
    private PageHome floatingPage;
    private WindowManager.LayoutParams layoutParams;
    private WindowManager wm;
    private int screenW;
    public FloatingBtn(Service service) {
        this.service = service;
    }

    public void setFloatingPage(PageHome floatingPage) {
        this.floatingPage = floatingPage;
    }


    @SuppressLint("ClickableViewAccessibility")
    public void show(){
        if(btn == null){
            screenW = service.getResources().getDisplayMetrics().widthPixels;
            btn = new ImageView(service);
            btn.setImageResource(R.drawable.ic_float_btn_sl);
            // 获取WindowManager服务
            wm = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
            // 设置LayoutParam
            layoutParams = new WindowManager.LayoutParams();
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

            if(AppConfigs.isFloatBtnLeftSide()){
                layoutParams.x = Util.dp2px(service, -16f);
            }else{
                layoutParams.x = screenW + Util.dp2px(service, -16f);
            }
            layoutParams.y = AppConfigs.getFloatBtnY(Util.dp2px(service, 30f));
            // 新建悬浮窗控件
            // 将悬浮窗控件添加到WindowManager
            wm.addView(btn, layoutParams);
            btn.setOnTouchListener(new ActionDetector(Util.dp2px(service, 3)){
                @Override
                protected void onMove(float dx, float dy){
                    layoutParams.x += dx;
                    layoutParams.y += dy;
                    wm.updateViewLayout(btn, layoutParams);
                }

                @Override
                protected void onClick(){
                    btn.setVisibility(View.GONE);
                    floatingPage.show();
                }

                @Override
                protected void onMoveEnded(){
                    animateBackSide();
                }
            });
        }else{
            btn.setVisibility(View.VISIBLE);
        }
    }


    private void animateBackSide(){
        int targetX;
        long duration;
        if(layoutParams.x > screenW/2){
            targetX = screenW + Util.dp2px(service, -16f);
            duration = (targetX - layoutParams.x)*600/screenW;
            AppConfigs.setFloatBtnLocation(layoutParams.y, false);
        }else{
            targetX = Util.dp2px(service, -16f);
            duration = (layoutParams.x - targetX)*600/screenW;
            AppConfigs.setFloatBtnLocation(layoutParams.y, true);
        }

       ValueAnimator animator = ValueAnimator.ofInt(layoutParams.x, targetX)
                .setDuration(duration);
        animator.addUpdateListener(animation -> {
            int x = (int) animation.getAnimatedValue();
            layoutParams.x = x;
            wm.updateViewLayout(btn, layoutParams);
        });
        animator.start();
    }

    public void close(){
        if(btn != null){
            service = null;
            wm.removeView(btn);
        }
    }
}
