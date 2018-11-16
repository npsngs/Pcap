package com.grumpycat.pcaplib.tcp;

import com.grumpycat.pcaplib.VpnMonitor;
import com.grumpycat.pcaplib.data.BufferManager;

import java.io.EOFException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by cc.he on 2018/11/12
 */
public class TCPTunnel {
    private SocketChannel innerChannel;
    private Queue<ByteBuffer> writeCache;
    private TCPTunnel mateTunnel;
    private Selector selector;
    private SelectHandler selectedHandler;
    private boolean isClosed;
    private TunnelInterceptor interceptor;
    private SelectionKey selectionKey;

    private TCPTunnel(){writeCache = new LinkedList<ByteBuffer>() {};}

    public void connect(InetSocketAddress connectAddress) throws Exception {
        innerChannel = SocketChannel.open();
        innerChannel.configureBlocking(false);
        if (VpnMonitor.protect(innerChannel.socket())) {
            selectionKey = innerChannel.register(selector, SelectionKey.OP_CONNECT, getSelectedHandler());
            innerChannel.connect(connectAddress);
        } else {
            throw new Exception("TCPTunnel protect socket failed.");
        }
    }

    private void setMateTunnel(TCPTunnel mateTunnel) {
        this.mateTunnel = mateTunnel;
    }

    public void setInterceptor(TunnelInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public SelectHandler getSelectedHandler(){
        if (selectedHandler == null){
            selectedHandler = new SelectHandler() {
                @Override
                public void onSelected(SelectionKey key) {
                    if (key.isReadable()) {
                        onReadable();
                    } else if (key.isWritable()) {
                        onWritable();
                    } else if (key.isConnectable()) {
                        onConnectable();
                    }
                }
            };
        }
        return selectedHandler;
    }

    private void onReadable(){
        try {
            ByteBuffer buffer = BufferManager.getBufferForRead();
            int bytesRead = innerChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                if (interceptor != null){
                    interceptor.onReceived(buffer);
                }

                mateTunnel.receiveFromMate(buffer);
            } else if (bytesRead < 0) {
                close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }


    private void onWritable(){
        try {
            writeCache();
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }


    private void onConnectable(){
        try {
            if (innerChannel.finishConnect()) {
                prepareBeforeReceive();
                mateTunnel.prepareBeforeReceive();
            } else {
                close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    private void prepareBeforeReceive() throws Exception {
        if (innerChannel.isBlocking()) {
            innerChannel.configureBlocking(false);
        }
        if(selectionKey == null){
            selectionKey = innerChannel.register(
                    selector,
                    SelectionKey.OP_READ,
                    getSelectedHandler());
        }else{
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_READ);
        }
        selector.wakeup();
    }


    private void receiveFromMate(ByteBuffer buffer) throws Exception{
        if (interceptor != null){
            interceptor.onSend(buffer);
        }

        if (writeCache.size() > 0){
            writeCache.add(buffer);
            writeCache();
        }else{
            while (buffer.hasRemaining()) {
                int len = innerChannel.write(buffer);
                if (len < 0){
                    throw new EOFException();
                }
                if (len == 0) {
                    writeCache.add(buffer);
                    selectionKey.interestOps(
                            selectionKey.interestOps() | SelectionKey.OP_WRITE);
                    selector.wakeup();
                    break;
                }
            }
        }
    }



    private void writeCache() throws Exception{
        if(writeCache.isEmpty()){
            return;
        }

        ByteBuffer buffer = writeCache.peek();
        while (!writeCache.isEmpty()) {
            while (buffer.hasRemaining()) {
                int len = innerChannel.write(buffer);
                if (len < 0) {
                    throw new EOFException();
                }
                if (len == 0) {
                    return;
                }
            }
            BufferManager.recycleBuffer(buffer);
            writeCache.poll();
        }

        if(writeCache.isEmpty()){
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
        }
    }


    public void close(){
        close(true);
    }

    public void close(boolean isClosePair){
        if (!isClosed) {
            try {
                if (!writeCache.isEmpty()){
                    BufferManager.recycleBuffers(writeCache);
                    writeCache.clear();
                }

                selectionKey.cancel();
                selector.selectNow();
                innerChannel.close();

                if (isClosePair && mateTunnel != null) {
                    mateTunnel.close(false);
                }
                innerChannel = null;
                selector = null;
                mateTunnel = null;
                isClosed = true;

                if (interceptor != null){
                    interceptor.onClosed();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static TCPTunnel buildLocalTunnel(SocketChannel innerChannel, Selector selector){
        TCPTunnel tcpTunnel = new TCPTunnel();
        tcpTunnel.selector = selector;
        tcpTunnel.innerChannel = innerChannel;
        return tcpTunnel;
    }

    public static TCPTunnel buildRemoteTunnel(Selector selector){
        TCPTunnel tcpTunnel = new TCPTunnel();
        tcpTunnel.selector = selector;
        return tcpTunnel;
    }


    public static void pair(TCPTunnel t1, TCPTunnel t2){
        t1.setMateTunnel(t2);
        t2.setMateTunnel(t1);
    }
}
