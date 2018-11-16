package com.grumpycat.pcaplib.protocol;

import java.io.Serializable;

/**
 * Created by cc.he on 2018/11/14
 */
public class HttpHeader implements Serializable{
    public String method;
    public String host;
    public String path;
    public String url;
    public boolean isHttps;
}
