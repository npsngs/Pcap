package com.grumpycat.pcap.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.security.KeyChain;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.tools.AppConfigs;
import com.grumpycat.pcap.ui.base.UiWidget;
import com.grumpycat.pcaplib.VpnController;
import com.grumpycat.pcaplib.session.SessionManager;
import com.grumpycat.pcaplib.util.Const;
import com.grumpycat.pcaplib.util.IOUtils;
import com.grumpycat.pcaplib.util.ThreadPool;

import java.io.InputStream;

import javax.security.cert.X509Certificate;

/**
 * Created by cc.he on 2018/11/27
 */
public class SideMenu extends UiWidget implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener{
    private Switch swt_filter_udp;
    private Switch swt_crack_tls;
    private TextView tv_cache_dir;
    private TextView tv_about;

    public SideMenu(Activity activity) {
        super(activity);
        findViewById(R.id.rl_history).setOnClickListener(this);
        findViewById(R.id.rl_cache_clear).setOnClickListener(this);
        swt_filter_udp = findViewById(R.id.swt_filter_udp);
        swt_crack_tls = findViewById(R.id.swt_crack_tls);
        tv_about = findViewById(R.id.tv_about);
        tv_cache_dir = findViewById(R.id.tv_cache_dir);
        tv_cache_dir.setText(Const.BASE_DIR);
        tv_about.setText("v"+getVersionName(activity));
    }

    public static String getVersionName(Context ctx) {
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void onOpen(){
        swt_filter_udp.setOnCheckedChangeListener(null);
        swt_crack_tls.setOnCheckedChangeListener(null);

        swt_filter_udp.setChecked(!AppConfigs.isFilterUdp());
        swt_crack_tls.setChecked(AppConfigs.isCrackTls());

        swt_filter_udp.setOnCheckedChangeListener(this);
        swt_crack_tls.setOnCheckedChangeListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_history:
                getActivity().startActivity(new Intent(getActivity(), HistoryActivity.class));
                break;
            case R.id.rl_cache_clear:
                ThreadPool.execute(()-> {
                    SessionManager.getInstance().clearHistory();
                    getActivity().runOnUiThread(()-> Toast.makeText(
                            getActivity(),
                            getActivity().getString(R.string.clear_finish),
                            Toast.LENGTH_LONG)
                            .show());
                });
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.swt_filter_udp:
                VpnController.setIsUdpNeedSave(isChecked);
                AppConfigs.setFilterUdp(!isChecked);
                break;
            case R.id.swt_crack_tls:
                VpnController.setIsCrackTLS(isChecked);
                AppConfigs.setCrackTls(isChecked);
                if(isChecked){
                    installCert(getActivity());
                }
                break;
        }
    }


    /**
     * 安裝证书
     */
    public void installCert(Context context) {
        InputStream assetsIn = null;
        try {
            Intent intent = KeyChain.createInstallIntent();
            //获取证书流，注意参数为assets目录文件全名
            assetsIn = context.getAssets().open("root.crt");
            byte[] cert = new byte[10240];
            assetsIn.read(cert);
            X509Certificate x509 = X509Certificate.getInstance(cert);
            //将证书传给系统
            intent.putExtra(KeyChain.EXTRA_CERTIFICATE, x509.getEncoded());
            //此处为给证书设置默认别名，第二个参数可自定义，设置后无需用户输入
            intent.putExtra("name", "Grumpy Cat Pcap");
            getActivity().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            IOUtils.safeClose(assetsIn);
        }
    }
}
