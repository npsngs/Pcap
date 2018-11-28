package com.grumpycat.pcaplib.session;


import android.content.Context;

import com.grumpycat.pcaplib.VpnController;
import com.grumpycat.pcaplib.VpnMonitor;
import com.grumpycat.pcaplib.data.FileCache;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.ThreadPool;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by cc.he on 2018/11/14
 */
public class SessionManager implements Closeable{
    private static SessionManager instance = new SessionManager();
    public static SessionManager getInstance(){
        return instance;
    }
    private int maxSessionCount = Const.SESSION_MAX_COUNT;
    private long maxSessionTimeout = Const.SESSION_MAX_TIMEOUT;
    private SessionDB sessionDB;
    private volatile SessionObserver observer;
    private ConcurrentHashMap<Integer, NetSession> sessions = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<NetSession> saveQueue = new ConcurrentLinkedQueue<>();
    public void init(Context context){
        sessionDB = new SessionDB(context);
    }

    public void reset(){
        /*VPN 重启*/
    }

    public NetSession getSession(short portKey) {
        return getSession(portKey & 0xFFFF);
    }
    public NetSession getSession(int portKey) {
        return sessions.get(portKey);
    }

    public void setObserver(SessionObserver observer) {
        this.observer = observer;
    }

    public void moveToSaveQueue(NetSession session){
        if(session == null || !session.hasData())return;
        NetSession ss = sessions.get(session.getPortKey());
        if(session.equals(ss)){
            sessions.remove(ss.getPortKey());
        }

        if(session.isUdp() && !VpnController.isUdpNeedSave()){
            return;
        }
        saveQueue.add(session);
        if(observer != null && !isFilter(session)){
            observer.onSessionChange(session, SessionObserver.EVENT_CLOSE);
        }

        if(saveQueue.size() >= Const.SESSION_MAX_SAVE_QUEUE){
            saveToDB();
        }
    }

    private void saveToDB(){
        ThreadPool.execute(() -> {
            List<NetSession> lt = new ArrayList<>(saveQueue.size());
            Iterator<NetSession> iterator = saveQueue.iterator();
            while (iterator.hasNext()){
                lt.add(iterator.next());
            }
            saveQueue.clear();
            sessionDB.insertAll(lt);
        });
    }

    /*清除过期的会话*/
    public void clearExpiredSessions() {
        long now = System.currentTimeMillis();
        Set<Map.Entry<Integer, NetSession>> entries = sessions.entrySet();
        Iterator<Map.Entry<Integer, NetSession>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, NetSession> entry = iterator.next();
            NetSession session = entry.getValue();
            if (now - session.lastActiveTime > maxSessionTimeout) {
                iterator.remove();
            }
        }
    }

    public List<NetSession> getAllSession() {
        ArrayList<NetSession> natSessions = new ArrayList<>();
        Set<Map.Entry<Integer, NetSession>> entries = sessions.entrySet();
        Iterator<Map.Entry<Integer, NetSession>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, NetSession> next = iterator.next();
            natSessions.add(next.getValue());
        }
        return natSessions;
    }


    public void addSessionReadBytes(NetSession session, int byteCount){
        session.receiveByte += byteCount;
        session.receivePacket++;
        if(observer != null && !isFilter(session)){
            observer.onSessionChange(session, SessionObserver.EVENT_RECV);
        }
    }

    public void addSessionSendBytes(NetSession session, int byteCount){
        session.sendByte += byteCount;
        session.sendPacket++;
        if(observer != null  && !isFilter(session)){
            observer.onSessionChange(session, SessionObserver.EVENT_SEND);
        }
    }

    public List<NetSession> loadAllSession() {
        String lastVpnStartTimeFormat = VpnMonitor.getVpnStartTimeStr();
        try {
            File file = new File(Const.CONFIG_DIR + lastVpnStartTimeFormat);
            FileCache aCache = FileCache.get(file);
            String[] list = file.list();
            ArrayList<NetSession> baseNetSessions = new ArrayList<>();
            if (list != null) {
                for (String fileName : list) {
                    NetSession netConnection = (NetSession) aCache.getAsObject(fileName);
                    baseNetSessions.add(netConnection);
                }
            }

            Collections.sort(baseNetSessions, new NetSession.SessionComparator());
            return baseNetSessions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 创建会话
     * @param portKey    源端口
     * @param remoteIP   远程ip
     * @param remotePort 远程端口
     * @return NatSession对象
     */
    public NetSession createSession(short portKey, int remoteIP, short remotePort, int protocol) {
        if (sessions.size() > maxSessionCount) {
            clearExpiredSessions();
        }

        NetSession session = new NetSession(protocol);
        session.lastActiveTime = System.currentTimeMillis();
        session.sendByte = 0;
        session.sendPacket = 0;
        session.receiveByte = 0;
        session.receivePacket = 0;
        session.setVpnStartTime(VpnMonitor.getVpnStartTime());
        session.setStartTime(session.lastActiveTime);
        session.setRemoteIp(remoteIP);
        session.setRemotePort(remotePort & 0xFFFF);
        session.setPortKey(portKey & 0xFFFF);
        NetSession lastSession = sessions.put(session.getPortKey(), session);
        if(lastSession != null){
            moveToSaveQueue(lastSession);
        }

        if(observer != null && !isFilter(session)){
            observer.onSessionChange(session, SessionObserver.EVENT_CREATE);
        }
        return session;
    }

    public boolean isFilter(NetSession session){
        if(!VpnController.isUdpNeedSave()
                && session.isUdp()){
            return true;
        }
        if(!session.hasData()){
            return true;
        }

        return false;
    }

    @Override
    public void close() throws IOException {
        saveToDB();
    }
}
