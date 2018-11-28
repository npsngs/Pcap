package com.grumpycat.pcap;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.grumpycat.pcaplib.VpnMonitor;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.ThreadPool;

import java.io.File;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/5.
 *         Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class SettingFragment extends BaseFragment {

    private Handler handler;
    private ProgressBar pb;
    private CheckBox includeCurrentCapture;
    private boolean isRunning;
    private SharedPreferences sp;
    private CheckBox cbShowUDP;
    private CheckBox cbSaveUDP;
    private boolean saveUDP;
    private boolean showUDP;

    @Override
    int getLayout() {
        return R.layout.fragment_setting;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.clear_cache_container).setOnClickListener(v -> {
            if (isDeleting) {
                return;
            }
            isDeleting = true;
            pb.setVisibility(View.VISIBLE);
            clearHistoryData();
        });
        view.findViewById(R.id.about_container)
                .setOnClickListener(v -> startActivity(new Intent(getActivity(), AboutActivity.class)));
        cbShowUDP = view.findViewById(R.id.show_udp);
        cbSaveUDP = view.findViewById(R.id.save_udp);
        sp = getContext().getSharedPreferences(Const.VPN_SP_NAME, Context.MODE_PRIVATE);
        saveUDP = sp.getBoolean(Const.IS_UDP_NEED_SAVE, false);
        showUDP = sp.getBoolean(Const.IS_UDP_SHOW, false);
        cbSaveUDP.setChecked(saveUDP);
        cbShowUDP.setChecked(showUDP);
        cbShowUDP.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showUDP = isChecked;
            sp.edit().putBoolean(Const.IS_UDP_SHOW, showUDP).apply();
        });
        cbSaveUDP.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveUDP = isChecked;
            sp.edit().putBoolean(Const.IS_UDP_NEED_SAVE, saveUDP).apply();
        });
        includeCurrentCapture = view.findViewById(R.id.check_current_capture);

        pb = view.findViewById(R.id.pb);
        handler = new Handler();
    }

    private boolean isDeleting;

    private void clearHistoryData() {
        ThreadPool.execute(() -> {

            File file = new File(Const.BASE_DIR);
            FileUtils.deleteFile(file, pathname -> {
                if (includeCurrentCapture.isChecked()) {
                    return true;
                }
                if (!pathname.exists()) {
                    return false;
                }

                String lastVpnStartTimeStr = VpnMonitor.getVpnStartTimeStr();

                String absolutePath = pathname.getAbsolutePath();
                //如果所选择文件是最近一次产生的，则不删除
                return !absolutePath.contains(lastVpnStartTimeStr);
            });
            isDeleting = false;
            handler.post(() -> {
                pb.setVisibility(View.GONE);
                showMessage(getString(R.string.success_clear_history_data));
            });
        });
    }

    private void showMessage(String string) {
        Toast.makeText(getActivity(), string, Toast.LENGTH_SHORT).show();
    }
}
