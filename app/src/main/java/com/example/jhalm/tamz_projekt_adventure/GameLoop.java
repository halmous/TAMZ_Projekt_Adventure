package com.example.jhalm.tamz_projekt_adventure;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

public class GameLoop extends Thread {

    private GameCanvas gameCanvas;
    private Map map;
    private boolean active;
    private Bitmap pauseButton;
    private Bitmap joystickIn;
    private Bitmap joystickOut;

    private Double touchStartX;
    private Double touchStartY;
    private Double touchActualX;
    private Double touchActualY;

    public GameLoop(GameCanvas gameCanvas, Map map) {
        this.gameCanvas = gameCanvas;
        this.map = map;
        this.pauseButton = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        this.joystickIn = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
        this.joystickOut = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 64; j++) {
                int x = j - 32;
                int y = i - 32;

                if ((x * x + y * y) <= (32 * 32)) {
                    if (Math.abs(x) >= 5 && Math.abs(x) <= 15 && Math.abs(y) < 25) {
                        this.pauseButton.setPixel(j, i, 0xDD000000);
                    } else {
                        this.pauseButton.setPixel(j, i, 0x77999999);
                    }
                } else {
                    this.pauseButton.setPixel(j, i, Color.TRANSPARENT);
                }
            }
        }

        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                int x = j - 64;
                int y = i - 64;

                if ((x * x + y * y) <= (64 * 64)) {
                    this.joystickIn.setPixel(j, i, 0x77333333);
                } else {
                    this.joystickIn.setPixel(j, i, Color.TRANSPARENT);
                }
            }
        }

        for (int i = 0; i < 192; i++) {
            for (int j = 0; j < 192; j++) {
                int x = j - 96;
                int y = i - 96;

                if ((x * x + y * y) <= (96 * 96)) {
                    this.joystickOut.setPixel(j, i, (Color.GRAY & 0x00FFFFFF) | 0xBB000000);
                } else {
                    this.joystickOut.setPixel(j, i, Color.TRANSPARENT);
                }
            }
        }

        this.touchActualX = this.touchStartX = this.touchActualY = this.touchStartY = -1.0;

        this.gameCanvas.setOnTouchListener(this.touchListener);
    }

    @Override
    public void run() {
        active = true;


        long frameTime = 16;
        while (active) {
            long startTime = System.currentTimeMillis();

            double startX;
            double startY;
            double actualX;
            double actualY;

            synchronized (this.touchStartX) {
                startX = this.touchStartX;
                startY = this.touchStartY;
            }
            synchronized (this.touchActualX) {
                actualX = this.touchActualX;
                actualY = this.touchActualY;
            }

            Bitmap bitmap = Bitmap.createBitmap(16 * 64, 9 * 64, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(map.rooms.get(0).GetGroundBitmap(), 0, 0, null);
            canvas.drawBitmap(this.pauseButton, 0, 0, null);

            if (touchStartX > -1) {
                double xDifference = actualX - startX;
                double yDifference = actualY - startY;
                double distance = Math.sqrt(xDifference * xDifference + yDifference * yDifference);

                canvas.drawBitmap(this.joystickOut, (float) (startX - 96), (float) (startY - 96), null);
                if ((xDifference * xDifference + yDifference * yDifference) <= (32 * 32)) {
                    canvas.drawBitmap(this.joystickIn, (float) (actualX - 64), (float) (actualY - 64), null);
                } else {
                    actualX = (startX + (32.0 * (xDifference / distance)));
                    actualY = (startY + (32.0 * (yDifference / distance)));

                    //Log.d("TAMZ", "X:" + Double.toString(tmpX) + " Y:" + Double.toString(tmpY));
                    canvas.drawBitmap(this.joystickIn, (float) (actualX - 64), (float) (actualY - 64), null);
                }
            }

            this.gameCanvas.SetBitmap(bitmap);
            this.gameCanvas.invalidate();

            frameTime = System.currentTimeMillis() - startTime;

            if(frameTime <= 16 && frameTime > 0)
            {
                try {
                    sleep(16 - frameTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            frameTime = System.currentTimeMillis() - startTime;

            //Log.d("TAMZ", "FPS: " + Long.toString(1000 / frameTime));
        }
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            double x = event.getX();
            double y = event.getY();
            double xRatio = (16 * 64) / (float) gameCanvas.getMeasuredWidth();
            double yRatio = (9 * 64) / (float) gameCanvas.getMeasuredHeight();

            //Log.d("TAMZ", "X:" + Double.toString( gameCanvas.getMeasuredWidth()) + " Y:" + Double.toString( gameCanvas.getMeasuredHeight()));

            if (!(x < 128 && y < 128)) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        synchronized (touchStartX) {
                            touchStartX = x * xRatio;
                            touchStartY = y * yRatio;
                        }
                        synchronized (touchActualX) {
                            touchActualX = x * xRatio;
                            touchActualY = y * yRatio;
                        }
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        synchronized (touchActualX) {
                            touchActualX = x * xRatio;
                            touchActualY = y * yRatio;
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        synchronized (touchStartX) {
                            touchStartX = -1.0;
                            touchStartY = -1.0;
                        }
                        synchronized (touchActualX) {
                            touchActualX = -1.0;
                            touchActualY = -1.0;
                            break;
                        }
                    }
                }
            }

            return true;
        }
    };
}
