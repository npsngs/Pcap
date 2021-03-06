package com.grumpycat.pcaplib;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.grumpycat.pcaplib.data.DataCacheHelper;
import com.grumpycat.pcaplib.http.HttpParser;
import com.grumpycat.pcaplib.port.PortQuery;
import com.grumpycat.pcaplib.port.PortService;
import com.grumpycat.pcaplib.protocol.HttpHeader;
import com.grumpycat.pcaplib.protocol.IPHeader;
import com.grumpycat.pcaplib.protocol.Packet;
import com.grumpycat.pcaplib.protocol.TCPHeader;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.tcp.TCPProxy;
import com.grumpycat.pcaplib.udp.UDPServer;
import com.grumpycat.pcaplib.util.CommonMethods;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.HexStr;
import com.grumpycat.pcaplib.util.IOUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Created by cc.he on 2018/11/13
 */
public class VpnHelper {
    private GVpnService service;
    public VpnHelper(GVpnService service) {
        this.service = service;
        init();
    }

    private byte[] buffer;
    private IPHeader ipHeader;
    private TCPHeader tcpHeader;
    private ConcurrentLinkedQueue<Packet> udpQueue;
    private ParcelFileDescriptor descriptor;
    private FileOutputStream fos;
    private FileInputStream fis;
    private PortService portService;
    private NetProxy tcpProxy;
    private UDPServer udpServer;
    private void init(){
        buffer = new byte[Const.MUTE_SIZE];
        ipHeader = new IPHeader(buffer, 0);
        tcpHeader = new TCPHeader(buffer, 20);
    }

    public void launchVpnThread(){
        new Thread("VPN-Service"){
            @Override
            public void run() {
                runningVpn();
            }
        }.start();
    }


    private void runningVpn(){
        try {
            portService = new PortService();
            portService.startObserve();
            udpQueue = new ConcurrentLinkedQueue<>();

            //启动TCP代理服务
                tcpProxy = new TCPProxy();
            tcpProxy.start();
            udpServer = new UDPServer(service, udpQueue);
            udpServer.start();

            SessionManager.getInstance().reset();
            descriptor = service.establishVpn();
            fos = new FileOutputStream(descriptor.getFileDescriptor());
            fis = new FileInputStream(descriptor.getFileDescriptor());
            DataCacheHelper.reset(VpnMonitor.getVpnStartTimeStr());

            startLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            close();
        }
    }


    private void startLoop() throws Exception{
        VpnMonitor.setVpnRunning(true);
        while (VpnMonitor.isVpnRunning()){
            boolean hasWrite = false;
            int size = fis.read(buffer);
            if (size == -1)break;

            if (size > 0) {
                hasWrite = onIPPacketReceived(ipHeader, size);
            }

            if (!hasWrite) {
                Packet packet = udpQueue.poll();
                if (packet != null) {
                    ByteBuffer bufferFromNetwork = packet.backingBuffer;
                    bufferFromNetwork.flip();
                    fos.write(bufferFromNetwork.array());
                    hasWrite = true;
                }
            }
            if(size == 0 && !hasWrite){
                Thread.sleep(10);
            }
        }
    }


