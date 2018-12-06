package com.grumpycat.pcaplib.tcp;

import com.grumpycat.pcaplib.NetProxy;
import com.grumpycat.pcaplib.VpnController;
import com.grumpycat.pcaplib.VpnMonitor;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
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
    private NioEventLoopGroup boss;
    private NioEventLoopGroup worker;
    private Channel channel;


    private short port;
    @Override
    public short getPort() {
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
                .bind(port).sync();
            channel = bindFuture.channel();
            NioServerSocketChannel ns = (NioServerSocketChannel) channel;
            port = (short) ns.localAddress().getPort();
            VpnMonitor.setTcpProxyPort(port & 0xFFFF);
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
            if(VpnController.isCrackTLS()){
                ch.pipeline().addLast("tcp", new TcpHandler());
            }else{
                ch.pipeline().addLast(new TcpDataHandler(false));
            }
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
                    ctx.pipeline().addLast(new TcpDataHandler(true));
                }else{
                    ctx.pipeline().addLast(new TcpDataHandler(false));
                }
                num++;
            }
            super.channelRead(ctx, msg);
        }
    }

    private boolean protectChannelSocket(Bootstrap bootstrap){
        try {
            Class bcls = AbstractBootstrap.class;
            Field fdcf = bcls.getDeclaredField("channelFactory");
            fdcf.setAccessible(true);
            ChannelFactory cf = (ChannelFactory) fdcf.get(bootstrap);
            fdcf.set(bootstrap, null);
            NewChannelInvocationHandler handler = new NewChannelInvocationHandler();

            ChannelFactory proxy = (ChannelFactory) handler.getInstance(cf);
            bootstrap.channelFactory(proxy);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }



    private class TcpDataHandler extends ChannelInboundHandlerAdapter {
        private boolean isSSL;
        private Channel remoteChannel;
        private TunnelInterceptor interceptor;
        public TcpDataHandler(boolean isSSL) {
            this.isSSL = isSSL;
        }


        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(remoteChannel == null){
                final NioSocketChannel localChannel = (NioSocketChannel) ctx.channel();
                int portKey = localChannel.remoteAddress().getPort();
                interceptor = new SessionInterceptor((short) portKey);
                NetSession session = SessionManager.getInstance().getSession(portKey);
                if (session != null) {
                    SocketAddress address = new InetSocketAddress(
                            localChannel.remoteAddress().getAddress(),
                            session.getRemotePort());

                    Bootstrap bootstrap = new Bootstrap()
                            .channel(NioSocketChannel.class);
                    boolean isSuccess = protectChannelSocket(bootstrap);
                    if(!isSuccess){
                        return;
                    }

                    ChannelFuture future = bootstrap
                            .group(worker)
                            .handler(new ChannelInitializer<SocketChannel>(){
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    if(isSSL){
                                        SSLEngine sslEngine = VpnMonitor.createSslEngine(true);
                                        ch.pipeline().addLast(new SslHandler(sslEngine));
                                    }
                                    ch.pipeline().addLast(new TunnelHandler(localChannel, interceptor));
                                }
                            })
                            .connect(address)
                            .sync();
                    remoteChannel = future.channel();
                }
            }

            if(interceptor != null){
                interceptor.onSend(readData(msg));
            }
            remoteChannel.write(msg);
            remoteChannel.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            ctx.channel().close();
            if(remoteChannel != null){
                remoteChannel.close();
                remoteChannel = null;
            }
            if(interceptor != null){
                interceptor.onClosed();
                interceptor = null;
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            ctx.channel().close();
            if(remoteChannel != null){
                remoteChannel.close();
                remoteChannel = null;
            }
            if(interceptor != null){
                interceptor.onClosed();
                interceptor = null;
            }
        }
    }


    private ByteBuffer readData(Object msg){
        try {
            ByteBuf byteBuf = (ByteBuf) msg;
            ByteBuffer buffer = ByteBuffer.allocate(byteBuf.writerIndex());
            byteBuf.readBytes(buffer);
            byteBuf.readerIndex(0);
            return buffer;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private class TunnelHandler extends ChannelInboundHandlerAdapter {
        private Channel localChannel;
        private TunnelInterceptor interceptor;

        public TunnelHandler(Channel localChannel, TunnelInterceptor interceptor) {
            this.localChannel = localChannel;
            this.interceptor = interceptor;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(interceptor != null) {
                interceptor.onReceived(readData(msg));
            }
            localChannel.write(msg);
            localChannel.flush();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            if(localChannel != null){
                localChannel.close();
                localChannel = null;
            }
            ctx.channel().close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            if(localChannel != null){
                localChannel.close();
                localChannel = null;
            }
            ctx.channel().close();
        }
    }
}
