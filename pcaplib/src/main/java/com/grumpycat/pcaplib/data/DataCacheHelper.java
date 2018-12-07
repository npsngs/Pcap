package com.grumpycat.pcaplib.data;
import android.util.SparseArray;

import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.IOUtils;
import com.grumpycat.pcaplib.util.ThreadPool;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * Created by cc.he on 2018/12/7
 */
public class DataCacheHelper {
    private static String cacheDir;
    private static SparseArray<RandomAccessFile> cacheFiles = new SparseArray<>();
    public static void reset(String vpnTime){
        cacheDir = new StringBuilder()
                .append(Const.CACHE_DIR)
                .append(vpnTime)
                .append("/")
                .toString();
        cacheFiles.clear();
    }

    public static void close(){
        cacheDir = null;
        cacheFiles.clear();
    }

    public static void saveData(NetSession session, byte[] data, int size){
        ThreadPool.execute(new SaveAction(session.hashCode(), data, size));
    }

    public static void closeSession(NetSession session){
        RandomAccessFile randomAccessFile = cacheFiles.get(session.hashCode());
        if(randomAccessFile != null){
            cacheFiles.remove(session.hashCode());
            IOUtils.safeClose(randomAccessFile);
        }
    }

    private static class SaveAction implements Runnable{
        private int sessionId;
        private byte[] data;
        private int size;

        public SaveAction(int sessionId, byte[] data, int size) {
            this.sessionId = sessionId;
            this.data = data;
            this.size = size;
        }

        @Override
        public void run() {
            try {
                RandomAccessFile file = cacheFiles.get(sessionId);
                if(file == null){
                    File dir = new File(cacheDir);
                    if(!dir.exists()){
                        dir.mkdirs();
                    }
                    file = new RandomAccessFile(cacheDir+sessionId, "rw");
                    cacheFiles.put(sessionId, file);
                }
                file.write(data,0, size);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public static DataParser createParser(String vpnStartTime, int sessionId){
        String fileName = new StringBuilder()
                .append(Const.CACHE_DIR)
                .append(vpnStartTime)
                .append("/").append(sessionId)
                .toString();
        try {
            return new DataParser(fileName, sessionId);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
