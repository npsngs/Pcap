package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.grumpycat.pcap.R;
import com.grumpycat.pcaplib.appinfo.AppInfo;
import com.grumpycat.pcaplib.appinfo.AppManager;

/**
 * Created by cc.he on 2018/11/29
 */
public class SessionListActivity extends Activity {

    public static void gotoActi(Activity from, int uid){
        Intent intent = new Intent(from, SessionListActivity.class);
        intent.putExtra("uid", uid);
        from.startActivity(intent);
    }

    private int uid;
    private AppInfo appInfo;
    private CaptureList captureList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_session_list);
        uid = getIntent().getIntExtra("uid", 0);
        findViewById(R.id.btn_left).setOnClickListener((v)->finish());
        TextView tv_title = findViewById(R.id.tv_title);
        if(uid != 0){
            appInfo = AppManager.getApp(uid);
            tv_title.setText(appInfo.name);
        }else{
            tv_title.setText(R.string.unknow);
        }

        captureList = new CaptureList(this);
        captureList.showSingleApp(uid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        captureList.onResume();
    }

}
