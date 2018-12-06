package com.grumpycat.pcap.ui.base;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.Util;

/**
 * Created by cc.he on 2018/12/6
 */
public class SingleList{
    private View root;
    private Activity activity;
    private RecyclerView rcv;
    public SingleList(View root) {
        this.root = root;
        init();
    }
    public SingleList(Activity activity){
        this.activity = activity;
        init();
    }

    private void init(){
        Context context = getContext();
        rcv = findViewById(R.id.rcv);
        rcv.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration did = new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL);
        did.setDrawable(new ListDividerDrawable(
                Util.dp2px(context, 1f),
                0xffeeeeee));
        rcv.addItemDecoration(did);
    }

    private Context getContext(){
        if(activity != null){
            return activity;
        }
        return root.getContext();
    }


    private  <T extends View> T findViewById(int id) {
        if(activity != null){
            return activity.findViewById(id);
        }
        return root.findViewById(id);
    }


    public void setAdapter(BaseAdapter adapter){
        rcv.setAdapter(adapter);
    }
}
