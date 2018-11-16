package com.grumpycat.pcaplib;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;

/**
 * Created by cc.he on 2018/11/13
 */
public class VpnController {
    public static final String OP =     "OP";
    public static final int OP_NOP =    0;
    public static final int OP_START =  1;
    public static final int OP_STOP =   2;

    private static boolean udpNeedSave = false;

    public static Intent startVpn(Context context){
        Intent intent = VpnService.prepare(context);
        if (intent != null){
            return intent;
        }

        intent = new Intent(context, GVpnService.class);
        intent.putExtra(OP, OP_START);
        context.startService(intent);
        return null;
    }

    public static void stopVpn(Context context){
        Intent intent = new Intent(context, GVpnService.class);
        intent.putExtra(OP, OP_STOP);
        context.startService(intent);
    }

    public static int fetchOP(Intent data){
        return data.getIntExtra(OP, OP_NOP);
    }


    public static boolean isUdpNeedSave() {
        return udpNeedSave;
    }

    public static void setIsUdpNeedSave(boolean udpNeedSave) {
        VpnController.udpNeedSave = udpNeedSave;
    }
}
