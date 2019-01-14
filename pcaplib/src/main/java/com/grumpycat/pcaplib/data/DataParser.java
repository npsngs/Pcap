package com.grumpycat.pcaplib.data;

import com.grumpycat.pcaplib.util.ThreadPool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by cc.he on 2018/12/7
 */
public class DataParser{
    private String dirPath;
    public DataParser(String dir){
        this.dirPath = dir;
    }

    public void asyncParse(ParseCallback callback){
        ThreadPool.execute(new ParseTask(callback));
    }


    private ParseResult parse(){
        ParseResult result = new ParseResult();
        List<ParseMeta> ret = new ArrayList<>();
        result.setParseMetas(ret);
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if(files != null && files.length > 0){
            for(File f:files){
                if(f.getName().startsWith("req")){
                    ret.add(new ParseMeta(true, f));
                }else if(f.getName().startsWith("resp")){
                    ret.add(new ParseMeta(false, f));
                }
            }
        }
        return result;
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
