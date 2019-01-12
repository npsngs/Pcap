package com.grumpycat.pcap.model;

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
    private NetSession lastSession;
    public AppSessions() {
        sessions = new SparseArray<>();
        sendBytes = 0;
        recvBytes = 0;
        sendPackets = 0;
        recvPackets = 0;
    }

    public boolean contains(int key){
       return null != sessions.get(key);
    }

    public NetSession insertOrUpdate(int key, NetSession session){
        lastSession = session;
        NetSession localSession = sessions.get(key);
        if(localSession != null){
            sendBytes -= localSession.sendByte;
            sendPackets -= localSession.sendPacket;
            recvBytes -= localSession.receiveByte;
            recvPackets -= localSession.receivePacket;

            sendBytes += session.sendByte;
            sendPackets += session.sendPacket;
            recvBytes += session.receiveByte;
            recvPackets += session.receivePacket;

            localSession.set(session);
        }else{
            localSession = session.copy();
            localSession.setSerialNumber(sessions.size());
            sessions.put(key, localSession);

            sendBytes += localSession.sendByte;
            sendPackets += localSession.sendPacket;
            recvBytes += localSession.receiveByte;
            recvPackets += localSession.receivePacket;
        }
        return localSession;
    }

    public NetSession getLastSession() {
        return lastSession;
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
