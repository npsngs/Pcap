package com.grumpycat.pcaplib.session;


import com.grumpycat.pcaplib.VpnMonitor;
import com.grumpycat.pcaplib.data.FileCache;
import com.grumpycat.pcaplib.util.Const;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cc.he on 2018/11/14
 */
public class SessionManager {
    private static int maxSessionCount = Const.SESSION_MAX_COUNT;
    private static long maxSessionTimeout = Const.SESSION_MAX_TIMEOUT;
    private static final ConcurrentHashMap<Integer, NetSession> sessions = new ConcurrentHashMap<>();

    public static NetSession getSession(short portKey) {
        return getSession(portKey & 0xFFFF);
    }

    public static NetSession getSession(int portKey) {
        return sessions.get(portKey);
    }


    /*清除过期的会话*/
    public static void clearExpiredSessions() {
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

    public static void clearAllSession() {
        sessions.clear();
    }

    public static List<NetSession> getAllSession() {
        ArrayList<NetSession> natSessions = new ArrayList<>();
        Set<Map.Entry<Integer, NetSession>> entries = sessions.entrySet();
        Iterator<Map.Entry<Integer, NetSession>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, NetSession> next = iterator.next();
            natSessions.add(next.getValue());
        }
        return natSessions;
    }

    public static List<NetSession> loadAllSession() {
        String lastVpnStartTimeFormat = VpnMonitor.getVpnStartTime();
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
    public static NetSession createSession(short portKey, int remoteIP, short remotePort, int protocol) {
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
        sessions.put(session.getPortKey(), session);
        return session;
    }

    public static void removeSession(short portKey) {
        sessions.remove(portKey & 0xFFFF);
    }
}
