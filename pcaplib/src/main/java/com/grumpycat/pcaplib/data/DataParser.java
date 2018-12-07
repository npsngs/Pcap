package com.grumpycat.pcaplib.data;

import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.ThreadPool;

import java.io.Closeable;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by cc.he on 2018/12/7
 */
public class DataParser implements Closeable{
    private String fileName;
    private int sessionId;
    private RandomAccessFile file;
    public DataParser(String fileName,int sessionId) throws FileNotFoundException {
        this.fileName = fileName;
        this.sessionId = sessionId;
        file = new RandomAccessFile(fileName, "r");
    }

    public void asyncParse(ParseCallback callback){
        ThreadPool.execute(new ParseTask(callback));
    }


    private ParseResult parse() throws Exception{
        ParseResult result = new ParseResult();
        List<ParseMeta> ret = new ArrayList<>();
        result.setParseMetas(ret);
        int offset = 0;
        while (true) {
            try {
                file.readFloat();
                ParseMeta meta = new ParseMeta();
                int size = parseOneFrame(meta, offset, result);
                if(size == -1){
                    break;
                }
                offset += size;
                ret.add(meta);
            } catch (EOFException eof) {
                break;
            }
        }
        return result;
    }

    private int parseOneFrame(ParseMeta meta, int offset,ParseResult result) throws IOException{
        meta.setIpOffset(offset);
        file.seek(offset);
        meta.setIpLength(file.readByte() & 0x0F);
        file.seek(offset+2);
        meta.setTotalLen(file.readShort() & 0xFFFF);
        file.seek(offset+9);
        meta.setProtocol(file.readByte() & 0xFF);
        file.seek(offset+12);
        int srcIp = file.readInt();
        meta.setSend(srcIp == Const.VPN_IP);
        int readSize = meta.getTotalLen();
        if(meta.getTotalLen() > 2048) {
            readSize = 2048;
        }
        byte[] data = new byte[readSize];
        file.seek(offset);
        file.read(data, 0, readSize);
        meta.setData(data);

        int ipDataSize = meta.getTotalLen() - meta.getIpLength();
        if(ipDataSize < 1){
            meta.setDataLength(0);
            return meta.getTotalLen();
        }

        switch (meta.getProtocol()){
            case Const.TCP: {
                int tcpOffset = offset + meta.getIpLength();
                file.seek(tcpOffset+12);
                int tcpLen = file.readByte() & 0xFF;
                meta.setDataOffset(tcpOffset + tcpLen);
                meta.setDataLength(ipDataSize-tcpLen);
                if(!result.isSSL() && meta.getDataLength() > 0){
                    file.seek(meta.getDataOffset());
                     byte firstData = file.readByte();
                     switch (firstData){
                         case 0x16:
                             result.setSSL(true);
                             break;
                     }
                }
            }break;
            case Const.UDP:
                meta.setDataOffset(offset + meta.getIpLength()+8);
                meta.setDataLength(ipDataSize -8);
                break;
            default:
                meta.setDataOffset(offset + meta.getIpLength());
                meta.setDataLength(ipDataSize);
                break;
        }
        return meta.getTotalLen();
    }


    @Override
    public void close() throws IOException {
        file.close();
    }


    private class ParseTask implements Runnable{
        private ParseCallback callback;
        public ParseTask(ParseCallback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                ParseResult ret = parse();
                callback.onParseFinish(ret);
            }catch (Exception e){
                e.printStackTrace();
                callback.onParseFailed();
            }
        }
    }

    public interface ParseCallback{
        void onParseFinish(ParseResult result);
        void onParseFailed();
    }

}
