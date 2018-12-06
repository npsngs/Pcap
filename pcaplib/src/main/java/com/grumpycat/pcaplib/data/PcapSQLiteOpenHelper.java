package com.grumpycat.pcaplib.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by cc.he on 2018/11/28
 */
public class PcapSQLiteOpenHelper extends SQLiteOpenHelper {
    public PcapSQLiteOpenHelper(Context context) {
        super(context, "pcap.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE sessions ("
                + "id Integer PRIMARY KEY,"
                + "uid Integer,"
                + "protocol Integer,"
                + "portKey Integer, "
                + "remoteIp Integer, "
                + "remotePort Integer, "
                + "startTime Long, "
                + "sendByte Long, "
                + "recvByte Long, "
                + "sendPacket Integer, "
                + "recvPacket Integer, "
                + "lastTimeStamp Long, "
                + "vpnTimeStamp Long, "
                + "extras Text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
