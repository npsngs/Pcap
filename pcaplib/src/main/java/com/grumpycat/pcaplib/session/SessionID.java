package com.grumpycat.pcaplib.session;

/**
 * Created by cc.he on 2018/11/13
 */
public class SessionID {
    public String type;
    public String localIp;
    public String remoteIp;
    public String remotePort;

    public int localPort;
    public int uid;
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
