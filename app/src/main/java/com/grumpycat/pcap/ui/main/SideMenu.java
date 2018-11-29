package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.ui.base.UiWidget;
import com.grumpycat.pcap.ui.floatwin.FloatingService;
import com.grumpycat.pcaplib.VpnMonitor;

/**
 * Created by cc.he on 2018/11/27
 */
public class SideMenu extends UiWidget implements View.OnClickListener{

    public SideMenu(Activity activity) {
        super(activity);
        findViewById(R.id.tv_instrument).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_instrument:
                if(VpnMonitor.isSingleApp()){
                    FloatingService.showFloatingWindow(getActivity());
                }else{
                    Toast.makeText(getActivity(), "Need Single App", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
