package com.grumpycat.pcaplib.tcp;

import com.grumpycat.pcaplib.data.DataCacheHelper;
import com.grumpycat.pcaplib.session.NetSession;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.util.CommonUtil;
import com.grumpycat.pcaplib.util.IOUtils;

import java.nio.ByteBuffer;

/**
 * Created by cc.he on 2018/11/12
 */
public class SessionInterceptor implements TunnelInterceptor {
    private DataCacheHelper helper;
    private NetSession session;
    public SessionInterceptor(short sessionKey) {
        session = SessionManager.getInstance().getSession(sessionKey);
        if(session != null){
            helper = new DataCacheHelper(session.hashCode());
        }
    }

    @Override
    public void onReceived(ByteBuffer data) {
        if(data == null || helper == null){
            return;
        }

        int size = data.limit();
        helper.saveData(CommonUtil.copyByte(data.array(), 0, size),
                size,
                false);
        SessionManager.getInstance().addSessionReadBytes(session, size);

        //SessionManager.getInstance().addSessionReadBytes(session, data.limit());
        /*refreshSessionAfterRead(data.limit());*/
        /*TcpDataSaveHelper.SaveData saveData = new TcpDataSaveHelper
                .SaveData
                .Builder()
                .isRequest(false)
                .needParseData(data.array())
                .length(data.limit())
                .offSet(0)
                .build();
        helper.addData(saveData);*/

        /*DataMeta dataMeta = DataManager.getInstance().putData(data.array(), 0, data.limit());
        dataMeta.setOut(false);
        session.addDataMeta(dataMeta);*/
    }

    @Override
    public void onSend(ByteBuffer data) {
        if(data == null || helper == null){
            return;
        }

        int size = data.limit();
        helper.saveData(CommonUtil.copyByte(data.array(), 0, size),
                size,
                true);
        SessionManager.getInstance().addSessionSendBytes(session, size);


        /*TcpDataSaveHelper.SaveData saveData = new TcpDataSaveHelper
                .SaveData
                .Builder()
                .isRequest(true)
                .needParseData(data.array())
                .length(data.limit())
                .offSet(0)
                .build();
        helper.addData(saveData);*/
        /*DataMeta dataMeta = DataManager.getInstance().putData(data.array(), 0, data.limit());
        dataMeta.setOut(true);
        session.addDataMeta(dataMeta);*/
    }

    @Override
    public void onClosed() {
        IOUtils.safeClose(helper);
        SessionManager.getInstance().moveToSaveQueue(session);
       /*handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ThreadPool.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (session.receiveByte == 0 && session.sendByte == 0) {
                            return;
                        }

                        String configFileDir = Const.CONFIG_DIR
                                + VpnMonitor.getVpnStartTimeStr() ;
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
        }, 1000);*/
    }

    /*private void refreshSessionAfterRead(int size) {
        session.receivePacket++;
        session.receiveByte += size;
    }*/
}
