package com.grumpycat.pcaplib.util;

import android.os.Environment;

import java.util.Locale;

/**
 * Created by cc.he on 2018/11/13
 */
public interface Const {
    /***************
     *  config
     ***************/
    boolean LOG_ON = false;
    int MUTE_SIZE = 2560;
    Locale LOCALE = Locale.CHINA;

    /*会话保存的数量上限*/
    int SESSION_MAX_COUNT = 64;
    int SESSION_MAX_SAVE_QUEUE = 32;

    /*会话保存的时间上限*/
    long SESSION_MAX_TIMEOUT = 60 * 1000L;

    int BUFFER_SIZE = 2560;
    int MAX_PAYLOAD_SIZE = 2520;
    String BASE_DIR = Environment.getExternalStorageDirectory() + "/Pcap/";
    String CACHE_DIR = BASE_DIR+ "cache/";
    String DATA_DIR = BASE_DIR + "data/";
    String CONFIG_DIR=BASE_DIR+"config/";
    String VPN_SP_NAME="vpn_sp_name";
    String IS_UDP_NEED_SAVE="isUDPNeedSave";
    String IS_UDP_SHOW = "isUDPShow";
    String DEFAULT_PACKAGE_ID = "default_package_id";
    String DEFAULT_PACKAGE_NAME = "default_package_name";
    boolean IS_NEED_GZIP = false;

    int VPN_IP = CommonMethods.ipStringToInt("10.8.0.2");

    /***************
     *  const
     ***************/
    int IP = 0x0800;
    int TCP = 6;
    int UDP = 17;
    int ICMP = 1;

    int HTTP = 0xCFF6;
    int HTTPS = 0xDFF6;

}
