package com.grumpycat.pcaplib.data;

import com.grumpycat.pcaplib.util.Const;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by cc.he on 2018/11/16
 */
public class BufferManager {
    private static int maxBufferCount = 30;
    private static ConcurrentLinkedQueue<ByteBuffer> idleList = new ConcurrentLinkedQueue<>();
    public static ByteBuffer getBufferForRead(){
        ByteBuffer buffer = idleList.poll();
        if (buffer == null){
            buffer = ByteBuffer.allocate(Const.MUTE_SIZE);
        }
        buffer.clear();
        return buffer;
    }


    public static void recycleBuffer(ByteBuffer byteBuffer){
        if(idleList.size() < maxBufferCount){
            idleList.add(byteBuffer);
        }
    }
    public static void recycleBuffers(Collection<ByteBuffer> buffers){
        if(idleList.size() < maxBufferCount) {
            idleList.addAll(buffers);
        }
    }


    public static void clear(){
        idleList.clear();
    }

}
