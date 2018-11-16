package com.grumpycat.pcaplib.data;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.grumpycat.pcaplib.DaemonWorker;
import com.grumpycat.pcaplib.util.Const;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okio.BufferedSource;
import okio.GzipSink;
import okio.GzipSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * Created by cc.he on 2018/11/15
 */
public class DataManager implements DaemonWorker {
    private DataManager(){isLaunched = false;}
    private static DataManager instance = new DataManager();
    public static DataManager getInstance(){
        return instance;
    }

    private final int BUFFER_SIZE = 64*1024;
    public void asyncGetData(String dir, List<DataMeta> metas, DataGetCallback callback){
        handler.obtainMessage(MSG_LOAD_DATA, new LoadInfo(dir, metas, callback)).sendToTarget();
    }

    public DataMeta putData(byte[] data, int offset, int len){
        if (len > BUFFER_SIZE) throw new IllegalArgumentException(
                String.format(Const.LOCALE, "data size can't large then %dKb", BUFFER_SIZE/1024));

        boolean isSuccess = buffer.putData(data, offset, len);
        if(!isSuccess){
            usedBufferCount++;
            if(bufferCount == usedBufferCount){
                DataBuffer newBuffer = new DataBuffer(BUFFER_SIZE);
                newBuffer.next = buffer.next;
                buffer.next = newBuffer;
                newBuffer.pre = buffer;
                newBuffer.next.pre = newBuffer;
                buffer = newBuffer;
                buffer.isIdle = false;
                bufferCount++;
                serialNum++;
            }else{
                buffer = buffer.next;
                if (!buffer.isIdle)throw new IllegalStateException("DataBuffer must Idle");
                buffer.isIdle = false;
                serialNum++;
                buffer.serialNum = serialNum;
            }
            buffer.putData(data, offset, len);
        }

        DataMeta meta = new DataMeta();
        meta.setTimeDir(curDir);
        meta.setSerialNum(serialNum);
        meta.setLength(len);
        meta.setOffset(buffer.size - len);
        if(bufferCount - usedBufferCount <= 2){
            handler.sendEmptyMessage(MSG_SAVE_DATA);
        }
        return meta;
    }

    private HandlerThread thread;
    private Handler handler;
    private DataBuffer buffer;
    private int serialNum;
    private static final int MSG_SAVE_DATA =    1;
    private static final int MSG_FLUSH_DATA =   2;
    private static final int MSG_LOAD_DATA =    3;

    private int bufferCount;
    private int usedBufferCount;
    private String curDir;
    private volatile boolean isLaunched;
    public void setCurDir(String curDir) {
        this.curDir = curDir;
    }

