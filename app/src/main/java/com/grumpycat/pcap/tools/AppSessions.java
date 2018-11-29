package com.grumpycat.pcap.tools;

import android.util.SparseArray;

import com.grumpycat.pcaplib.appinfo.AppInfo;
import com.grumpycat.pcaplib.appinfo.AppManager;
import com.grumpycat.pcaplib.session.NetSession;

/**
 * Created by cc.he on 2018/11/28
 */
public class AppSessions {
    private int uid;
    private AppInfo appInfo;
    private int sendBytes;
    private int recvBytes;
    private int sendPackets;
    private int recvPackets;
    private int serialNumber;
    private SparseArray<NetSession> sessions;

    public AppSessions() {
        sessions = new SparseArray<>();
        sendBytes = 0;
        recvBytes = 0;
        sendPackets = 0;
        recvPackets = 0;
    }

    public void put(int key, NetSession value) {
        sessions.put(key, value);
        sendBytes += value.sendByte;
        sendPackets += value.sendPacket;
        recvBytes += value.receiveByte;
        recvPackets += value.receivePacket;
    }

    public NetSession valueAt(int index) {
        return sessions.valueAt(index);
    }

    public int size() {
        return sessions.size();
    }

    public void setUid(int uid) {
        this.uid = uid;
        appInfo = AppManager.getApp(uid);
    }

    public NetSession get(int id){
        return sessions.get(id);
    }

    public void clear(){
        sessions.clear();
        sendBytes = 0;
        recvBytes = 0;
        sendPackets = 0;
        recvPackets = 0;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public int getUid() {
        return uid;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public int getSessionCount() {
        return size();
    }

    public int getSendBytes() {
        return sendBytes;
    }

    public int getRecvBytes() {
        return recvBytes;
    }

    public int getSendPackets() {
        return sendPackets;
    }

    public int getRecvPackets() {
        return recvPackets;
    }
}
