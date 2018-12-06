package com.grumpycat.pcap.model;

import com.grumpycat.pcaplib.session.NetSession;

/**
 * Created by cc.he on 2018/11/28
 */
public interface SessionListener {
    void onUpdate(NetSession session);
    void onNewAdd(NetSession session);
    void onClear();
}
