package com.grumpycat.pcaplib.session;

import android.text.TextUtils;

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
public class NetSession implements Serializable , Cloneable{
    private int uid;
    private int protocol;
    private int portKey;
    private int remoteIp;
    private int remotePort;
    private long startTime;

    public long sendByte;
    public int sendPacket;
    public long receiveByte;
    public int receivePacket;
    public long lastActiveTime;
    private long vpnStartTime;
    private HttpHeader httpHeader;
    private int hashCode = 0;

    private transient int serialNumber;
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

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
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

    public long getVpnStartTime() {
        return vpnStartTime;
    }

    public void setVpnStartTime(long vpnStartTime) {
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

    public String getExtras(){
        if(httpHeader == null){
            return null;
        }
        return httpHeader.toJson();
    }



    @Override
    public int hashCode() {
        if(hashCode == 0){
            int hash = portKey*37;
            hash += remoteIp*17;
            hash += remotePort*7;
            hash += (startTime & 0xFFFF);
            hashCode = hash;
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return String.format(Const.LOCALE, "PortKey:%d RemoteAddress[%s:%d] sb:%d sp:%d rb:%d rp:%d",
                portKey,
                StrUtil.ip2Str(remoteIp),
                remotePort & 0xFFFF,
                sendByte, sendPacket, receiveByte, receivePacket);
    }

    public String getBriefInfo(){
        switch (protocol){
            case Const.HTTP:
            case Const.HTTPS:
                if(httpHeader != null){
                    if (!TextUtils.isEmpty(httpHeader.url)) {
                        return httpHeader.url;
                    } else if(!TextUtils.isEmpty(httpHeader.host)){
                        return httpHeader.host;
                    }else{
                        return String.format(Const.LOCALE,
                                "%s:%d",
                                StrUtil.ip2Str(remoteIp),
                                remotePort);
                    }
                }
            default:
                return String.format(Const.LOCALE,
                        "%s:%d",
                        StrUtil.ip2Str(remoteIp),
                        remotePort);
        }
    }




    public NetSession copy(){
        try {
            return (NetSession) this.clone();
        }catch (Exception e){
            e.printStackTrace();
        }
        NetSession ret = new NetSession(this.protocol);
        ret.set(this);
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof NetSession){
            return hashCode() == obj.hashCode();
        }
        return false;
    }

    public void set(NetSession session){
        this.uid = session.uid;
        this.protocol = session.protocol;
        this.portKey = session.portKey;
        this.remoteIp = session.remoteIp;
        this.remotePort = session.remotePort;
        this.startTime = session.startTime;
        this.hashCode = session.hashCode;
        this.sendByte = session.sendByte;
        this.sendPacket = session.sendPacket;
        this.receiveByte = session.receiveByte;
        this.receivePacket = session.receivePacket;
        this.lastActiveTime = session.lastActiveTime;
        this.vpnStartTime = session.vpnStartTime;
        if(this.httpHeader == null && session.httpHeader !=null){
            this.httpHeader = session.httpHeader.copy();
        }else if(session.httpHeader == null){
            this.httpHeader = null;
        }else {
            this.httpHeader.set(session.httpHeader);
        }
    }

    public boolean hasData(){
       return receiveByte != 0 || sendByte != 0;
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
