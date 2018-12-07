package com.grumpycat.pcaplib.data;

import java.util.List;

/**
 * Created by cc.he on 2018/12/7
 */
public class ParseResult {
    private boolean isSSL = false;
    private boolean isHttp;
    private List<ParseMeta> parseMetas;

    public boolean isSSL() {
        return isSSL;
    }

    public void setSSL(boolean SSL) {
        isSSL = SSL;
    }

    public List<ParseMeta> getParseMetas() {
        return parseMetas;
    }

    public void setParseMetas(List<ParseMeta> parseMetas) {
        this.parseMetas = parseMetas;
    }
}
