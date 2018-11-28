package com.grumpycat.pcaplib.session;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.grumpycat.pcaplib.data.PcapSQLiteOpenHelper;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Created by cc.he on 2018/11/28
 */
public class SessionDB implements Closeable{
    private static final String INSERT_SQL = "INSERT INTO sessions(" +
            "id," +
            "protocol," +
            "portKey," +
            "remoteIp," +
            "remotePort," +
            "startTime," +
            "sendByte," +
            "recvByte," +
            "sendPacket," +
            "recvPacket," +
            "lastTimeStamp," +
            "vpnTimeStamp," +
            "extras) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";


    private SQLiteDatabase db;
    public SessionDB(Context context) {
        PcapSQLiteOpenHelper helper = new PcapSQLiteOpenHelper(context);
        db = helper.getWritableDatabase();
    }

    private SQLiteStatement insertStm;
    public void insert(NetSession session){
        if(session == null){return;}
        if(insertStm == null){
            insertStm = db.compileStatement(INSERT_SQL);
        }
        insertStm.clearBindings();
        insertStm.bindLong(1, session.hashCode());
        insertStm.bindLong(2, session.getProtocol());
        insertStm.bindLong(3, session.getPortKey());
        insertStm.bindLong(4, session.getRemoteIp());
        insertStm.bindLong(5, session.getRemotePort());
        insertStm.bindLong(6, session.getStartTime());
        insertStm.bindLong(7, session.sendByte);
        insertStm.bindLong(8, session.receiveByte);
        insertStm.bindLong(9, session.sendPacket);
        insertStm.bindLong(10, session.receiveByte);
        insertStm.bindLong(11, session.lastActiveTime);
        insertStm.bindLong(12, session.getVpnStartTime());
        insertStm.bindString(13, session.getExtras());
        insertStm.executeInsert();
    }

    public void insertAll(List<NetSession> sessions){
        if(sessions != null && sessions.size() > 0){
            db.beginTransaction();
            try{
                for(NetSession session:sessions){
                    insert(session);
                }
                db.setTransactionSuccessful();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                db.endTransaction();
            }
        }
    }

    public List<NetSession> queryByVpnTimeStamp(long vpnTime){
        return null;
    }

    public List<NetSession> queryByID(int hashId){
        return null;
    }

    public List<NetSession> queryByUID(int uid){
        return null;
    }

    public void updateOrInsert(NetSession session){

    }

    public void deleteByVpnTimeStamp(long vpnTime){

    }

    public void deleteAll(){

    }


    @Override
    public void close() throws IOException {
        db.close();
    }
}
