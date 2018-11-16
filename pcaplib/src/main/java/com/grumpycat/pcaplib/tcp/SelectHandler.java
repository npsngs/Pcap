package com.grumpycat.pcaplib.tcp;

import java.nio.channels.SelectionKey;

/**
 * Created by cc.he on 2018/11/12
 */
public interface SelectHandler {
    void onSelected(SelectionKey key);
}