    @Override
    public void launch() {
        if(isLaunched){
            return;
        }
        isLaunched = true;
        DataBuffer buffer0 = new DataBuffer(BUFFER_SIZE);
        DataBuffer buffer1 = new DataBuffer(BUFFER_SIZE);
        DataBuffer buffer2 = new DataBuffer(BUFFER_SIZE);
        DataBuffer buffer3 = new DataBuffer(BUFFER_SIZE);

        buffer0.next = buffer1;
        buffer1.next = buffer2;
        buffer2.next = buffer3;
        buffer3.next = buffer0;
        buffer0.pre = buffer3;
        buffer1.pre = buffer0;
        buffer2.pre = buffer1;
        buffer3.pre = buffer2;
        buffer = buffer0;
        buffer.isIdle = false;
        serialNum = 1;
        buffer.serialNum = serialNum;
        bufferCount = 4;
        usedBufferCount = 0;

        thread = new HandlerThread("DataManager");
        thread.start();
        handler = new Handler(thread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                handleOp(msg);
            }
        };
    }

    private void handleOp(Message msg){
        switch (msg.what){
            case MSG_SAVE_DATA:
                saveData(usedBufferCount, buffer.pre);
                break;
            case MSG_FLUSH_DATA:
                flushData();
                break;
            case MSG_LOAD_DATA:
                if(msg.obj instanceof LoadInfo){
                    loadData((LoadInfo) msg.obj);
                }
                break;
        }
    }

    private void flushData(){
        saveData(usedBufferCount+1, buffer);
    }


    private void saveData(int count, DataBuffer dataBuffer){
        for(int i = count; i > 0; i--){
            if(dataBuffer.size > 0){
                try {
                    File file = createCacheFile(curDir, dataBuffer.serialNum);
                    Sink sink = Okio.sink(file);
                    if(Const.IS_NEED_GZIP){
                        sink = new GzipSink(sink);
                    }

                    Okio.buffer(sink)
                            .write(dataBuffer.data, 0, dataBuffer.size)
                            .close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            dataBuffer.reset();
            usedBufferCount--;
            dataBuffer = dataBuffer.pre;
            if(usedBufferCount == 0){
                break;
            }
        }
    }

    private File createCacheFile(String dir, int serialNum) throws IOException {
        String pathStr = Const.DATA_DIR + dir + "/" + serialNum;
        File file = new File(pathStr);
        if(!file.exists()){
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }


    private void loadData(LoadInfo loadInfo){
        if (loadInfo.dir == null
                || loadInfo.metas == null
                || loadInfo.metas.size() < 1
                || loadInfo.callback == null){
            return;
        }

        DataBuffer readCache = null;
        String dir = Const.DATA_DIR + loadInfo.dir + "/";
        for(DataMeta dataMeta :loadInfo.metas){
            try {
                int serialNum = dataMeta.getSerialNum();
                int length = dataMeta.getLength();
                byte[] data = new byte[length];
                /*find in memory*/
                DataBuffer dataBuffer = buffer;
                while (dataBuffer.serialNum != serialNum
                        && !dataBuffer.isIdle && dataBuffer != buffer.next ){
                    dataBuffer = dataBuffer.pre;
                }

                if(dataBuffer.serialNum == serialNum){
                    System.arraycopy(
                            dataBuffer.data,
                            dataMeta.getOffset(),
                            data, 0, length);
                    dataMeta.setData(data);
                    dataMeta.setDataStatus(DataMeta.STATUS_READED);
                    continue;
                }


                /*find in read cache*/
                if(readCache != null && readCache.serialNum == serialNum){
                    System.arraycopy(
                            dataBuffer.data,
                            dataMeta.getOffset(),
                            data, 0, length);
                    dataMeta.setData(data);
                    dataMeta.setDataStatus(DataMeta.STATUS_READED);
                    continue;
                }


                /*find in read file*/
                if(readCache == null){
                    DataBuffer buffer0 = new DataBuffer(BUFFER_SIZE);
                    DataBuffer buffer1 = new DataBuffer(BUFFER_SIZE);
                    buffer0.next = buffer1;
                    buffer1.next = buffer0;
                    readCache = buffer0;
                }else{
                    readCache = readCache.next;
                }

                String pathStr = dir + dataMeta.getSerialNum();
                Source source = Okio.source(new File(pathStr));
                if(Const.IS_NEED_GZIP){
                    source = new GzipSource(source);
                }
                BufferedSource bs = Okio.buffer(source);
                int index = 0;
                int ret = 0;
                byte[] buffer = readCache.data;
                while (ret != -1 && index < BUFFER_SIZE){
                    ret = bs.read(buffer, index, BUFFER_SIZE-index);
                    index += ret;
                }
                readCache.serialNum = serialNum;
                System.arraycopy(
                        dataBuffer.data,
                        dataMeta.getOffset(),
                        data, 0, length);
                dataMeta.setData(data);
                dataMeta.setDataStatus(DataMeta.STATUS_READED);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        loadInfo.callback.onGetData();
    }


    @Override
    public void reset() {
        serialNum = 1;
        buffer.reset();
        DataBuffer dataBuffer = buffer.next;
        while (dataBuffer != buffer){
            dataBuffer.reset();
        }
        usedBufferCount = 0;
    }

    @Override
    public void shutdown() {
        thread.quitSafely();
        buffer = null;
        isLaunched = false;
    }


    public interface DataGetCallback{
        void onGetData();
    }

    private static class LoadInfo{
        private String dir;
        private List<DataMeta> metas;
        private DataGetCallback callback;

        LoadInfo(String dir, List<DataMeta> metas, DataGetCallback callback) {
            this.dir = dir;
            this.metas = metas;
            this.callback = callback;
        }
    }
}
