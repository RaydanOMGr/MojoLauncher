package net.kdt.pojavlaunch.customcontrols.mouse;

import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.customcontrols.mouse.cursor.Cursor;
import net.kdt.pojavlaunch.GrabListener;

import net.kdt.pojavlaunch.customcontrols.mouse.cursor.CursorUpdateListener;
import net.kdt.pojavlaunch.customcontrols.mouse.cursor.StandardCursor;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import org.lwjgl.glfw.CallbackBridge;

import java.util.Map;

/**
 * Class dealing with the virtual mouse
 */
public class Touchpad extends View implements GrabListener, AbstractTouchpad {
    /* Whether the Touchpad should be displayed */
    private boolean mDisplayState;
    private Cursor mCursor;
    private Map<Long, Cursor> mCursorMap;
    /* Mouse pointer icon used by the touchpad */
    private Drawable mMousePointerDrawable;
    private float mMouseX, mMouseY;
    public Touchpad(@NonNull Context context) {
        this(context, null);
    }

    public Touchpad(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /** Enable the touchpad */
    private void _enable(){
        setVisibility(VISIBLE);
        placeMouseAt(currentDisplayMetrics.widthPixels / 2f, currentDisplayMetrics.heightPixels / 2f);
    }

    /** Disable the touchpad and hides the mouse */
    private void _disable(){
        setVisibility(GONE);
    }

    /** @return The new state, enabled or disabled */
    public boolean switchState(){
        mDisplayState = !mDisplayState;
        if(!CallbackBridge.isGrabbing()) {
            if(mDisplayState) _enable();
            else _disable();
        }
        return mDisplayState;
    }

    public void placeMouseAt(float x, float y) {
        mMouseX = x;
        mMouseY = y;
        updateMousePosition();
    }

    private void sendMousePosition() {
        CallbackBridge.sendCursorPos((mMouseX * LauncherPreferences.PREF_SCALE_FACTOR), (mMouseY * LauncherPreferences.PREF_SCALE_FACTOR));
    }

    private void updateMousePosition() {
        sendMousePosition();
        // I wanted to implement a dirty rect for this, but it is ignored since API level 21
        // (which is our min API)
        // Let's hope the "internally calculated area" is good enough.
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float scale = LauncherPreferences.PREF_MOUSESCALE;
        canvas.translate(
                mMouseX - (mCursor.getXHotspot() * scale),
                mMouseY - (mCursor.getYHotspot() * scale)
        );
        mMousePointerDrawable.draw(canvas);
    }

    private void init(){
        // Setup mouse pointer
        mCursorMap = new ArrayMap<>();
        CursorUpdateListener onUpdate = ((cursor) -> post(() -> {
            if(!mCursorMap.containsKey(cursor)) {
                mCursorMap.put(cursor, CallbackBridge.getCursor(cursor));
            }
            mCursor = mCursorMap.get(cursor);
            setupMousePointer();
            invalidate();
        }));
        CursorUpdateListener onDestroy = ((cursor) -> {
            if(!mCursorMap.containsKey(cursor)) return;
            if(mCursorMap.get(cursor) == mCursor) mCursor = new StandardCursor();
            mCursorMap.remove(cursor);
        });
        addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                CallbackBridge.addCursorUpdateListener(onUpdate);
                CallbackBridge.addCursorDestroyListener(onDestroy);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                CallbackBridge.removeCursorUpdateListener(onUpdate);
                CallbackBridge.removeCursorDestroyListener(onDestroy);
            }
        });


        setFocusable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setDefaultFocusHighlightEnabled(false);
        }

        // When the game is grabbing, we should not display the mouse
        disable();
        mDisplayState = false;
    }

    private void setupMousePointer() {
        mMousePointerDrawable = mCursor.getDrawable(getContext());
        mMousePointerDrawable.setBounds(
                0, 0,
                (int) (mCursor.getWidth() * LauncherPreferences.PREF_MOUSESCALE),
                (int) (mCursor.getHeight() * LauncherPreferences.PREF_MOUSESCALE)
        );
    }

    @Override
    public void onGrabState(boolean isGrabbing) {
        post(()->updateGrabState(isGrabbing));
    }
    private void updateGrabState(boolean isGrabbing) {
        if(!isGrabbing) {
            if(mDisplayState && getVisibility() != VISIBLE) _enable();
            if(!mDisplayState && getVisibility() == VISIBLE) _disable();
        }else{
            if(getVisibility() != View.GONE) _disable();
        }
    }

    @Override
    public boolean getDisplayState() {
        return mDisplayState;
    }

    @Override
    public void applyMotionVector(float x, float y) {
        mMouseX = Math.max(0, Math.min(currentDisplayMetrics.widthPixels, mMouseX + x * LauncherPreferences.PREF_MOUSESPEED));
        mMouseY = Math.max(0, Math.min(currentDisplayMetrics.heightPixels, mMouseY + y * LauncherPreferences.PREF_MOUSESPEED));
        updateMousePosition();
    }

    @Override
    public void enable(boolean supposed) {
        if(mDisplayState) return;
        mDisplayState = true;
        if(supposed && CallbackBridge.isGrabbing()) return;
        _enable();
    }

    @Override
    public void disable() {
        if(!mDisplayState) return;
        mDisplayState = false;
        _disable();
    }
}
