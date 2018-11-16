package com.grumpycat.pcap.ui.base;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by cc.he on 2018/9/10
 */
public class ListDividerDrawable extends Drawable {
    private int height;
    private Paint paint;
    public ListDividerDrawable(int height, @ColorInt int color) {
        this.height = height;
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setColor(color);
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds != null) {
            canvas.drawRect(bounds, paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
