package com.grumpycat.pcap.ui.floatwin;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.ActionDetector;
import com.grumpycat.pcap.tools.AppConfigs;
import com.grumpycat.pcap.tools.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cc.he on 2018/12/6
 */
public class PageHome {
    private Service service;
    private ViewGroup root;
    private ViewGroup container;
    private List<PageUnit> pages;
    private LayoutInflater inflater;
    private PageTitle pageTitle;
    public PageHome(Service service) {
        this.service = service;
    }

    private FloatingBtn floatingBtn;
    public void setFloatingBtn(FloatingBtn floatingBtn) {
        this.floatingBtn = floatingBtn;
    }


    public void init(){
        pages = new ArrayList<>();
        inflater = LayoutInflater.from(service);
        root = (ViewGroup) View.inflate(service, R.layout.float_page_home, null);
        container = root.findViewById(R.id.rl_container);
        pageTitle = new PageTitle(root) {
            @Override
            protected void onLeftClick() {
                getTopPage().onLeftClick();
            }

            @Override
            protected void onRightClick(int num) {
                getTopPage().onRightClick(num);
            }

            @Override
            protected void onPageMove(float dx, float dy) {
                layoutParams.x += dx;
                layoutParams.y += dy;
                wm.updateViewLayout(root, layoutParams);
            }

            @Override
            protected void onPageMoveEnded() {
                AppConfigs.setFloatPageLocation(layoutParams.x, layoutParams.y);
            }
        };

        View dragView = root.findViewById(R.id.iv_drag);
        dragView.setOnTouchListener(new ActionDetector(-1){
            @Override
            protected void onMove(float dx, float dy) {
                layoutParams.width += dx;
                layoutParams.height += dy;
                wm.updateViewLayout(root, layoutParams);
            }

            @Override
            protected void onMoveEnded() {
                AppConfigs.setFloatPageSize(layoutParams.width, layoutParams.height);
            }
        });

    }

    public void setLeftBtn(int resId) {
        pageTitle.setLeftBtn(resId);
    }

    public void setRightBtn(int resId, int num) {
        pageTitle.setRightBtn(resId, num);
    }

    public void setTitleStr(int resId) {
        pageTitle.setTitleStr(resId);
    }

    public void setTitleStr(String str) {
        pageTitle.setTitleStr(str);
    }

    public void goNextPage(PageUnit from, PageUnit nextPage){
        View view = inflater.inflate(nextPage.getLayoutRes(), root, false);
        nextPage.onViewCreate(view);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if(lp == null){
            lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(lp);
        }else{
            lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        }

        container.addView(view);
        nextPage.onStart();
        if(from != null){
            from.onStop();
        }
        pages.add(nextPage);
    }

    public void exitPage(PageUnit exitPage){
        if (pages.size() == 1) {
            hide();
            return;
        }
        int index = pages.indexOf(exitPage);
        int size = pages.size();
        for(int i = size-1; i>index-1;i--){
            PageUnit page = pages.remove(index);
            container.removeView(page.getPageRoot());
            exitPage.onStop();
            exitPage.onExit();
        }
        PageUnit startPage = pages.get(index-1);
        startPage.onStart();
    }

    public PageUnit getTopPage(){
       return pages.get(pages.size()-1);
    }


    private WindowManager wm;
    private WindowManager.LayoutParams layoutParams;
    public void show(){
        if (root == null){
            init();
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
            layoutParams.flags =  WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            DisplayMetrics dm = service.getResources().getDisplayMetrics();
            layoutParams.width = AppConfigs.getFloatPageWidth((int) (dm.widthPixels * 0.66f));
            layoutParams.height = AppConfigs.getFloatPageHeight((int) (dm.heightPixels * 0.4f));
            layoutParams.x = AppConfigs.getFloatPageX(Util.dp2px(service, 10f));
            layoutParams.y = AppConfigs.getFloatPageY(Util.dp2px(service, 30f));
            // 新建悬浮窗控件
            // 将悬浮窗控件添加到WindowManager
            wm.addView(root, layoutParams);

            goNextPage(null, new SessionsPage(this));
        }else{
            root.setVisibility(View.VISIBLE);
            getTopPage().onStart();
        }
    }

    public void hide(){
        getTopPage().onStop();
        root.setVisibility(View.GONE);
        floatingBtn.show();
    }

    public void close(){
        getTopPage().onStop();
        wm.removeView(root);
        wm = null;
        root = null;
        service = null;
    }

    public Service getService() {
        return service;
    }
}
