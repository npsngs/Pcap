/*
** Copyright 2015, Mohamed Naufal
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package com.grumpycat.pcap;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.grumpycat.pcap.appinfo.AppInfo;
import com.grumpycat.pcap.appinfo.AppManager;
import com.grumpycat.pcaplib.VpnController;
import com.grumpycat.pcaplib.VpnMonitor;

import java.util.ArrayList;

import static com.grumpycat.pcaplib.util.Const.DEFAULT_PACAGE_NAME;
import static com.grumpycat.pcaplib.util.Const.DEFAULT_PACKAGE_ID;
import static com.grumpycat.pcaplib.util.Const.VPN_SP_NAME;

public class VPNCaptureActivity extends FragmentActivity {
    private static final int REQUEST_PACKAGE = 103;
    private static final int REQUEST_STORAGE_PERMISSION = 104;
    private static String TAG = "VPNCaptureActivity";




    private VpnMonitor.StatusListener statusListener = new VpnMonitor.StatusListener() {
        @Override
        public void onVpnStart() {
            handler.post(()->vpnButton.setImageResource(R.mipmap.ic_stop));
        }

        @Override
        public void onVpnStop() {
            handler.post(()->vpnButton.setImageResource(R.mipmap.ic_start));
        }
    };



    private ImageView vpnButton;
    private TextView packageId;
    private SharedPreferences sharedPreferences;
    private String selectPackage;
    private String selectName;
    private ArrayList<BaseFragment> baseFragments;
    private TabLayout tabLayout;
    private FragmentPagerAdapter simpleFragmentAdapter;
    private ViewPager viewPager;
    String[] needPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn_capture);
        vpnButton = findViewById(R.id.vpn);
        vpnButton.setOnClickListener(v -> {
          if(VpnMonitor.isVpnRunning()){
              closeVpn();
          }else {
              startVPN();
          }
        });
        packageId = findViewById(R.id.package_id);

        sharedPreferences = getSharedPreferences(VPN_SP_NAME, MODE_PRIVATE);
        selectPackage = sharedPreferences.getString(DEFAULT_PACKAGE_ID, null);
        selectName = sharedPreferences.getString(DEFAULT_PACAGE_NAME, null);
        packageId.setText(selectName != null ? selectName :
                selectPackage != null ? selectPackage : getString(R.string.all));

        VpnMonitor.setAllowPackageName(selectPackage);

        vpnButton.setEnabled(true);
        //  summerState = findViewById(R.id.summer_state);
        findViewById(R.id.select_package).setOnClickListener(v -> {
            Intent intent = new Intent(VPNCaptureActivity.this, AppListActivity.class);
            startActivityForResult(intent, REQUEST_PACKAGE);
        });
        initChildFragment();
        initViewPager();
        initTab();
        //推荐用户进行留评
        boolean hasFullUseApp = sharedPreferences.getBoolean(AppConstants.HAS_FULL_USE_APP, false);
        if (hasFullUseApp) {
            boolean hasShowRecommand = sharedPreferences.getBoolean(AppConstants.HAS_SHOW_RECOMMAND, false);
            if (!hasShowRecommand) {
                sharedPreferences.edit().putBoolean(AppConstants.HAS_SHOW_RECOMMAND, true).apply();
                showRecommand();
            } else {
                requestStoragePermission();
            }

        } else {
            requestStoragePermission();
        }
        handler = new Handler();
        VpnMonitor.setStatusListener(statusListener);
        new Thread(){
            @Override
            public void run() {
                AppManager.loadAppInfo(getApplication());
            }
        }.start();
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, needPermissions, REQUEST_STORAGE_PERMISSION);
    }

    private void showRecommand() {
        new AlertDialog
                .Builder(this)
                .setTitle(getString(R.string.do_you_like_the_app))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    showGotoStarDialog();
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                    showGotoDiscussDialog();
                    dialog.dismiss();
                })
                .show();


    }

    private void showGotoStarDialog() {
        new AlertDialog
                .Builder(this)
                .setTitle(getString(R.string.do_you_want_star))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    String url = "https://github.com/huolizhuminh/NetWorkPacketCapture";

                    launchBrowser(url);
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showGotoDiscussDialog() {
        new AlertDialog
                .Builder(this)
                .setTitle(getString(R.string.go_to_give_the_issue))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    String url = "https://github.com/huolizhuminh/NetWorkPacketCapture/issues";
                    launchBrowser(url);
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void launchBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.d(TAG, "failed to launchBrowser " + e.getMessage());
        }
    }

    private void initViewPager() {
        simpleFragmentAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return baseFragments.get(position);
            }

            @Override
            public int getCount() {
                return baseFragments.size();
            }
        };
        viewPager = findViewById(R.id.container_vp);
        viewPager.setAdapter(simpleFragmentAdapter);
    }

    private void initTab() {
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        String[] tabTitle = getResources().getStringArray(R.array.tabs);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setText(tabTitle[i]);
        }
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //   viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void initChildFragment() {
        baseFragments = new ArrayList<>();
        // BaseFragment captureFragment = new SimpleFragment();
        BaseFragment captureFragment = new CaptureFragment();
        BaseFragment historyFragment = new HistoryFragment();
        BaseFragment settingFragment = new SettingFragment();
        baseFragments.add(captureFragment);
        baseFragments.add(historyFragment);
        baseFragments.add(settingFragment);

    }

    private void closeVpn() {
        VpnController.stopVpn(this);
    }

    private void startVPN() {
        Intent intent = VpnController.startVpn(this);
        if (intent != null) {
            startActivityForResult(intent, 1024);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VpnMonitor.setStatusListener(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1024 && resultCode == RESULT_OK) {
            VpnController.startVpn(this);
        } else if (requestCode == REQUEST_PACKAGE && resultCode == RESULT_OK) {
            int appUid = data.getIntExtra("app_uid", 0);
            AppInfo appInfo = AppManager.getApp(appUid);
            if (appInfo == null) {
                VpnMonitor.setAllowPackageName(null);
                selectPackage = null;
                selectName = null;
            } else {
                VpnMonitor.setAllowPackageName(appInfo.pkgName);
                selectPackage = appInfo.pkgName;
                selectName = appInfo.name;
            }
            packageId.setText(selectName != null ? selectName :
                    selectPackage != null ? selectPackage : getString(R.string.all));
            vpnButton.setEnabled(true);
            sharedPreferences.edit().putString(DEFAULT_PACKAGE_ID, selectPackage)
                    .putString(DEFAULT_PACAGE_NAME, selectName).apply();
        }
    }


}
