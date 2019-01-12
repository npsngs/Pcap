package com.grumpycat.pcap.ui.floatwin;

import android.support.annotation.LayoutRes;
import android.view.View;

/**
 * Created by cc.he on 2018/12/6
 */
public abstract class PageUnit {
    private View pageRoot;
    protected PageHome home;
    PageUnit(PageHome home) {
        this.home = home;
    }

    public View getPageRoot() {
        return pageRoot;
    }

    public <T extends View> T findViewById(int id) {
        return pageRoot.findViewById(id);
    }
    public abstract @LayoutRes int getLayoutRes();
    public void onViewCreate(View pageRoot){
        this.pageRoot = pageRoot;
    }

    public void onStop(){}
    public void onStart(){}

    public void onLeftClick(){
        exit();
    }
    public void onRightClick(int num){}
    public void onExit(){
        home = null;
    }

    public void exit(){
        home.exitPage(this);
    }

    public void setLeftBtn(int resId) {
        home.setLeftBtn(resId);
    }

    public void setRightBtn(int resId, int num) {
        home.setRightBtn(resId, num);
    }

    public void setTitleStr(int resId) {
        home.setTitleStr(resId);
    }

    public void setTitleStr(String str) {
        home.setTitleStr(str);
    }

    public void goNextPage(PageUnit nextPage){
        home.goNextPage(this, nextPage);
    }
}
