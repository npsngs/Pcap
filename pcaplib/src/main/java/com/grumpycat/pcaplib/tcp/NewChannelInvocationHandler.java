package com.grumpycat.pcaplib.tcp;

import com.grumpycat.pcaplib.VpnMonitor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

import io.netty.channel.ChannelFactory;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by cc.he on 2018/11/26
 */
public class NewChannelInvocationHandler implements InvocationHandler {
    private ChannelFactory target;
    public Object getInstance(ChannelFactory target){
        this.target = target;
        Class clazz = this.target.getClass();
        // 参数1：被代理类的类加载器 参数2:被代理类的接口 参数3
        return Proxy.newProxyInstance(clazz.getClassLoader(),
                clazz.getInterfaces(),
                this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = method.invoke(target, args);
        NioSocketChannel channel = (NioSocketChannel) ret;
        Socket socket = getSocket(channel);
        if(socket != null){
            VpnMonitor.protect(socket);
        }
        return ret;
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
