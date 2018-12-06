package com.grumpycat.pcap.ui.base;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by hechengcheng on 2018/7/20
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseHolder<T>> implements BindDataGetter<T>{
    private List<T> data = new ArrayList<>();
    public void setData(List<T> dataList){
        data.clear();
        if (dataList != null && dataList.size() > 0) {
            for (T t : dataList) {
                data.add(t);
            }
        }
        notifyDataSetChanged();
    }


    public void add(List<T> dataList){
        if (dataList == null || dataList.size() == 0){
            return;
        }

        int start = data.size();
        data.addAll(dataList);
        notifyItemRangeInserted(start, dataList.size());
    }

    public void add(T t){
        int pos = data.size();
        data.add(t);
        notifyItemInserted(pos);
    }

    public void add(T t, int index){
        data.add(index, t);
        notifyItemInserted(index);
    }



    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, int position) {
        holder.bindData(this);
    }

    @Override
    public int getItemCount() {
        return data ==null?0: data.size();
    }

    @Override
    public T getItemData(int index){
        return data ==null?null: data.get(index);
    }

    public void remove(int index){
        if (data !=null && index >=0 && index < data.size()){
            data.remove(index);
            notifyItemRemoved(index);
        }
    }

    public List<T> getData() {
        if (data == null){
            return null;
        }

        List<T> results = new ArrayList<>(data.size());
        for(T t: data){
            results.add(t);
        }

        return results;
    }

    public void removeAll(){
        if (data != null && data.size() > 0){
            data.clear();
            notifyDataSetChanged();
        }
    }

    protected abstract @LayoutRes int getItemLayoutRes();
    protected View createItemView(@NonNull ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return inflater.inflate(getItemLayoutRes(), parent, false);
    }
}