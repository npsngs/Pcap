package com.grumpycat.pcaplib.data;

/**
 * Created by cc.he on 2018/11/28
 */
public interface JsonBean {
    String toJson();
    void fromJson(String json) throws Exception;
}
