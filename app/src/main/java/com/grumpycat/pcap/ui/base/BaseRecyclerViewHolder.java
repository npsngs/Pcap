package com.grumpycat.pcap.ui.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by hechengcheng on 2018/7/20
 */

public abstract class BaseRecyclerViewHolder<T> extends RecyclerView.ViewHolder{
    public BaseRecyclerViewHolder(View itemView) {
        super(itemView);
        initWithView(itemView);
    }

    protected abstract void initWithView(View itemView);

    public void bindData(BindDataGetter<T> dataGetter){
        onBindData(dataGetter);
    }

    protected abstract void onBindData(BindDataGetter<T> dataGetter);

    public <T extends View> T findViewById(int id) {
        return itemView.findViewById(id);
    }
}