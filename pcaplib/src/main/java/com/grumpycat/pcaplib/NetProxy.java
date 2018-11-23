package com.grumpycat.pcaplib;

/**
 * Created by cc.he on 2018/11/23
 */
public interface NetProxy {
    int getPort();
    void start();
    void stop();
}
