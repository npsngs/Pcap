package com.grumpycat.pcaplib.udp;

import android.net.VpnService;


import com.grumpycat.pcaplib.data.MyLRUCache;
import com.grumpycat.pcaplib.protocol.Packet;
import com.grumpycat.pcaplib.tcp.SelectHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by minhui.zhu on 2017/7/13.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class UDPServer implements Runnable {
    private VpnService vpnService;
    private ConcurrentLinkedQueue<Packet> outputQueue;
    private Selector selector;

    private static final int MAX_UDP_CACHE_SIZE = 50;
    private final MyLRUCache<Short, UDPTunnel> udpConnections =
            new MyLRUCache<>(MAX_UDP_CACHE_SIZE, new MyLRUCache.CleanupCallback<UDPTunnel>() {
                @Override
                public void cleanUp(UDPTunnel udpTunnel) {
                    udpTunnel.close();
                }
            });


    public void start() {
        Thread thread = new Thread(this, "UDPServer");
        thread.start();
    }

    public UDPServer(VpnService vpnService, ConcurrentLinkedQueue<Packet> outputQueue) {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.vpnService = vpnService;
        this.outputQueue = outputQueue;
    }


    public void processUDPPacket(Packet packet,short portKey) {
        UDPTunnel udpConn = getUDPConn(portKey);
        if (udpConn == null) {
            udpConn = new UDPTunnel(vpnService, selector, this, packet, outputQueue,portKey);
            putUDPConn(portKey, udpConn);
            udpConn.initConnection();
        } else {
            udpConn.processPacket(packet);
        }
    }


    public void closeAllUDPConn() {
        synchronized (udpConnections) {
            Iterator<Map.Entry<Short, UDPTunnel>> it = udpConnections.entrySet().iterator();
            while (it.hasNext()) {
                it.next().getValue().close();
                it.remove();
            }
        }
    }


    public void closeUDPConn(UDPTunnel connection) {
        synchronized (udpConnections) {
            connection.close();
            udpConnections.remove(connection.getPortKey());
        }
    }

    public UDPTunnel getUDPConn(short portKey) {
        synchronized (udpConnections) {
            return udpConnections.get(portKey);
        }
    }

    void putUDPConn(short ipAndPort, UDPTunnel connection) {
        synchronized (udpConnections) {
            udpConnections.put(ipAndPort, connection);
        }

    }

    @Override
    public void run() {
        try {
            while (true) {
                int select = selector.select();
                if (select == 0) {
                    Thread.sleep(5);
                }
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isValid()) {
                        try {
                            Object attachment = key.attachment();
                            if (attachment instanceof SelectHandler) {
                                ((SelectHandler) attachment).onSelected(key);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    keyIterator.remove();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.stop();
        }
    }

    private void stop() {
        try {
            selector.close();
            selector = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
