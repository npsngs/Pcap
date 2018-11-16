package com.grumpycat.pcaplib.tcp;

import java.nio.ByteBuffer;

/**
 * Created by cc.he on 2018/11/12
 */
public interface TunnelInterceptor {
    void onReceived(ByteBuffer data);
    void onSend(ByteBuffer data);
    void onClosed();
}
