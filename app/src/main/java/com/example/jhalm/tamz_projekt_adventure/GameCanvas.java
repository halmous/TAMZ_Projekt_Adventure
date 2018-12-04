package com.example.jhalm.tamz_projekt_adventure;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

public class GameCanvas extends View {

    private int width;
    private int height;
    private Bitmap toDraw;

    public GameCanvas(Context context) {
        super(context);
    }

    public GameCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameCanvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if(this.toDraw != null) {
            synchronized (this.toDraw) {
                canvas.drawBitmap(this.toDraw, null, new Rect(0, 0, this.width, this.height), null);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        this.width = w;
        this.height = h;

        super.onSizeChanged( w, h, oldw, oldh);
    }

    public void SetBitmap(Bitmap bitmap)
    {
        if(this.toDraw != null) {
            synchronized (this.toDraw) {
                this.toDraw = bitmap;
            }
        }
        else
        {
            this.toDraw = bitmap;
        }
    }
}
