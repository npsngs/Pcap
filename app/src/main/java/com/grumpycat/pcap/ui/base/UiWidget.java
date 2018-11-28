package com.grumpycat.pcap.ui.base;

import android.app.Activity;
import android.view.View;

/**
 * Created by cc.he on 2018/11/27
 */
public class UiWidget {
    private Activity activity;
    public Activity getActivity() {
        return activity;
    }
    public UiWidget(Activity activity) {
        this.activity = activity;
    }

    public <T extends View> T findViewById(int id) {
        return activity.findViewById(id);
    }
}
