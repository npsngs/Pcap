package com.grumpycat.pcaplib.session;

/**
 * Created by cc.he on 2018/11/28
 */
public interface SessionObserver {
    int EVENT_CREATE =  0;
    int EVENT_SEND =    1;
    int EVENT_RECV =    2;
    int EVENT_CLOSE =   4;
    void onSessionChange(NetSession session, int event);
}
