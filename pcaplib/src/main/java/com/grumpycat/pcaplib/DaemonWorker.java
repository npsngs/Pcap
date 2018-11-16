package com.grumpycat.pcaplib;

/**
 * Created by cc.he on 2018/11/15
 */
public interface DaemonWorker {
    void launch();
    void reset();
    void shutdown();
}
