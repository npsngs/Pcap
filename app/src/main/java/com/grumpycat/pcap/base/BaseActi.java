package com.grumpycat.pcap.base;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.grumpycat.pcap.R;
import com.grumpycat.pcap.ui.dialog.ProgressDialog;

public class BaseActi extends AppCompatActivity {
    private Toolbar toolbar;
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.acti_base, null);
        toolbar = root.findViewById(R.id.toolbar);
        View contentView = inflater.inflate(layoutResID, root, false);

        RelativeLayout.LayoutParams lp =
                (RelativeLayout.LayoutParams) contentView.getLayoutParams();
        lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        lp.addRule(RelativeLayout.BELOW, R.id.toolbar);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        root.addView(contentView,lp);
        super.setContentView(root);
        initView();
    }

    @Override
    public void setContentView(View contentView) {
        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.acti_base, null);
        toolbar = root.findViewById(R.id.toolbar);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.BELOW, R.id.toolbar);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        root.addView(contentView,lp);
        super.setContentView(root);
        initView();
    }

    private ProgressDialog pbDlg;
    private void initView(){
        toolbar.setNavigationIcon(R.drawable.sl_ic_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    public void showProgressBar() {
        if(pbDlg == null){
            pbDlg = new ProgressDialog(this);
        }
        pbDlg.show();
    }

    public void hideProgressBar(){
        if(pbDlg != null){
            pbDlg.dismiss();
        }
    }
}
