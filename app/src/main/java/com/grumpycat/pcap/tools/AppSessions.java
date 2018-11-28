package com.grumpycat.pcap.tools;

import com.grumpycat.pcaplib.appinfo.AppInfo;

/**
 * Created by cc.he on 2018/11/28
 */
public class AppSessions {
    private AppInfo appInfo;
    private int sessionCount;
    private int sendBytes;
    private int recvBytes;
    private int sendPackets;
    private int recvPackets;

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }

    public int getSendBytes() {
        return sendBytes;
    }

    public void setSendBytes(int sendBytes) {
        this.sendBytes = sendBytes;
    }

    public int getRecvBytes() {
        return recvBytes;
    }

    public void setRecvBytes(int recvBytes) {
        this.recvBytes = recvBytes;
    }

    public int getSendPackets() {
        return sendPackets;
    }

    public void setSendPackets(int sendPackets) {
        this.sendPackets = sendPackets;
    }

    public int getRecvPackets() {
        return recvPackets;
    }

    public void setRecvPackets(int recvPackets) {
        this.recvPackets = recvPackets;
    }
}
