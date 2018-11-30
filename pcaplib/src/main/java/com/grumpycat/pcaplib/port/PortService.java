package com.grumpycat.pcaplib.port;

import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;


import com.grumpycat.pcaplib.session.SessionID;
import com.grumpycat.pcaplib.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by cc.he on 2018/11/13
 */
public class PortService {
    static {
        System.loadLibrary("NativePortService");
    }
    public native static int parseUidByPort(int port, int type);

    protected SparseArray<SessionID> portSessions;
    private Handler handler;

    private final static int MSG_TYPE_QUERY =   20;
    public PortService() {
        portSessions = new SparseArray<>();
        HandlerThread thread = new HandlerThread("PortService");
        thread.start();
        handler = new Handler(thread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_TYPE_QUERY:
                        if (msg.obj instanceof PortQuery){
                            query((PortQuery) msg.obj);
                        }
                        break;

                }
            }
        };
    }

    private FileObserver observer;
    public void startObserve(){
        observer = new FileObserver("/proc/net/") {
            @Override
            public void onEvent(int event, @Nullable String path) {
                Log.e("PortS", "Event:"+event+" path:"+path);
            }
        };
        observer.startWatching();
    }

    public void stopObserve(){
        if(observer != null){
            observer.stopWatching();
        }
    }

    public void asyncQuery(PortQuery query){
        handler.obtainMessage(MSG_TYPE_QUERY, query).sendToTarget();
    }

    private void query(PortQuery query){
        int portKey = query.getPort();
        SessionID sessionID = portSessions.get(portKey);
        if(sessionID != null){
            query.onQueryResult(sessionID);
        }
        load(query);
    }

    protected void load(PortQuery query){
        int[] types = query.getType();
        for(int type:types){
            String filePath = PortQuery.getParseFilePath(type);
            if (filePath == null) {
                return;
            }

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(filePath);
                Scanner scanner = new Scanner(fis);
                scanner.useDelimiter("\n");
                String lineStr;


                if(scanner.hasNextLine()){
                    scanner.nextLine();
                    while (scanner.hasNextLine()) {
                        lineStr = scanner.nextLine();
                        SessionID sessionID = parseLineStr(lineStr);
                        if (sessionID == null){
                            continue;
                        }
                        sessionID.type = PortQuery.getProtocolStr(type);
                        portSessions.put(sessionID.localPort, sessionID);
                    }
                }

                SessionID sessionID = portSessions.get(query.getPort());
                if(sessionID != null){
                    query.onQueryResult(sessionID);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                IOUtils.safeClose(fis);
            }
        }
    }


    protected void loadWithCmd(PortQuery query){
        int[] types = query.getType();
        for(int type:types) {
            String filePath = PortQuery.getParseFilePath(type);
            if (filePath == null) {
                return;
            }


            InputStream is = null;
            try {
                String[] cmd = {"cat", filePath};
                ProcessBuilder builder = new ProcessBuilder(cmd);
                builder.directory(new File("/"));
                builder.redirectErrorStream(true);
                Process process = builder.start();
                is = process.getInputStream();
                Scanner scanner = new Scanner(is);
                scanner.useDelimiter("\n");
                String lineStr;
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                    while (scanner.hasNextLine()) {
                        lineStr = scanner.nextLine();
                        SessionID sessionID = parseLineStr(lineStr);
                        if (sessionID == null) {
                            continue;
                        }
                        sessionID.type = PortQuery.getProtocolStr(type);
                        portSessions.put(sessionID.localPort, sessionID);
                    }
                }

                SessionID sessionID = portSessions.get(query.getPort());
                if (sessionID != null) {
                    query.onQueryResult(sessionID);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.safeClose(is);
            }
        }
    }


    protected SessionID parseLineStr(String lineStr) {
        String itemStr[] = lineStr.split("\\s+");
        if (itemStr.length < 9){
            return null;
        }


        String localAddress = itemStr[2];
        String addressItem[] = localAddress.split(":");
        if (addressItem.length < 2) {
            return null;
        }

        SessionID sessionID = new SessionID();
        sessionID.localIp = addressItem[0];
        String localPortStr = addressItem[1];
        sessionID.localPort = Integer.parseInt(localPortStr, 16);

        String remoteAddress = itemStr[3];
        addressItem = remoteAddress.split(":");
        if (addressItem.length < 2) {
            return null;
        }
        sessionID.remoteIp = addressItem[0];
        sessionID.remotePort = addressItem[1];

        String uid = itemStr[8];
        sessionID.uid = Integer.parseInt(uid);

        return sessionID;
    }

}
