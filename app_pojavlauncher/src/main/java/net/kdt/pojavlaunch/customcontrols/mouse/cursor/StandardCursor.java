package net.kdt.pojavlaunch.customcontrols.mouse.cursor;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import git.artdeell.mojo.R;

public class StandardCursor implements Cursor {
    private Drawable drawable;

    @Override
    public int getXHotspot() {
        return 0;
    }

    @Override
    public int getYHotspot() {
        return 0;
    }

    @Override
    public int getWidth() {
        return 36;
    }

    @Override
    public int getHeight() {
        return 54;
    }

    @Override
    public Drawable getDrawable(Context context) {
        if(drawable != null) return drawable;
        return drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mouse_pointer, context.getTheme());
    }
}