    private boolean onIPPacketReceived(IPHeader ipHeader, int size) throws IOException {
        boolean hasWrite = false;
        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:
                hasWrite = onTcpPacketReceived(ipHeader, size);
                break;
            case IPHeader.UDP:
                onUdpPacketReceived(ipHeader, size);
                break;
        }
        return hasWrite;
    }

    private void onUdpPacketReceived(IPHeader ipHeader, int size) throws UnknownHostException {
        short portKey = tcpHeader.getSourcePort();


        NetSession session = SessionManager.getInstance().getSession(portKey);
        if (session == null || session.getRemoteIp() != ipHeader.getDestinationIP()
                || session.getRemotePort() != tcpHeader.getDestinationPort()) {
            session = SessionManager.getInstance().createSession(
                    portKey,
                    ipHeader.getDestinationIP(),
                    tcpHeader.getDestinationPort(),
                    Const.UDP);

            if(VpnMonitor.isSingleApp()){
                session.setUid(VpnMonitor.getSingleAppUid());
            }else{
                int uid = PortService.parseUidByPort(portKey & 0xFFFF, PortQuery.TYPE_UDP);
                session.setUid(uid);
            }
        }else{
            session.lastActiveTime = System.currentTimeMillis();
        }

        session.sendPacket++; //注意顺序

        byte[] bytes = Arrays.copyOf(buffer, buffer.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, size);
        byteBuffer.limit(size);
        Packet packet = new Packet(byteBuffer);
        udpServer.processUDPPacket(packet, portKey);
    }

    private boolean onTcpPacketReceived(IPHeader ipHeader, int size) throws IOException {
        boolean hasWrite;
        //矫正TCPHeader里的偏移量，使它指向真正的TCP数据地址
        tcpHeader.mOffset = ipHeader.getHeaderLength();
        if (tcpHeader.getSourcePort() == tcpProxy.getPort()) {
            NetSession session = SessionManager.getInstance().getSession(tcpHeader.getDestinationPort());
            if (session != null) {
                ipHeader.setSourceIP(ipHeader.getDestinationIP());
                tcpHeader.setSourcePort((short) session.getRemotePort());
                ipHeader.setDestinationIP(VpnMonitor.getLocalIp());

                CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);


                /*int offset = ipHeader.getHeaderLength()+tcpHeader.getHeaderLength();
                if(ipHeader.mData[offset] == 0x16){
                    Log.e("TLS", convertHexStr(ipHeader.mData, 0, ipHeader.getTotalLength()));
                }*/


                if(Const.LOG_ON){
                    Log.e("exchange", ipHeader.toString()
                            + "size:"+size
                            + " totalS:"
                            + ipHeader.getTotalLength()
                            + "THLen:"+tcpHeader.getHeaderLength()
                            + "TCP:["+tcpHeader.toString()+"]"
                            + printData(ipHeader, tcpHeader));
                }


                fos.write(ipHeader.mData, ipHeader.mOffset, size);
                VpnMonitor.addReceiveBytes(size);
            }
        } else {
            //添加端口映射
            short portKey = tcpHeader.getSourcePort();
            NetSession session = SessionManager.getInstance().getSession(portKey);
            if (session == null || session.getRemoteIp() != ipHeader.getDestinationIP()
                    || session.getRemotePort() != tcpHeader.getDestinationPort()) {
                session = SessionManager.getInstance().createSession(
                        portKey,
                        ipHeader.getDestinationIP(),
                        tcpHeader.getDestinationPort(),
                        Const.TCP);
                if(VpnMonitor.isSingleApp()){
                    session.setUid(VpnMonitor.getSingleAppUid());
                }else {
                    int uid = PortService.parseUidByPort(portKey & 0xFFFF, PortQuery.TYPE_TCP);
                    session.setUid(uid);
                }
            }else{
                session.lastActiveTime = System.currentTimeMillis();
            }
            session.sendPacket++;
            int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();

            if (session.sendByte == 0 && tcpDataSize > 10) {
                int dataOffset = tcpHeader.mOffset + tcpHeader.getHeaderLength();
                HttpHeader httpHeader = HttpParser.parseHttpRequestHeader(
                        tcpHeader.mData,
                        dataOffset,
                        tcpDataSize);
                if(httpHeader != null){
                    session.setHttpHeader(httpHeader);
                    session.setProtocol(httpHeader.isHttps?Const.HTTPS:Const.HTTP);
                }
            } else if (session.sendByte > 0
                    && session.getProtocol() != Const.HTTPS
                    && session.getProtocol() == Const.HTTP
                    && session.getHttpHeader() == null) {
                int dataOffset = tcpHeader.mOffset + tcpHeader.getHeaderLength();

                HttpHeader httpHeader = new HttpHeader();
                httpHeader.host = HttpParser.getRemoteHost(
                        tcpHeader.mData,
                        dataOffset,
                        tcpDataSize);
                httpHeader.url = "http://" + httpHeader.host + "/";
                session.setHttpHeader(httpHeader);
                session.setProtocol(Const.HTTP);
            }

            ipHeader.setSourceIP(VpnMonitor.getLocalIp());

            ipHeader.setSourceIP(ipHeader.getDestinationIP());
            ipHeader.setDestinationIP(VpnMonitor.getLocalIp());
            tcpHeader.setDestinationPort(tcpProxy.getPort());

            CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);

            fos.write(ipHeader.mData, ipHeader.mOffset, size);
            //注意顺序
            VpnMonitor.addSendBytes(size);
        }
        hasWrite = true;
        return hasWrite;
    }

    private String printData(IPHeader ipHeader, TCPHeader tcpHeader){
        int data = ipHeader.getDataLength() - tcpHeader.getHeaderLength();
        if (data <= 0){
            return "";
        }
        int offset = ipHeader.getHeaderLength()+tcpHeader.getHeaderLength();
        return "[data:]\n" + convertByte2Hex(ipHeader.mData, offset, data) +"\n[dataStr:]\n"+new String(ipHeader.mData, offset, data);
    }

    private String convertByte2Hex(byte[] data, int offset, int len){
        StringBuilder s = new StringBuilder();
        int index = offset;
        int row = 0;
        while (index < len){
            s.append(HexStr.byte2Hex(data[index]));
            index++;
            row++;
            if (row > 15){
                s.append("\n");
                row = 0;
            }
        }
        return s.toString();
    }

    private void close(){
        IOUtils.safeClose(fis);
        IOUtils.safeClose(fos);
        IOUtils.safeClose(descriptor);
        IOUtils.safeClose(SessionManager.getInstance());
        try {
            //停止TCP代理服务
            if (tcpProxy != null) {
                tcpProxy.stop();
                tcpProxy = null;
            }
            if(udpServer != null){
                udpServer.closeAllUDPConn();
            }
            portService.stopObserve();

            service.stopSelf();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            VpnMonitor.setVpnRunning(false);
        }
    }


    public void stopVpn(){
        VpnMonitor.setVpnRunning(false);
    }

}
