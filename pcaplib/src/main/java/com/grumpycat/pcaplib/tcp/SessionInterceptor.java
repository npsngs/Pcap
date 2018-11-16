package com.grumpycat.pcaplib.tcp;

import android.os.Handler;
import android.os.Looper;

import com.grumpycat.pcaplib.VpnMonitor;
import com.grumpycat.pcaplib.data.DataManager;
import com.grumpycat.pcaplib.data.DataMeta;
import com.grumpycat.pcaplib.data.FileCache;
import com.grumpycat.pcaplib.data.TcpDataSaveHelper;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.ThreadProxy;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Created by cc.he on 2018/11/12
 */
public class SessionInterceptor implements TunnelInterceptor {
    private TcpDataSaveHelper helper;
    private NetSession session;
    private final Handler handler;
    public SessionInterceptor(short sessionKey) {
        session = SessionManager.getSession(sessionKey);
        String helperDir = new StringBuilder()
                .append(Const.DATA_DIR)
                .append(VpnMonitor.getVpnStartTime())
                .append("/")
                .append(session.hashCode())
                .toString();

        helper = new TcpDataSaveHelper(helperDir);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onReceived(ByteBuffer data) {
        refreshSessionAfterRead(data.limit());
        TcpDataSaveHelper.SaveData saveData = new TcpDataSaveHelper
                .SaveData
                .Builder()
                .isRequest(false)
                .needParseData(data.array())
                .length(data.limit())
                .offSet(0)
                .build();
        helper.addData(saveData);

        DataMeta dataMeta = DataManager.getInstance().putData(data.array(), 0, data.limit());
        dataMeta.setOut(false);
        session.addDataMeta(dataMeta);
    }

    @Override
    public void onSend(ByteBuffer data) {
        TcpDataSaveHelper.SaveData saveData = new TcpDataSaveHelper
                .SaveData
                .Builder()
                .isRequest(true)
                .needParseData(data.array())
                .length(data.limit())
                .offSet(0)
                .build();
        helper.addData(saveData);
        DataMeta dataMeta = DataManager.getInstance().putData(data.array(), 0, data.limit());
        dataMeta.setOut(true);
        session.addDataMeta(dataMeta);
    }

    @Override
    public void onClosed() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ThreadProxy.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (session.receiveByte == 0 && session.sendByte == 0) {
                            return;
                        }

                        String configFileDir = Const.CONFIG_DIR
                                + VpnMonitor.getVpnStartTime() ;
                        File parentFile = new File(configFileDir);
                        if (!parentFile.exists()) {
                            parentFile.mkdirs();
                        }
                        //说已经存了
                        File file = new File(parentFile, String.valueOf(session.hashCode()));
                        if (file.exists()) {
                            return;
                        }
                        FileCache configACache = FileCache.get(parentFile);
                        configACache.put(String.valueOf(session.hashCode()), session);
                    }
                });
            }
        }, 1000);
    }

    private void refreshSessionAfterRead(int size) {
        session.receivePacket++;
        session.receiveByte += size;
    }
}
