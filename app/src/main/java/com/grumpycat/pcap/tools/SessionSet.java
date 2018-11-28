package com.grumpycat.pcap.tools;

import android.util.SparseArray;

import com.grumpycat.pcaplib.session.NetSession;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cc.he on 2018/11/28
 */
public class SessionSet {
    private static WeakReference<SessionListener> listenerWeakRef;
    private static SparseArray<SparseArray<NetSession>> appSets = new SparseArray<>();

    public static void clear(){
        int size = appSets.size();
        for(int i=0; i<size; i++){
            SparseArray<NetSession> set = appSets.valueAt(i);
            if(set != null){
                set.clear();
            }
        }

        SessionListener listener = getListener();
        if(listener != null){
            listener.onClear();
        }
    }

    public static void insertOrUpdate(NetSession session){
        int id = session.hashCode();
        SparseArray<NetSession> set = getAppSet(session.getUid());
        SessionListener listener = getListener();
        NetSession localSession = set.get(id);
        if(localSession == null){
            localSession = session.copy();
            localSession.setSerialNumber(set.size());
            set.put(id, localSession);
            if(listener != null){
                listener.onNewAdd(localSession);
            }
        }else{
            localSession.set(session);
            if(listener != null){
                listener.onUpdate(localSession);
            }
        }
    }

    private static SparseArray<NetSession> getAppSet(int uid){
        SparseArray<NetSession> set = appSets.get(uid);
        if(set == null){
            set = new SparseArray<>();
            appSets.put(uid, set);
        }
        return set;
    }

    public static List<NetSession> getSessionByUid(int uid){
        SparseArray<NetSession> set = getAppSet(uid);
        List<NetSession> sessions = new ArrayList<>(set.size());
        int size = set.size();
        for(int i=0; i<size; i++){
            sessions.add(set.valueAt(i));
        }
        return sessions;
    }

    public static List<AppSessions> getAppSessions(){
        return null;
    }

    public static void setSessionListener(SessionListener sessionListener) {
        listenerWeakRef = new WeakReference<>(sessionListener);
    }

    private static SessionListener getListener(){
        if(listenerWeakRef == null)return null;
        return listenerWeakRef.get();
    }
}
