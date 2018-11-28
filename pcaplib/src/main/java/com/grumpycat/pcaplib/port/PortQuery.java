package com.grumpycat.pcaplib.port;

import com.grumpycat.pcaplib.session.SessionID;

/**
 * Created by cc.he on 2018/11/13
 */
public abstract class PortQuery {
    private int port;
    private int[] type;
    public final static int TYPE_TCP =     0;
    public final static int TYPE_TCP6 =    1;
    public final static int TYPE_UDP =     2;
    public final static int TYPE_UDP6 =    3;
    public PortQuery(int port, int ...type) {
        this.port = port;
        this.type = type;
    }

    public int getPort() {
        return port;
    }

    public int[] getType() {
        return type;
    }

    public abstract void onQueryResult(SessionID sessionID);




    public static String getParseFilePath(int type){
        switch (type){
            case TYPE_TCP:
                return "/proc/net/tcp";
            case TYPE_TCP6:
                return "/proc/net/tcp6";
            case TYPE_UDP:
                return "/proc/net/udp";
            case TYPE_UDP6:
                return "/proc/net/udp6";
        }
        return null;
    }

    public static String getProtocolStr(int type){
        switch (type){
            case TYPE_TCP:
                return "tcp";
            case TYPE_TCP6:
                return "tcp6";
            case TYPE_UDP:
                return "udp";
            case TYPE_UDP6:
                return "udp6";
        }
        return null;
    }
}
