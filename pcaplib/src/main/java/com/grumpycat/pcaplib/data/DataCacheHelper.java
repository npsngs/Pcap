package com.grumpycat.pcaplib.data;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.IOUtils;
import com.grumpycat.pcaplib.util.ThreadPool;

import java.io.Closeable;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cc.he on 2018/12/7
 */
public class DataCacheHelper implements Closeable {
    private static String cacheDir;
    public static void reset(String vpnTime){
        cacheDir = new StringBuilder()
                .append(Const.CACHE_DIR)
                .append(vpnTime)
                .append("/")
                .toString();
    }

    @Override
    public void close(){
        IOUtils.safeClose(cacheFile);
        cacheFile = null;
    }

    private int reqNum = 0;
    private int respNum = 0;
    private int count;
    private RandomAccessFile cacheFile;
    private int cursorPos;
    private String filePath;
    private boolean lastIsReq;
    private String lastFile;
    public DataCacheHelper(int sessionId) {
        cursorPos = -1;
        filePath = cacheDir+sessionId+"/";
        count = 0;
        try {
            File dir = new File(filePath);
            if(!dir.exists()){
                dir.mkdirs();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void saveData(byte[] data, int size, boolean isRequest){
        ThreadPool.saveCache(new SaveAction(data, size, isRequest));
    }


    private class SaveAction implements Runnable{
        private byte[] data;
        private int size;
        private boolean isRequest;
        public SaveAction(byte[] data, int size, boolean isRequest) {
            this.data = data;
            this.size = size;
            this.isRequest = isRequest;
        }

        @Override
        public void run() {
            try {
                if(count == 0 || lastIsReq != isRequest){
                    lastIsReq = isRequest;
                    String name = isRequest?"req":"resp";
                    if(isRequest){
                        name += reqNum;
                        reqNum++;
                    }else{
                        name += respNum;
                        respNum++;
                    }
                    lastFile = filePath + name;
                    cacheFile = new RandomAccessFile(lastFile, "rw");
                    cacheFile.write(data, 0, size);
                    cacheFile.close();
                }else{
                    cacheFile = new RandomAccessFile(lastFile, "rw");
                    long len = cacheFile.length();
                    cacheFile.seek(len);
                    cacheFile.write(data, 0, size);
                    cacheFile.close();
                }
                count++;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static ParseResult parseSession(String vpnStartTime, int sessionId){
        String dirPath = new StringBuilder()
                .append(Const.CACHE_DIR)
                .append(vpnStartTime)
                .append("/").append(sessionId)
                .toString();

        ParseResult result = new ParseResult();
        List<ParseMeta> ret = new ArrayList<>();
        result.setParseMetas(ret);
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if(files != null && files.length > 0){
            for(File f:files){
                if(f.getName().startsWith("req")){
                    ret.add(new ParseMeta(true, f));
                }else if(f.getName().startsWith("resp")){
                    ret.add(new ParseMeta(false, f));
                }
            }
        }
        return result;
    }
}
