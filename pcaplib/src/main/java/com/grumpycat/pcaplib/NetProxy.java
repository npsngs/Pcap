package com.grumpycat.pcaplib;

/**
 * Created by cc.he on 2018/11/23
 */
public interface NetProxy {
    short getPort();
    void start();
    void stop();
}
