package com.grumpycat.pcap.tools;
/**
 * Created by cc.he on 2018/11/29
 */
public interface AppSessionListener {
    void onUpdate(AppSessions session);
    void onNewAdd(AppSessions session);
    void onClear();
}