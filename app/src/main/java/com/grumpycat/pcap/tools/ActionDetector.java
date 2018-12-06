package com.grumpycat.pcap.tools;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by cc.he on 2018/12/6
 */
public class ActionDetector implements View.OnTouchListener{
    private float clickThreshold;
    public ActionDetector(float clickThreshold) {
        this.clickThreshold = clickThreshold;
    }

    private float lastX, lastY;
    private float downX, downY;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX = event.getRawX();
                lastY = event.getRawY();
                downX = lastX;
                downY = lastY;
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getRawX();
                float y = event.getRawY();
                onMove(x - lastX, y - lastY);
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if(isClick(event.getRawX(), event.getRawY())){
                    onClick();
                }else{
                    onMoveEnded();
                }
                break;
        }
        return true;
    }

    private boolean isClick(float upX, float upY){
        float dx = upX - downX;
        float dy = upY - downY;
        return Math.sqrt(dx*dx+dy*dy) < clickThreshold;
    }

    protected void onMove(float dx, float dy){};

    protected void onClick(){};

    protected void onMoveEnded(){};
}