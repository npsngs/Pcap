package com.grumpycat.pcap.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cc.he on 2018/11/27
 */
public class Config {
    public static final long MAX_LOAD = 1024*8;

    private static Context context;
    private static SharedPreferences sp;
    public static void init(Context context){
        Config.context = context.getApplicationContext();
        sp = context.getSharedPreferences("Configs", Context.MODE_PRIVATE);
    }

    private static int[] selectedUids;
    public static int[] getSelectApps(){
        if(selectedUids != null){
            return selectedUids;
        }

        String saveStr = sp.getString("select_uid", null);
        if(TextUtils.isEmpty(saveStr)){
            return null;
        }

        String[] splits = saveStr.split(",");
        if(splits == null || splits.length == 0){
            return null;
        }

        List<String> ss = new ArrayList<>();
        for(int i=0;i<splits.length;i++){
            String s = splits[i];
            if(TextUtils.isEmpty(s)){
                continue;
            }
            ss.add(s);
        }

        int[] uids = new int[ss.size()];
        for(int i=0;i<uids.length;i++){
            String s = ss.get(i);
            uids[i] = Integer.parseInt(s);
        }
        selectedUids = uids;
        return selectedUids;
    }

    public static void saveSelectApps(int[] uids){
        selectedUids = uids;
        if(selectedUids != null){
            String saveStr = "";
            for(int uid:uids){
                saveStr += uid;
                saveStr += ",";
            }
            sp.edit().putString("select_uid", saveStr).apply();
        }else{
            sp.edit().putString("select_uid", null).apply();
        }
    }

}
