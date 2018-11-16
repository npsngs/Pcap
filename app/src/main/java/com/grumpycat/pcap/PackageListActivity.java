package com.grumpycat.pcap;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.grumpycat.pcaplib.util.ThreadProxy;

import java.util.List;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/2/27.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class PackageListActivity extends Activity {

    public static final String SELECT_PACKAGE = "package_select";
    private static final String TAG = "PackageListActivity";
    private ListView packageListView;
    private List<PackageShowInfo> packageShowInfo;
    PackageManager pm;
    private ProgressBar pg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_list);
        pg = findViewById(R.id.pg);

        pm = getPackageManager();
        packageListView = findViewById(R.id.package_list);
        ThreadProxy.getInstance().execute(new Runnable() {

            private ShowPackageAdapter showPackageAdapter;

            @Override
            public void run() {
                packageShowInfo = PackageShowInfo.getPackageShowInfo(getApplicationContext());
                showPackageAdapter = new ShowPackageAdapter();
                runOnUiThread(() -> {
                    packageListView.setAdapter(showPackageAdapter);
                    pg.setVisibility(View.GONE);
                });
            }
        });


        packageListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent();
            if (position != 0) {
                intent.putExtra(SELECT_PACKAGE, packageShowInfo.get(position - 1));
            }
            setResult(RESULT_OK, intent);
            finish();
        });
        findViewById(R.id.back).setOnClickListener(v -> finish());
    }

    class ShowPackageAdapter extends BaseAdapter {
        int ALL = 0;
        int COMMON = 1;
        Drawable defaultDrawable;

        ShowPackageAdapter() {
            defaultDrawable = getResources().getDrawable(R.drawable.sym_def_app_icon);
        }

        @Override
        public int getCount() {
            return packageShowInfo.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Holder holder;
            if (convertView == null) {
                convertView = View.inflate(PackageListActivity.this, R.layout.item_app_list, null);
                holder = new Holder(convertView, position);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
                holder.holderPosition = position;
            }
            if (position == 0) {
                refreshAll(holder);
                return convertView;
            }
            final PackageShowInfo packageShowInfo = PackageListActivity.this.packageShowInfo.get(position - 1);
            if (packageShowInfo.appName == null) {
                holder.appName.setText(packageShowInfo.packageName);
            } else {
                holder.appName.setText(packageShowInfo.appName);
            }
            holder.icon.setImageDrawable(defaultDrawable);
            final View alertIconView = convertView;
            ThreadProxy.getInstance().execute(() -> {
                Holder iconHolder = (Holder) alertIconView.getTag();
                if (iconHolder.holderPosition != position) {
                    return;
                }
                final Drawable drawable = packageShowInfo.applicationInfo.loadIcon(pm);
                runOnUiThread(() -> {
                    Holder iconHolder1 = (Holder) alertIconView.getTag();
                    if (iconHolder1.holderPosition != position) {
                        return;
                    }
                    holder.icon.setImageDrawable(drawable);
                });
            });
            return convertView;
        }

        private void refreshAll(Holder holder) {
            holder.appName.setText(getString(R.string.all));
            holder.icon.setImageDrawable(null);
        }

        private View getAllView(int position, View convertView, ViewGroup parent) {
            View inflate = View.inflate(PackageListActivity.this, R.layout.item_app_list, null);
            ((TextView) inflate.findViewById(R.id.app_name)).setText(getString(R.string.all));
            return inflate;
        }

        class Holder {
            TextView appName;
            ImageView icon;
            View baseView;
            int holderPosition;

            Holder(View view, int position) {
                baseView = view;
                appName = (TextView) view.findViewById(R.id.app_name);
                icon = (ImageView) view.findViewById(R.id.iv_icon);
                this.holderPosition = position;
            }
        }
    }

}
