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
    }

    @Override
    public void onClosed() {
        IOUtils.safeClose(helper);
        SessionManager.getInstance().moveToSaveQueue(session);
    }
}
