package com.grumpycat.pcap.model;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import com.grumpycat.pcap.tools.AppSessionListener;
import com.grumpycat.pcap.tools.Util;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.session.SessionObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cc.he on 2018/11/28
 */
public class SessionSet {
    private static List<SessionListener> listeners = new ArrayList<>(3);
    private static List<AppSessionListener> appListeners = new ArrayList<>(1);

    private static SparseArray<AppSessions> appSets = new SparseArray<>();
    private static Handler handler = new Handler(Looper.getMainLooper());
    public static void startObserve(){
        SessionManager.getInstance().setObserver((session, event) -> {
            if(event != SessionObserver.EVENT_CLOSE){
                handler.post(() -> insertOrUpdate(session));
            }
        });
    }

    public static void stopObserve(){
        SessionManager.getInstance().setObserver(null);
    }

    public static void clear(){
        int size = appSets.size();
        for(int i=0; i<size; i++){
            AppSessions appSessions = appSets.valueAt(i);
            if(appSessions != null){
                appSessions.clear();
            }
        }

        onClearSession();
        onClearAppSession();
    }

    public static void insertOrUpdate(NetSession session){
        int id = session.hashCode();
        int uid = session.getUid();
        int beforeSize = appSets.size();
        AppSessions appSessions = getAppSet(uid);
        int afterSize = appSets.size();

        boolean isNewAdd = !appSessions.contains(id);
        NetSession localCopy = appSessions.insertOrUpdate(id, session);

        if(isNewAdd){
            onNewAdd(localCopy);
        }else{
            onUpdate(localCopy);
        }

        if(afterSize > beforeSize){
            onNewAdd(appSessions);
        }else{
            onUpdate(appSessions);
        }
    }

    private static AppSessions getAppSet(int uid){
        AppSessions set = appSets.get(uid);
        if(set == null){
            set = new AppSessions();
            set.setUid(uid);
            set.setSerialNumber(appSets.size());
            appSets.put(uid, set);
        }
        return set;
    }

    public static List<NetSession> getSessionByUid(int uid){
        AppSessions set = getAppSet(uid);

        List<NetSession> sessions = new ArrayList<>(set.size());
        int size = set.size();
        for(int i=0; i<size; i++){
            sessions.add(set.valueAt(i));
        }
        return sessions;
    }

    public static List<AppSessions> getAppSessions(){
        int size = appSets.size();
        List<AppSessions> ret = new ArrayList<>(size);
        for(int i=0;i<size;i++){
            ret.add(appSets.valueAt(i));
        }
        return ret;
    }

    public static void addSessionListener(SessionListener sessionListener){
        Util.assertRunInMainThread();
        if(!listeners.contains(sessionListener)){
            listeners.add(sessionListener);
        }
    }

    public static void removeSessionListener(SessionListener sessionListener){
        Util.assertRunInMainThread();
        listeners.remove(sessionListener);
    }

    public static void addAppSessionListener(AppSessionListener sessionListener){
        Util.assertRunInMainThread();
        if(!appListeners.contains(sessionListener)){
            appListeners.add(sessionListener);
        }
    }

    public static void removeAppSessionListener(AppSessionListener sessionListener){
        Util.assertRunInMainThread();
        appListeners.remove(sessionListener);
    }


    private static void onUpdate(NetSession session){
        for(SessionListener listener:listeners){
            listener.onUpdate(session);
        }
    }

    private static void onNewAdd(NetSession session){
        for(SessionListener listener:listeners){
            listener.onNewAdd(session);
        }
    }

    private static void onClearSession(){
        for(SessionListener listener:listeners){
            listener.onClear();
        }
    }

    private static void onUpdate(AppSessions session){
        for(AppSessionListener listener:appListeners){
            listener.onUpdate(session);
        }
    }

    private static void onNewAdd(AppSessions session){
        for(AppSessionListener listener:appListeners){
            listener.onNewAdd(session);
        }
    }

    private static void onClearAppSession(){
        for(AppSessionListener listener:appListeners){
            listener.onClear();
        }
    }


    /***********************
     *  use for history
     ***********************/
    private static List<NetSession> history;
    public static List<NetSession> getHistory() {
        return history;
    }

    public static List<NetSession> getHistory(long vpnStartTime) {
        if(history == null || history.isEmpty()){
            return null;
        }
        List<NetSession> ret = new ArrayList<>();
        for(NetSession session:history){
            if(session.getVpnStartTime() == vpnStartTime){
                ret.add(session);
            }
        }
        return ret;
    }

    public static void setHistory(List<NetSession> history) {
        SessionSet.history = history;
    }
}
