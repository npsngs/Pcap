package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.ui.base.ListDividerDrawable;
import com.grumpycat.pcap.ui.base.UiWidget;
import com.grumpycat.pcap.ui.base.Util;

/**
 * Created by cc.he on 2018/11/27
 */
public class CaptureList extends UiWidget {
    private RecyclerView rcv;
    public CaptureList(Activity activity) {
        super(activity);
        rcv = findViewById(R.id.rcv);
        rcv.setLayoutManager(new LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration did = new DividerItemDecoration(activity,
                DividerItemDecoration.VERTICAL);
        did.setDrawable(new ListDividerDrawable(
                Util.dp2px(activity, 1f),
                0xffeeeeee));
        rcv.addItemDecoration(did);


    }
}
