package com.grumpycat.pcap.ui.base;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by hechengcheng on 2018/7/20
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseRecyclerViewHolder<T>> implements BindDataGetter<T>{
    private List<T> datas;
    public void setData(List<T> dataList){
        if (dataList == null || dataList.size() == 0){
            if (datas != null) {
                datas.clear();
                notifyDataSetChanged();
            }
            return;
        }

        if (datas == null){
            datas = new ArrayList<>();
        }

        datas.clear();
        for(T t:dataList){
            datas.add(t);
        }
        notifyDataSetChanged();
    }


    public void appendData(List<T> dataList){
        if (dataList == null || dataList.size() == 0){
            return;
        }

        if (datas == null){
            datas = new ArrayList<>();
        }

        int start = datas.size();
        for(T t:dataList){
            datas.add(t);
        }
        notifyItemRangeInserted(start, dataList.size());
    }




    @Override
    public void onBindViewHolder(@NonNull BaseRecyclerViewHolder holder, int position) {
        holder.bindData(this);
    }

    @Override
    public int getItemCount() {
        return datas==null?0:datas.size();
    }

    @Override
    public T getItemData(int index){
        return datas==null?null:datas.get(index);
    }

    public void deleteItem(int index){
        if (datas !=null && index >=0 && index < datas.size()){
            datas.remove(index);
            notifyItemRemoved(index);
        }
    }

    public List<T> getAllDatas() {
        if (datas == null){
            return null;
        }

        List<T> results = new ArrayList<>(datas.size());
        for(T t:datas){
            results.add(t);
        }

        return results;
    }

    public void removeAllItem(){
        if (datas != null && datas.size() > 0){
            datas.clear();
            notifyDataSetChanged();
        }
    }

    public abstract View createItemView(@NonNull ViewGroup parent, int viewType);

    protected void onItemClick(int position){

    }
}