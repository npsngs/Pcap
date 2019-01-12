package com.grumpycat.pcaplib.data;

import java.io.File;

/**
 * Created by cc.he on 2018/12/7
 */
public class ParseMeta {
    private boolean isSend;
    private File dataFile;

    public ParseMeta(boolean isSend, File dataFile) {
        this.isSend = isSend;
        this.dataFile = dataFile;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public File getDataFile() {
        return dataFile;
    }

    public void setDataFile(File dataFile) {
        this.dataFile = dataFile;
    }
}
