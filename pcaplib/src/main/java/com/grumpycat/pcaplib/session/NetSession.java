package com.grumpycat.pcaplib.session;

import com.grumpycat.pcaplib.data.DataMeta;
import com.grumpycat.pcaplib.protocol.HttpHeader;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.StrUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by cc.he on 2018/11/14
 */
public class NetSession implements Serializable {
    private int uid;
    private int protocol;
    private int portKey;
    private int remoteIp;
    private int remotePort;
    private long startTime;
    private HttpHeader httpHeader;
    private String vpnStartTime;

    public long sendByte;
    public int sendPacket;
    public long receiveByte;
    public int receivePacket;
    public long lastActiveTime;

    private transient List<DataMeta> dataMetas;

    public NetSession(int protocol) {
        this.protocol = protocol;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public void setPortKey(int portKey) {
        this.portKey = portKey;
    }

    public void setRemoteIp(int remoteIp) {
        this.remoteIp = remoteIp;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setHttpHeader(HttpHeader httpHeader) {
        this.httpHeader = httpHeader;
    }


    public int getUid() {
        return uid;
    }

    public int getProtocol() {
        return protocol;
    }

    public int getPortKey() {
        return portKey;
    }

    public int getRemoteIp() {
        return remoteIp;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isTcp(){
        return protocol == Const.TCP
                || protocol == Const.HTTP
                || protocol == Const.HTTPS;
    }

    public boolean isUdp(){
        return protocol == Const.UDP;
    }

    public HttpHeader getHttpHeader() {
        return httpHeader;
    }

    public String getVpnStartTime() {
        return vpnStartTime;
    }

    public void setVpnStartTime(String vpnStartTime) {
        this.vpnStartTime = vpnStartTime;
    }

    public List<DataMeta> getDataMetas() {
        return dataMetas;
    }

    public void addDataMeta(DataMeta dataMeta){
        if(dataMetas == null){
            dataMetas = new ArrayList<>();
        }
        dataMetas.add(dataMeta);
    }

    @Override
    public int hashCode() {
        int hash = portKey*31;
        hash += remoteIp*17;
        hash += remotePort*7;
        hash += (startTime & 0xFFFF);
        return hash;
    }

    @Override
    public String toString() {
        return String.format(Const.LOCALE, "PortKey:%d RemoteAddress[%s:%d] sb:%d sp:%d rb:%d rp:%d",
                portKey,
                StrUtil.ip2Str(remoteIp),
                remotePort & 0xFFFF,
                sendByte, sendPacket, receiveByte, receivePacket);
    }

    public static class SessionComparator implements Comparator<NetSession> {
        @Override
        public int compare(NetSession o1, NetSession o2) {
            if (o1 == o2) {
                return 0;
            }
            return Long.compare(o2.lastActiveTime, o1.lastActiveTime);
        }
    }
}
