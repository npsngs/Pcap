package com.grumpycat.pcaplib.session;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.grumpycat.pcaplib.data.PcapSQLiteOpenHelper;
import com.grumpycat.pcaplib.protocol.HttpHeader;
import com.grumpycat.pcaplib.util.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cc.he on 2018/11/28
 */
public class SessionDB implements Closeable{
    private static final String INSERT_SQL = "INSERT INTO sessions(" +
            "id," +
            "uid," +
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
            "extras) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SELECT_ALL_SORT_BY_VPNTIME_SQL = "SELECT * FROM sessions "+
            "ORDER BY vpnTimeStamp DESC";

    private static final String DELETE_ALL_SQL = "DELETE FROM sessions";

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
        insertStm.bindLong(2, session.getUid());
        insertStm.bindLong(3, session.getProtocol());
        insertStm.bindLong(4, session.getPortKey());
        insertStm.bindLong(5, session.getRemoteIp());
        insertStm.bindLong(6, session.getRemotePort());
        insertStm.bindLong(7, session.getStartTime());
        insertStm.bindLong(8, session.sendByte);
        insertStm.bindLong(9, session.receiveByte);
        insertStm.bindLong(10, session.sendPacket);
        insertStm.bindLong(11, session.receivePacket);
        insertStm.bindLong(12, session.lastActiveTime);
        insertStm.bindLong(13, session.getVpnStartTime());
        String extras = session.getExtras();
        if(extras != null){
            insertStm.bindString(14, extras);
        }else{
            insertStm.bindNull(14);
        }
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

    public List<NetSession> queryAll(){
        List<NetSession> ret;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_ALL_SORT_BY_VPNTIME_SQL, null);
            if(cursor != null && cursor.getCount() > 0){
                ret = new ArrayList<>(cursor.getCount());
                while(cursor.moveToNext()){
                    int protocol = cursor.getInt(2);
                    NetSession session = new NetSession(protocol);
                    session.setUid(cursor.getInt(1));
                    session.setPortKey(cursor.getInt(3));
                    session.setRemoteIp(cursor.getInt(4));
                    session.setRemotePort(cursor.getInt(5));
                    session.setStartTime(cursor.getLong(6));
                    session.sendByte = cursor.getLong(7);
                    session.receiveByte = cursor.getLong(8);
                    session.sendPacket = cursor.getInt(9);
                    session.receivePacket = cursor.getInt(10);
                    session.lastActiveTime = cursor.getLong(11);
                    session.setVpnStartTime(cursor.getLong(12));
                    String extras = cursor.getString(13);
                    if(extras != null){
                        HttpHeader httpHeader = new HttpHeader();
                        httpHeader.fromJson(extras);
                        session.setHttpHeader(httpHeader);
                    }else{
                        session.setHttpHeader(null);
                    }
                    ret.add(session);
                }
                return ret;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            IOUtils.safeClose(cursor);
        }
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
        db.execSQL(DELETE_ALL_SQL);
    }

    @Override
    public void close() throws IOException {
        db.close();
    }
}
