package com.grumpycat.pcaplib.protocol;

import com.grumpycat.pcaplib.data.JsonBean;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by cc.he on 2018/11/14
 */
public class HttpHeader implements Serializable, JsonBean, Cloneable{
    public String method="";
    public String host="";
    public String path="";
    public String url="";
    public boolean isHttps;

    @Override
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("method", method);
            json.put("host", host);
            json.put("path", path);
            json.put("url", url);
            json.put("isHttps", isHttps);
            return json.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public HttpHeader copy(){
        try {
            return (HttpHeader) this.clone();

        }catch (Exception e){
            e.printStackTrace();
        }

        HttpHeader ret = new HttpHeader();
        ret.set(this);
        return ret;
    }

    public void set(HttpHeader httpHeader){
        this.method = httpHeader.method;
        this.host = httpHeader.host;
        this.path = httpHeader.path;
        this.url = httpHeader.url;
        this.isHttps = httpHeader.isHttps;
    }




    @Override
    public void fromJson(String jsonStr) throws Exception{
            JSONObject json = new JSONObject(jsonStr);
            method = json.getString("method");
            host = json.getString("host");
            path = json.getString("path");
            url = json.getString("url");
            isHttps = json.getBoolean("isHttps");
    }
}
