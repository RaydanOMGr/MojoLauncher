package net.kdt.pojavlaunch.customcontrols.mouse.cursor;

import android.content.Context;
import android.graphics.drawable.Drawable;

public interface Cursor {
    int getXHotspot();
    int getYHotspot();
    int getWidth();
    int getHeight();
    Drawable getDrawable(Context context);
}
