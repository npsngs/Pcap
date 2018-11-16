package com.grumpycat.pcaplib.tcp;

import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by cc.he on 2018/11/12
 */
public class TCPProxy{
    private static final String TAG = "TCPProxy";
    public boolean Stopped;
    public short port;

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public TCPProxy(int port) throws IOException {
        selector = Selector.open();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.port = (short) serverSocketChannel.socket().getLocalPort();
    }

    /**
     * 启动守护线程代理TCP通信
     */
    public void start() {
        Thread thread = new Thread(TAG){
            @Override
            public void run() {
                startSelect();
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        this.Stopped = true;
        if (selector != null) {
            try {
                selector.close();
                selector = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (serverSocketChannel != null) {
            try {
                serverSocketChannel.close();
                serverSocketChannel = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void startSelect() {
        try {
            while (true) {
                int select = selector.select();
                if (select == 0) {
                    Thread.sleep(5);
                    continue;
                }

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                if (selectionKeys == null) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isValid()) {
                        try {
                            if (key.isAcceptable()) {
                                onAccepted();
                            } else {
                                Object attachment = key.attachment();
                                if (attachment instanceof SelectHandler) {
                                    ((SelectHandler) attachment).onSelected(key);
                                }
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



    /**
     * 建立代理隧道
     */
    private void onAccepted() {
        TCPTunnel localTunnel = null;
        try {
            SocketChannel localChannel = serverSocketChannel.accept();
            short portKey = (short) localChannel.socket().getPort();

            localTunnel = TCPTunnel.buildLocalTunnel(localChannel, selector);

            InetSocketAddress remoteAddress = getRemoteAddress(localChannel);
            TCPTunnel remoteTunnel = TCPTunnel.buildRemoteTunnel(selector);
            //关联配对
            TCPTunnel.pair(localTunnel, remoteTunnel);
            //开始连接
            remoteTunnel.setInterceptor(new SessionInterceptor(portKey));
            remoteTunnel.connect(remoteAddress);
        } catch (Exception e) {
            e.printStackTrace();
            if (localTunnel != null) {
                localTunnel.close();
            }
        }
    }


    private InetSocketAddress getRemoteAddress(SocketChannel localChannel) {
        int portKey = localChannel.socket().getPort();
        NetSession session = SessionManager.getSession(portKey);
        if (session != null) {
            return new InetSocketAddress(localChannel.socket().getInetAddress(), session.getRemotePort());
        }
        return null;
    }
}
