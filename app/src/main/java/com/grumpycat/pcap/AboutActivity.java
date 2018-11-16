package com.grumpycat.pcap;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/6.
 *         Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_about);
        findViewById(R.id.back).setOnClickListener(v -> finish());
    }
}
