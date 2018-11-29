package com.grumpycat.pcap.tools;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.session.SessionObserver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cc.he on 2018/11/28
 */
public class SessionSet {
    private static WeakReference<SessionListener> listenerWeakRef;
    private static WeakReference<AppSessionListener> appListenerWeakRef;
    private static SparseArray<AppSessions> appSets = new SparseArray<>();
    private static int uid = -1;
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

    public static void setSingleApp(int uid) {
        SessionSet.uid = uid;
    }
    public static void setMultiApp() {
        SessionSet.uid = -1;
    }

    private static boolean isSingleApp(){
        return uid > -1;
    }
    public static void clear(){
        int size = appSets.size();
        for(int i=0; i<size; i++){
            AppSessions appSessions = appSets.valueAt(i);
            if(appSessions != null){
                appSessions.clear();
            }
        }

        if(isSingleApp()){
            SessionListener listener = getListener();
            if(listener != null){
                listener.onClear();
            }
        }else{
            AppSessionListener listener = getAppListener();
            if(listener != null){
                listener.onClear();
            }
        }
    }

    public static void insertOrUpdate(NetSession session){
        int id = session.hashCode();
        int uid = session.getUid();
        AppSessions appSessions = getAppSet(uid);
        NetSession localSession = appSessions.get(id);
        if(localSession == null){
            localSession = session.copy();
            localSession.setSerialNumber(appSessions.size());
            appSessions.put(id, localSession);
            if(isSingleApp() && uid == SessionSet.uid){
                SessionListener listener = getListener();
                if(listener != null){
                    listener.onNewAdd(localSession);
                }
            }else{
                AppSessionListener listener = getAppListener();
                if(listener != null){
                    if(appSessions.size() > 1){
                        listener.onUpdate(appSessions);
                    }else{
                        listener.onNewAdd(appSessions);
                    }
                }
            }
        }else{
            localSession.set(session);
            if(isSingleApp() && uid == SessionSet.uid){
                SessionListener listener = getListener();
                if(listener != null){
                    listener.onUpdate(localSession);
                }
            } else {
                AppSessionListener listener = getAppListener();
                if(listener != null){
                    listener.onUpdate(appSessions);
                }
            }
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

    public static void setSessionListener(SessionListener sessionListener) {
        listenerWeakRef = new WeakReference<>(sessionListener);
    }

    public static void setAppSessionListener(AppSessionListener sessionListener) {
        appListenerWeakRef = new WeakReference<>(sessionListener);
    }

    private static SessionListener getListener(){
        if(listenerWeakRef == null)return null;
        return listenerWeakRef.get();
    }

    private static AppSessionListener getAppListener(){
        if(appListenerWeakRef == null)return null;
        return appListenerWeakRef.get();
    }
}
