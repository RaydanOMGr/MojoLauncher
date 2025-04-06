package net.kdt.pojavlaunch.customcontrols.mouse.cursor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.lwjgl.glfw.CallbackBridge;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ByteBufferCursor implements Cursor {
    public static final int X_HOT_OFFSET = 0;
    public static final int Y_HOT_OFFSET = 4;
    public static final int WIDTH_OFFSET = 4 * 2;
    public static final int HEIGHT_OFFSET = 4 * 3;
    public static final int PIXELS_OFFSET = 4 * 4;

    private Drawable drawable;
    private final ByteBuffer buffer;

    public ByteBufferCursor(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int getXHotspot() {
        buffer.position(0);
        return buffer.getInt(X_HOT_OFFSET);
    }

    @Override
    public int getYHotspot() {
        buffer.position(0);
        return buffer.getInt(Y_HOT_OFFSET);
    }

    @Override
    public int getWidth() {
        buffer.position(0);
        return buffer.getInt(WIDTH_OFFSET);
    }

    @Override
    public int getHeight() {
        buffer.position(0);
        return buffer.getInt(HEIGHT_OFFSET);
    }

    @Override
    public Drawable getDrawable(Context context) {
        if(drawable != null) return drawable;

        int width = getWidth();
        int height = getHeight();
        byte[] pixels = getPixels();
        int[] argbPixels = new int[width * height];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int offset = (i * width + j) * 4;
                int r = pixels[offset] & 0xFF;
                int g = pixels[offset + 1] & 0xFF;
                int b = pixels[offset + 2] & 0xFF;
                int a = pixels[offset + 3] & 0xFF;
                argbPixels[i * width + j] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(argbPixels, width, height, Bitmap.Config.ARGB_8888);
        return drawable = new BitmapDrawable(context.getResources(), bitmap);
    }

    public byte[] getPixels() {
        int size = getWidth() * getHeight() * 4;
        byte[] pixels = new byte[size];
        buffer.position(PIXELS_OFFSET);
        buffer.get(pixels, 0, size);
        return pixels;
    }

    @Override
    protected void finalize() throws Throwable {
        CallbackBridge.nativeDeallocateDirectByteBuffer(buffer);
        super.finalize();
    }
}
