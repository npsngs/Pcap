package com.grumpycat.pcap.model;

/**
 * Created by cc.he on 2018/12/6
 */
public class HistoryInfo {
    private long timeStamp;
    private int sessionCount;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }

    public void addCount(int add){
        this.sessionCount += add;
    }
}
