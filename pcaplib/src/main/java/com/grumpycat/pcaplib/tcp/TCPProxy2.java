package com.grumpycat.pcaplib.tcp;

import com.grumpycat.pcaplib.NetProxy;
import com.grumpycat.pcaplib.VpnMonitor;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.net.ssl.SSLEngine;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;

/**
 * Created by cc.he on 2018/11/23
 */
public class TCPProxy2 implements NetProxy {
    private int port;
    private NioEventLoopGroup boss;
    private NioEventLoopGroup worker;
    private Channel channel;

    public TCPProxy2(int port) {
        this.port = port;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void start() {
        boss = new NioEventLoopGroup(1);
        worker = new NioEventLoopGroup();
        try {
            ChannelFuture bindFuture = new ServerBootstrap()
                    .channel(NioServerSocketChannel.class)
                    .group(boss, worker)
                    .childHandler(new ChildInitHandler())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .bind(port).sync();
            channel = bindFuture.channel();
            NioServerSocketChannel ns = (NioServerSocketChannel) channel;
            port = ns.localAddress().getPort();
            channel
                    .closeFuture()
                    .addListener(new CloseListener());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void stop() {
        if(channel != null){
            channel.close();
            channel = null;
        }
    }


    private class CloseListener implements ChannelFutureListener {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            boss = null;
            worker = null;
        }
    }

    private class ChildInitHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast("tcp", new TcpHandler());
        }
    }

    private class TcpHandler extends ChannelInboundHandlerAdapter {
        private int num = 0;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (num == 0) {
                ByteBuf byteBuf = (ByteBuf) msg;
                byte b = byteBuf.readByte();
                byteBuf.readerIndex(0);
                if (b == 22) {
                    ctx.pipeline().addAfter("tcp", "ssl",
                            new SslHandler(VpnMonitor.createSslEngine(false)));
                    ctx.pipeline().addLast(new DataHandler(true));
                }else{
                    ctx.pipeline().addLast(new DataHandler(false));
                }
                num++;
            }
            super.channelRead(ctx, msg);
        }
    }


    private class DataHandler extends ChannelInboundHandlerAdapter {
        private boolean isSSL;
        private Channel remoteChannel;
        public DataHandler(boolean isSSL) {
            this.isSSL = isSSL;
        }


        @Override
        public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
            if(remoteChannel == null){
                NioSocketChannel channel = (NioSocketChannel) ctx.channel();
                int portKey = channel.remoteAddress().getPort();
                NetSession session = SessionManager.getSession(portKey);
                if (session != null) {
                    SocketAddress address = new InetSocketAddress(
                            channel.remoteAddress().getAddress(),
                            session.getRemotePort());

                    Bootstrap bootstrap = new Bootstrap();
                    ChannelFuture future = bootstrap.channel(NioSocketChannel.class)
                            .group(worker)
                            .option(ChannelOption.SO_KEEPALIVE, true)
                            .handler(new ChannelInitializer<SocketChannel>(){
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    if(isSSL){
                                        SSLEngine sslEngine = VpnMonitor.createSslEngine(true);
                                        ch.pipeline().addLast(new SslHandler(sslEngine));
                                    }
                                    ch.pipeline().addLast(new TunnelHandler(ctx.channel()));
                                }
                            })
                            .connect(address)
                            .sync();
                    remoteChannel = future.channel();
                    Socket socket = getSocket((NioSocketChannel) remoteChannel);
                    if(socket != null){
                        VpnMonitor.protect(socket);
                    }
                }
            }
            remoteChannel.write(msg);
            remoteChannel.flush();
        }
    }


    private class TunnelHandler extends ChannelInboundHandlerAdapter {
        private Channel localChannel;

        public TunnelHandler(Channel localChannel) {
            this.localChannel = localChannel;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            localChannel.write(msg);
            localChannel.flush();
        }
    }


    private Socket getSocket(NioSocketChannel channel){
        try {
            Class<NioSocketChannel> cls = NioSocketChannel.class;
            Method mtd = cls.getDeclaredMethod("javaChannel");
            mtd.setAccessible(true);
            Object obj = mtd.invoke(channel);
            java.nio.channels.SocketChannel ssc = (java.nio.channels.SocketChannel) obj;
            return ssc.socket();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
