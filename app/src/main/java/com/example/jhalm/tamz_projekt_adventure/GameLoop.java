package com.example.jhalm.tamz_projekt_adventure;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayDeque;
import java.util.Deque;

public class GameLoop extends Thread {

    public static final int MAX_30_FPS = 32;
    public static final int MAX_60_FPS = 16;
    public static final int MAX_120_FPS = 8;

    public static final int STATUS_RUN = 0;
    public static final int STATUS_END = 1;
    public static final int STATUS_STOP = 2;
    public static final int STATUS_PAUSE = 3;

    private GameCanvas gameCanvas;
    private Map map;
    private boolean active;
    private boolean pause;
    private Bitmap pauseButton;
    private Bitmap joystickIn;
    private Bitmap joystickOut;

    private Double touchStartX;
    private Double touchStartY;
    private Double touchActualX;
    private Double touchActualY;

    private Player player;

    private long frameRate;

    private EndHandler onEnd;
    private Result result;

    private Vibrator vibrator;

    private Integer status;

    private final double movePerMs = 0.2;
    private final double npcRatio = 0.8;

    private final long[][] vibrationTimings = {{500}, {900, 100, 900, 100, 900, 100}, {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100}, {50}};
    private final int[][] vibrationAmplitude = {{VibrationEffect.DEFAULT_AMPLITUDE}, {VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE, 0}, {VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE, 0}, {VibrationEffect.DEFAULT_AMPLITUDE}};

    public GameLoop(GameCanvas gameCanvas, Map map, long frameRate, EndHandler onEnd, Vibrator vibrator) {
        this.gameCanvas = gameCanvas;
        this.map = map;
        this.pauseButton = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        this.joystickIn = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
        this.joystickOut = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888);
        this.frameRate = frameRate;
        this.onEnd = onEnd;

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

        this.player = new Player(3);
        this.RespawnPlayer();

        for(int i = 0; i < this.map.rooms.size(); i++)
        {
            for(int j = 0; j < this.map.rooms.get(i).NPCCount(); j++)
            {
                this.map.rooms.get(i).GetNPC(j).x *= 64;
                this.map.rooms.get(i).GetNPC(j).y *= 64;
            }
        }

        this.vibrator = vibrator;

        this.status = STATUS_RUN;
        this.pause = false;
        this.gameCanvas.setOnTouchListener(this.touchListener);
    }

    @Override
    public void run() {
        this.active = true;

        synchronized (this.status)
        {
            this.status = STATUS_RUN;
        }

        this.player.startTime = System.currentTimeMillis();
        long frameTime = this.frameRate;
        long fps = 100 / this.frameRate;
        while (active) {
            long startTime = System.currentTimeMillis();

            double startX;
            double startY;
            double actualX;
            double actualY;

            double movePerFrame = frameTime * this.movePerMs;

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
            canvas.setDensity(0);



            if (touchStartX > -1)
            {
                double xDifference = actualX - startX;
                double yDifference = actualY - startY;
                double distance = Math.sqrt(xDifference * xDifference + yDifference * yDifference);

                if ((xDifference * xDifference + yDifference * yDifference) > (32 * 32))
                {
                    actualX = (startX + (32.0 * (xDifference / distance)));
                    actualY = (startY + (32.0 * (yDifference / distance)));

                    //Log.d("TAMZ", "X:" + Double.toString(tmpX) + " Y:" + Double.toString(tmpY));
                    canvas.drawBitmap(this.joystickIn, (float) (actualX - 64), (float) (actualY - 64), null);
                }
            }

            double oldPlayerX = this.player.x;
            this.player.x += ((actualX - startX) / 32) * movePerFrame;
            int arrayPosition = ((int)(this.player.x / 64)) + ((int)(this.player.y / 64)) * this.map.rooms.get(this.player.room).GetSize()[0];
            if(this.player.x < 0)
            {
                this.player.x = 0;
            }
            else if (this.player.x > ((this.map.rooms.get(this.player.room).GetSize()[0] - 1) * 64))
            {
                this.player.x = (this.map.rooms.get(this.player.room).GetSize()[0] - 1) * 64;
            }
            else if((this.player.x - oldPlayerX) < 0 && (this.map.rooms.get(this.player.room).GetCollision(arrayPosition) > 0 || ((this.player.y - (int) (this.player.y)) > 0 && this.map.rooms.get(player.room).GetCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]) > 0)))
            {
                this.player.x = Math.floor(oldPlayerX);
            }
            else if((this.player.x - oldPlayerX) > 0 && (this.map.rooms.get(this.player.room).GetCollision(arrayPosition + 1 ) > 0 || ((this.player.y - (int) (this.player.y)) > 0 && this.map.rooms.get(player.room).GetCollision(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]) > 0)))
            {
                this.player.x = Math.ceil(oldPlayerX);
            }
            else if((this.player.x - oldPlayerX) < 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition) != null)
            {
                if(this.ItemCollision(arrayPosition))
                {
                    this.player.x = Math.floor(oldPlayerX);
                }
            }
            else if((this.player.x - oldPlayerX) < 0 && (this.player.y - (int) (this.player.y)) > 0 && this.map.rooms.get(player.room).GetItem(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]) != null)
            {
                if(this.ItemCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]))
                {
                    this.player.x = Math.ceil(oldPlayerX);
                }
            }
            else if((this.player.x - oldPlayerX) > 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition + 1 ) != null)
            {
                if(this.ItemCollision(arrayPosition + 1))
                {
                    this.player.x = Math.ceil(oldPlayerX);
                }
            }
            else if((this.player.x - oldPlayerX) > 0 && (this.player.y - (int) (this.player.y)) > 0 && this.map.rooms.get(player.room).GetItem(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]) != null)
            {
                if(this.ItemCollision(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]))
                {
                    this.player.x = Math.ceil(oldPlayerX);
                }
            }

            double oldPlayerY = this.player.y;
            this.player.y += ((actualY - startY) / 32) * movePerFrame;
            arrayPosition = ((int)(this.player.x / 64)) + ((int)(this.player.y / 64)) * this.map.rooms.get(this.player.room).GetSize()[0];
            if(this.player.y < 0)
            {
                this.player.y = 0;
            }
            else if(this.player.y > ((this.map.rooms.get(this.player.room).GetSize()[1] - 1) * 64))
            {
                this.player.y = (this.map.rooms.get(this.player.room).GetSize()[1] - 1) * 64;
            }
            else if((this.player.y - oldPlayerY) < 0 && (this.map.rooms.get(this.player.room).GetCollision(arrayPosition) > 0 || ((this.player.x - (int)(this.player.x)) > 0 && this.map.rooms.get(this.player.room).GetCollision(arrayPosition + 1) > 0)))
            {
                this.player.y = Math.floor(oldPlayerY);
            }
            else if((this.player.y - oldPlayerY) > 0 && (this.map.rooms.get(this.player.room).GetCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0] ) > 0 || ((this.player.x - (int)(this.player.x)) > 0 && this.map.rooms.get(this.player.room).GetCollision(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0] ) > 0)))
            {
                this.player.y = Math.ceil(oldPlayerY);
            }
            else if((this.player.y - oldPlayerY) < 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition) != null)
            {
                if(this.ItemCollision(arrayPosition))
                {
                    this.player.y = Math.floor(oldPlayerY);
                }
            }
            else if((this.player.y - oldPlayerY) < 0 && (this.player.x - (int)(this.player.x)) > 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition + 1) != null)
            {
                if(this.ItemCollision(arrayPosition + 1))
                {
                    this.player.y = Math.floor(oldPlayerY);
                }
            }
            else if((this.player.y - oldPlayerY) > 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0] ) != null)
            {
                if(this.ItemCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]))
                {
                    this.player.y = Math.ceil(oldPlayerY);
                }
            }
            else if((this.player.y - oldPlayerY) > 0 && (this.player.x - (int)(this.player.x)) > 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]) != null)
            {
                if(this.ItemCollision(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]))
                {
                    this.player.y = Math.ceil(oldPlayerY);
                }
            }

            arrayPosition = ((int)(this.player.x / 64)) + ((int)(this.player.y / 64)) * this.map.rooms.get(this.player.room).GetSize()[0];

            float displayGroundX;
            float displayGroundY;
            float displayPlayerX;
            float displayPlayerY;

            if((8 * 64) >= this.player.x)
            {
                displayGroundX = 0;
                displayPlayerX = (float) this.player.x;
            }
            else if((map.rooms.get(this.player.room).GetGroundBitmap().getWidth() - (8 * 64)) <= this.player.x)
            {
                displayGroundX = ((16 * 64) - map.rooms.get(this.player.room).GetGroundBitmap().getWidth());
                displayPlayerX = (float) ((16 * 64) - (map.rooms.get(this.player.room).GetGroundBitmap().getWidth() - this.player.x));
            }
            else
            {
                displayGroundX =(float) (8 * 64 - this.player.x);
                displayPlayerX = 8 * 64;
            }

            if((4.5 * 64) >= this.player.y)
            {
                displayGroundY = 0;
                displayPlayerY = (float) this.player.y;
            }
            else if((map.rooms.get(this.player.room).GetGroundBitmap().getHeight() - (4.5 * 64)) <= this.player.y)
            {
                displayGroundY = (float) ((9 * 64) - map.rooms.get(this.player.room).GetGroundBitmap().getHeight());
                displayPlayerY = (float) ((9 * 64) - (map.rooms.get(this.player.room).GetGroundBitmap().getHeight() - this.player.y));
            }
            else
            {
                displayGroundY =(float) (4.5 * 64 - this.player.y);
                displayPlayerY = (float) (4.5 * 64);
            }

            canvas.drawBitmap(map.rooms.get(this.player.room).GetGroundBitmap(), displayGroundX, displayGroundY, null);

            int fromX = (int) (0 - displayGroundX) / 64;
            int fromY = (int) (0 - displayGroundY) / 64;
            int toX = ((int) (0 - displayGroundX) / 64) + this.map.rooms.get(this.player.room).GetSize()[0] + 1;
            int toY = ((int) (0 - displayGroundY) / 64) + this.map.rooms.get(this.player.room).GetSize()[1] + 1;

            for(int i = fromY; i < toY; i++)
            {
                for(int j = fromX; j < toX; j++)
                {
                    int arrayPos = j + i * this.map.rooms.get(this.player.room).GetSize()[0];
                    if(this.map.rooms.get(this.player.room).GetItem(arrayPos) != null)
                        canvas.drawBitmap(this.map.items.get(this.map.rooms.get(this.player.room).GetItem(arrayPos)[0]), (j * 64) + displayGroundX, (i * 64 ) + displayGroundY, null);
                }
            }

            canvas.drawBitmap(map.avatars.get(0), displayPlayerX, (float) displayPlayerY, null);

            for(int i = 0; i < map.rooms.get(this.player.room).NPCCount(); i++)
            {
                NPCMove(map.rooms.get(this.player.room).GetNPC(i), movePerFrame);
                canvas.drawBitmap(map.avatars.get(map.rooms.get(this.player.room).GetNPC(i).type), (float) (map.rooms.get(this.player.room).GetNPC(i).x + displayGroundX), (float)(map.rooms.get(this.player.room).GetNPC(i).y + displayGroundY), null);
            }

            for(int i = 0; i < this.player.maxLives; i++)
            {
                float x = canvas.getWidth() - 32 - 32 * i;
                float y = canvas.getHeight() - 32;
                if(i < this.player.lives)
                {
                    canvas.drawBitmap(map.hearts.get(1), x, y, null);
                }
                else
                {
                    canvas.drawBitmap(map.hearts.get(0), x, y, null);
                }
            }

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(20);
            canvas.drawText("Score: " + Integer.toString(this.player.score), (float) 32, canvas.getHeight() - 32, paint);

            if (touchStartX > -1) {

                canvas.drawBitmap(this.joystickOut, (float) (startX - 96), (float) (startY - 96), null);
                canvas.drawBitmap(this.joystickIn, (float) (actualX - 64), (float) (actualY - 64), null);
            }
            //Log.d("TAMZ",  Double.toString(displayGroundX) + "  " + Double.toString(displayGroundY) + "  " + Double.toString(displayPlayerX) + "  " + Double.toString(displayPlayerY));

            canvas.drawBitmap(this.pauseButton, 0, 0, null);

            fps = (fps + 1000/ frameTime) / 2;

            paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(10);
            canvas.drawText("FPS: " + Long.toString(fps), (float) (canvas.getWidth() * 0.9), 10, paint);

            if(this.player.dead)
            {
                this.player.lives--;
                this.RespawnPlayer();

                if(this.player.lives < 0)
                {
                    paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTextSize(150);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("GameOver", canvas.getWidth() / 2, canvas.getHeight() / 2, paint);

                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawText("GameOver", canvas.getWidth() / 2, canvas.getHeight() / 2, paint);
                    this.Vibrate(1);

                    synchronized (this.status)
                    {
                        this.status = STATUS_END;
                    }

                    this.active = false;
                }
                else
                {
                    this.Vibrate(0);
                    this.player.dead = false;
                }
            }

            if(this.map.finishRoom == this.player.room && this.player.dead == false)
            {
                if(this.map.finishX == (int) (this.player.x / 64) && this.map.finishY == (int)(this.player.y ) / 64)
                {
                    this.WinGame(canvas);
                }
                else if((this.player.x - (int)(this.player.x)) > 0 && this.map.finishX == (int) (this.player.x + 1) / 64 && this.map.finishY == (int)(this.player.y / 64))
                {
                    this.WinGame(canvas);
                }
                else if((this.player.y - (int)(this.player.y)) > 0 && this.map.finishX == (int) (this.player.x) / 64 && this.map.finishY == (int)(this.player.y + this.map.rooms.get(this.player.room).GetSize()[0]) / 64)
                {
                    this.WinGame(canvas);
                }
                else if((this.player.x - (int)(this.player.x)) > 0 && (this.player.y - (int)(this.player.y)) > 0 && this.map.finishX == (int) (this.player.x + 1) / 64 && this.map.finishY == (int)(this.player.y + this.map.rooms.get(this.player.room).GetSize()[0]) / 64)
                {
                    this.WinGame(canvas);
                }
            }

            this.gameCanvas.SetBitmap(bitmap);
            this.gameCanvas.invalidate();

            if(this.map.rooms.get(this.player.room).GetJump(arrayPosition) != null)
            {
                if(this.player.jump == false)
                    this.Jump(this.map.rooms.get(this.player.room).GetJump(arrayPosition));
            }
            else if((this.player.x - (int)(this.player.x)) > 0 && this.map.rooms.get(this.player.room).GetJump(arrayPosition + 1 ) != null)
            {
                if(this.player.jump == false)
                    this.Jump(this.map.rooms.get(this.player.room).GetJump(arrayPosition + 1));
            }
            else if((this.player.y - (int)(this.player.y)) > 0 && this.map.rooms.get(this.player.room).GetJump(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0] ) != null)
            {
                if(this.player.jump == false)
                    this.Jump(this.map.rooms.get(this.player.room).GetJump(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]));
            }
            else if((this.player.x - (int)(this.player.x)) > 0 && (this.player.y - (int)(this.player.y)) > 0 && this.map.rooms.get(this.player.room).GetJump(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0] + 1) != null)
            {
                if(this.player.jump == false)
                    this.Jump(this.map.rooms.get(this.player.room).GetJump(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0] + 1));
            }
            else
            {
                this.player.jump = false;
            }

            /*if((this.player.x - oldPlayerX) < 0 && (this.map.rooms.get(this.player.room).GetJump(arrayPosition) != null))
            {
                if(this.player.jump == false)
                    this.Jump(this.map.rooms.get(this.player.room).GetJump(arrayPosition));
            }
            else if((this.player.x - oldPlayerX) > 0 && this.map.rooms.get(this.player.room).GetJump(arrayPosition + 1 ) != null)
            {
                if(this.player.jump == false)
                    this.Jump(this.map.rooms.get(this.player.room).GetJump(arrayPosition + 1));
            }
            else if((this.player.y - oldPlayerY) < 0 && this.map.rooms.get(this.player.room).GetJump(arrayPosition) != null)
            {
                if(this.player.jump == false)
                    this.Jump(this.map.rooms.get(this.player.room).GetJump(arrayPosition));
            }
            else if((this.player.y - oldPlayerY) > 0 && this.map.rooms.get(this.player.room).GetJump(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0] ) != null)
            {
                if(this.player.jump == false)
                   this.Jump(this.map.rooms.get(this.player.room).GetJump(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]));
            }
            else
            {
                this.player.jump = false;
            }*/
            frameTime = System.currentTimeMillis() - startTime;

            if(frameTime < this.frameRate && frameTime > 0)
            {
                try {
                    sleep(this.frameRate - frameTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            frameTime = System.currentTimeMillis() - startTime;

            if(this.pause == true)
            {
                int tmpStatus;
                synchronized (this.status)
                {
                    tmpStatus = this.status;
                    this.status = STATUS_PAUSE;
                }

                try
                {
                    synchronized (this)
                    {
                        paint = new Paint();
                        paint.setColor(0xFFFF9900);
                        paint.setStyle(Paint.Style.FILL);
                        paint.setTextSize(150);
                        paint.setTextAlign(Paint.Align.CENTER);
                        canvas.drawText("Pause", canvas.getWidth() / 2, canvas.getHeight() / 2, paint);

                        paint.setColor(Color.BLACK);
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawText("Pause", canvas.getWidth() / 2, canvas.getHeight() / 2, paint);

                        this.gameCanvas.SetBitmap(bitmap);
                        this.gameCanvas.invalidate();

                        this.wait();
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                synchronized (this.status)
                {
                    this.status = tmpStatus;
                    this.pause = false;
                }
            }

            //Log.d("TAMZ", "FPS: " + Long.toString(1000 / frameTime));
        }

        synchronized (this.status)
        {
            if (this.status == STATUS_END)
            {
                if (this.player.dead == false)
                {
                    this.result = new Result();
                    this.result.time = System.currentTimeMillis() - this.player.startTime;
                    this.result.score = this.player.score;
                    this.result.name = this.map.name;
                }

                try
                {
                    sleep(5000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                this.onEnd.OnEnd();
            }
        }
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            double x = event.getX();
            double y = event.getY();
            double xRatio = (16 * 64) / (float) gameCanvas.getMeasuredWidth();
            double yRatio = (9 * 64) / (float) gameCanvas.getMeasuredHeight();
            double realX = x * xRatio;
            double realY = y * yRatio;

            //Log.d("TAMZ", "X:" + Double.toString( gameCanvas.getMeasuredWidth()) + " Y:" + Double.toString( gameCanvas.getMeasuredHeight()));

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    if(!(realX < 160 && realY < 160))
                    {
                        synchronized (touchStartX) {
                            touchStartX = realX;
                            touchStartY = realY;
                        }
                        synchronized (touchActualX) {
                            touchActualX = realX;
                            touchActualY = realY;
                        }
                    }
                    else if(realX < 64 && realY < 64)
                    {
                        double distance = (realX - 31.5) * (realX - 31.5) + (realY - 31.5) * (realY - 31.5);
                        if(distance < (32 * 32))
                        {
                            PauseOrResume();
                        }
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if(touchStartX >= 0)
                    {
                        synchronized (touchActualX)
                        {
                            touchActualX = realX;
                            touchActualY = realY;
                        }
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

            return true;
        }
    };

    public void End()
    {
        synchronized (this.status)
        {
            this.status = STATUS_STOP;
        }
        this.active = false;
    }

    public void PauseOrResume()
    {
        synchronized (this.status)
        {
            if (this.status == STATUS_RUN)
            {
                this.pause = true;
            }
            else if (this.status == STATUS_PAUSE)
            {
                synchronized (this)
                {
                    this.notify();
                }
            }
        }
    }

    private void NPCMove(NPC npc, double movePerFrame)
    {
        if(npc.type == 1)
        {
            double oldX = npc.x;
            double oldY = npc.y;


            //Log.d("TAMZ", "cos " + Double.toString(Math.cos(Math.toRadians(npc.direction))) + " sin " + Double.toString(Math.sin(Math.toRadians(npc.direction))));

            double differenceX = npc.x - this.player.x;
            double differenceY = npc.y - this.player.y;
            double distance = Math.sqrt(differenceX * differenceX + differenceY * differenceY);

            if(distance < 64)
            {
                this.player.dead = true;
            }
            else if(distance <= 320)
            {
                if (differenceY >= 0) {
                    npc.direction = 180 - (int) Math.toDegrees(Math.acos(differenceX / distance));
                } else if (differenceY < 0) {
                    npc.direction = 180 + (int) Math.toDegrees(Math.acos(differenceX / distance));
                }
            }

            npc.x += movePerFrame * Math.cos(Math.toRadians(npc.direction)) * npcRatio;
            int arrayPosition = ((int)(npc.x / 64)) + ((int)(npc.y / 64)) * this.map.rooms.get(this.player.room).GetSize()[0];

            if(npc.x < 0)
            {
                npc.x = 0;

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if (npc.x > ((this.map.rooms.get(this.player.room).GetSize()[0] - 1) * 64))
            {
                npc.x = (this.map.rooms.get(this.player.room).GetSize()[0] - 1) * 64;

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if((npc.x - oldX) < 0 && (this.map.rooms.get(this.player.room).GetCollision(arrayPosition) > 0 || ((npc.y - (int) (npc.y)) > 0 && this.map.rooms.get(this.player.room).GetCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]) > 0)))
            {
                npc.x = Math.floor(oldX);

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if((npc.x - oldX) > 0 && (this.map.rooms.get(this.player.room).GetCollision(arrayPosition + 1 ) > 0 || ((npc.y - (int) (npc.y)) > 0 && this.map.rooms.get(this.player.room).GetCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0] + 1) > 0)))
            {
                npc.x = Math.ceil(oldX);

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if((npc.x - oldX) < 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition) != null)
            {
                if(this.NPCItemCollision(arrayPosition))
                {
                    npc.x = Math.floor(oldX);
                }

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if((npc.x - oldX) < 0 && (npc.y - (int) (npc.y)) > 0 && this.map.rooms.get(player.room).GetItem(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]) != null)
            {
                if(this.NPCItemCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]))
                {
                    npc.x = Math.ceil(oldX);
                }

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if((npc.x - oldX) > 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition + 1 ) != null)
            {
                if(this.NPCItemCollision(arrayPosition + 1))
                {
                    npc.x = Math.ceil(oldX);
                }

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if((npc.x - oldX) > 0 && (npc.y - (int) (npc.y)) > 0 && this.map.rooms.get(player.room).GetItem(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]) != null)
            {
                if(this.NPCItemCollision(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]))
                {
                    npc.x = Math.ceil(oldX);
                }

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }

            npc.y += movePerFrame * ( - Math.sin(Math.toRadians(npc.direction))) * npcRatio;
            arrayPosition = ((int)(npc.x / 64)) + ((int)(npc.y / 64)) * this.map.rooms.get(this.player.room).GetSize()[0];

            if(npc.y < 0)
            {
                npc.y = 0;

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if (npc.y > ((this.map.rooms.get(this.player.room).GetSize()[1] - 1) * 64))
            {
                npc.y = (this.map.rooms.get(this.player.room).GetSize()[1] - 1) * 64;

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if((npc.y - oldY) < 0 && (this.map.rooms.get(this.player.room).GetCollision(arrayPosition) > 0 || ((npc.x - (int) (npc.x)) > 0 && this.map.rooms.get(this.player.room).GetCollision(arrayPosition + 1) > 0)))
            {
                npc.y = Math.floor(oldY);

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if((npc.y - oldY) > 0 && (this.map.rooms.get(this.player.room).GetCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0] ) > 0 || ((npc.x - (int)(npc.x)) > 0 && this.map.rooms.get(this.player.room).GetCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0] + 1) > 0)))
            {
                npc.y = Math.ceil(oldY);

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if((npc.y - oldY) < 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition) != null)
            {
                if(this.NPCItemCollision(arrayPosition))
                {
                    npc.y = Math.floor(oldY);
                }

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if((npc.y - oldY) < 0 && (npc.x - (int)(npc.x)) > 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition + 1) != null)
            {
                if(this.NPCItemCollision(arrayPosition + 1))
                {
                    npc.y = Math.floor(oldY);
                }

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if((npc.y - oldY) > 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0] ) != null)
            {
                if(this.NPCItemCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]))
                {
                    npc.y = Math.ceil(oldY);
                }

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }
            else if((npc.y - oldY) > 0 && (npc.x - (int)(npc.x)) > 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]) != null)
            {
                if(this.NPCItemCollision(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]))
                {
                    npc.y = Math.ceil(oldY);
                }

                if(distance > 320)
                {
                    npc.direction = (int) (360 * Math.random());
                }
            }

            //Log.d("TAMZ", "Direction" + Integer.toString(npc.direction));
        }
        else if(npc.type == 2)
        {
            double oldX = npc.x;
            double oldY = npc.y;


            //Log.d("TAMZ", "cos " + Double.toString(Math.cos(Math.toRadians(npc.direction))) + " sin " + Double.toString(Math.sin(Math.toRadians(npc.direction))));

            double differenceX = npc.x - this.player.x;
            double differenceY = npc.y - this.player.y;
            double distance = Math.sqrt(differenceX * differenceX + differenceY * differenceY);

            if (distance < 64)
            {
                this.player.dead = true;
            }

            npc.x += movePerFrame * Math.cos(Math.toRadians(npc.direction)) * npcRatio;
            int arrayPosition = ((int) (npc.x / 64)) + ((int) (npc.y / 64)) * this.map.rooms.get(this.player.room).GetSize()[0];

            if (npc.x < 0)
            {
                npc.x = 0;
                npc.direction += 180;
            }
            else if (npc.x > ((this.map.rooms.get(this.player.room).GetSize()[0] - 1) * 64))
            {
                npc.x = (this.map.rooms.get(this.player.room).GetSize()[0] - 1) * 64;
                npc.direction += 180;
            }
            else if ((npc.x - oldX) < 0 && (this.map.rooms.get(this.player.room).GetCollision(arrayPosition) > 0 || ((npc.y - (int) (npc.y)) > 0 && this.map.rooms.get(this.player.room).GetCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]) > 0)))
            {
                npc.x = Math.floor(oldX);
                npc.direction += 180;
            }
            else if ((npc.x - oldX) > 0 && (this.map.rooms.get(this.player.room).GetCollision(arrayPosition + 1) > 0 || ((npc.y - (int) (npc.y)) > 0 && this.map.rooms.get(this.player.room).GetCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0] + 1) > 0)))
            {
                npc.x = Math.ceil(oldX);
                npc.direction += 180;
            }
            else if ((npc.x - oldX) < 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition) != null)
            {
                if (this.NPCItemCollision(arrayPosition))
                {
                    npc.x = Math.floor(oldX);
                    npc.direction += 180;
                }
            }
            else if ((npc.x - oldX) < 0 && (npc.y - (int) (npc.y)) > 0 && this.map.rooms.get(player.room).GetItem(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]) != null)
            {
                if (this.NPCItemCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]))
                {
                    npc.x = Math.ceil(oldX);
                    npc.direction += 180;
                }

            }
            else if ((npc.x - oldX) > 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition + 1) != null)
            {
                if (this.NPCItemCollision(arrayPosition + 1))
                {
                    npc.x = Math.ceil(oldX);
                    npc.direction += 180;
                }
            }
            else if ((npc.x - oldX) > 0 && (npc.y - (int) (npc.y)) > 0 && this.map.rooms.get(player.room).GetItem(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]) != null)
            {
                if (this.NPCItemCollision(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]))
                {
                    npc.x = Math.ceil(oldX);
                    npc.direction += 180;
                }
            }

            npc.y += movePerFrame * (-Math.sin(Math.toRadians(npc.direction))) * npcRatio;
            arrayPosition = ((int) (npc.x / 64)) + ((int) (npc.y / 64)) * this.map.rooms.get(this.player.room).GetSize()[0];

            if (npc.y < 0)
            {
                npc.y = 0;
                npc.direction += 180;
            }
            else if (npc.y > ((this.map.rooms.get(this.player.room).GetSize()[1] - 1) * 64))
            {
                npc.y = (this.map.rooms.get(this.player.room).GetSize()[1] - 1) * 64;
                npc.direction += 180;
            }
            else if ((npc.y - oldY) < 0 && (this.map.rooms.get(this.player.room).GetCollision(arrayPosition) > 0 || ((npc.x - (int) (npc.x)) > 0 && this.map.rooms.get(this.player.room).GetCollision(arrayPosition + 1) > 0)))
            {
                npc.y = Math.floor(oldY);
                npc.direction += 180;
            }
            else if ((npc.y - oldY) > 0 && (this.map.rooms.get(this.player.room).GetCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]) > 0 || ((npc.x - (int) (npc.x)) > 0 && this.map.rooms.get(this.player.room).GetCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0] + 1) > 0)))
            {
                npc.y = Math.ceil(oldY);
                npc.direction += 180;
            }
            else if ((npc.y - oldY) < 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition) != null)
            {
                if (this.NPCItemCollision(arrayPosition))
                {
                    npc.y = Math.floor(oldY);
                    npc.direction += 180;
                }
            }
            else if ((npc.y - oldY) < 0 && (npc.x - (int) (npc.x)) > 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition + 1) != null)
            {
                if (this.NPCItemCollision(arrayPosition + 1))
                {
                    npc.y = Math.floor(oldY);
                    npc.direction += 180;
                }
            }
            else if ((npc.y - oldY) > 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]) != null)
            {
                if (this.NPCItemCollision(arrayPosition + this.map.rooms.get(this.player.room).GetSize()[0]))
                {
                    npc.y = Math.ceil(oldY);
                    npc.direction += 180;
                }
            }
            else if ((npc.y - oldY) > 0 && (npc.x - (int) (npc.x)) > 0 && this.map.rooms.get(this.player.room).GetItem(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]) != null)
            {
                if (this.NPCItemCollision(arrayPosition + 1 + this.map.rooms.get(this.player.room).GetSize()[0]))
                {
                    npc.y = Math.ceil(oldY);
                    npc.direction += 180;
                }
            }
        }
    }

    private void RespawnPlayer()
    {
        this.player.x = this.map.spawnX * 64;
        this.player.y = this.map.spawnY * 64;
        this.player.room = this.map.spawnRoom;
    }

    private void Jump(Room.Jump jump)
    {
        this.player.x = jump.x * 64;
        this.player.y = jump.y * 64;
        this.player.room = jump.room;
        this.player.jump = true;
    }

    private boolean ItemCollision(int itemId)
    {
        Integer[] item = this.map.rooms.get(this.player.room).GetItem(itemId);

        if((item[1] & Room.ITEM_CASH) != 0)
        {
            this.player.score += item[2];
            this.map.rooms.get(this.player.room).DeleteItem(itemId);
            this.Vibrate(3);
        }
        else if((item[1] & Room.ITEM_KEY) != 0)
        {
            this.player.keys.add(item[2]);
            this.map.rooms.get(this.player.room).DeleteItem(itemId);
            this.Vibrate(3);
        }
        else if((item[1] & Room.ITEM_OPENED_BY_KEY) != 0)
        {
            for(int i = 0; i < this.player.keys.size(); i++)
            {
                if(this.player.keys.get(i) == item[2])
                {
                    Deque<Integer> queue = new ArrayDeque<Integer>();
                    queue.addFirst(itemId);

                    while(queue.peekLast() != null)
                    {
                        Integer tmp = queue.pollLast();
                        if(this.map.rooms.get(this.player.room).GetItem(tmp - 1) != null)
                        {
                            Integer[] item2 = this.map.rooms.get(this.player.room).GetItem(tmp - 1);
                            if(item2[0] == item[0])
                            {
                                queue.addFirst(tmp - 1);
                            }
                        }
                        if(this.map.rooms.get(this.player.room).GetItem(tmp + 1) != null)
                        {
                            Integer[] item2 = this.map.rooms.get(this.player.room).GetItem(tmp + 1);
                            if(item2[0] == item[0])
                            {
                                queue.addFirst(tmp + 1);
                            }
                        }
                        if(this.map.rooms.get(this.player.room).GetItem(tmp - this.map.rooms.get(this.player.room).GetSize()[0]) != null)
                        {
                            Integer[] item2 = this.map.rooms.get(this.player.room).GetItem(tmp - this.map.rooms.get(this.player.room).GetSize()[0]);
                            if(item2[0] == item[0])
                            {
                                queue.addFirst(tmp - this.map.rooms.get(this.player.room).GetSize()[0]);
                            }
                        }
                        if(this.map.rooms.get(this.player.room).GetItem(tmp + this.map.rooms.get(this.player.room).GetSize()[0]) != null)
                        {
                            Integer[] item2 = this.map.rooms.get(this.player.room).GetItem(tmp + this.map.rooms.get(this.player.room).GetSize()[0]);
                            if(item2[0] == item[0])
                            {
                                queue.addFirst(tmp + this.map.rooms.get(this.player.room).GetSize()[0]);
                            }
                        }
                        this.map.rooms.get(this.player.room).DeleteItem(tmp);
                    }
                    this.player.keys.remove(i);
                    return false;
                }
            }
        }
        if((item[1] & Room.ITEM_COLLISION) != 0)
        {
            return true;
        }

        return false;
    }

    private boolean NPCItemCollision(int itemId)
    {
        Integer[] item = this.map.rooms.get(this.player.room).GetItem(itemId);
        if((item[1] & Room.ITEM_COLLISION) != 0)
        {
            return true;
        }

        return false;
    }

    private void WinGame(Canvas canvas)
    {
        synchronized (this.status)
        {
            this.status = STATUS_END;
        }

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(150);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Winner", canvas.getWidth() / 2, canvas.getHeight() / 2, paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawText("Winner", canvas.getWidth() / 2, canvas.getHeight() / 2, paint);

        this.Vibrate(2);

        this.active = false;
    }

    private void Vibrate(int vibrationType)
    {
        if(this.vibrator != null)
        {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                this.vibrator.vibrate(VibrationEffect.createWaveform( this.vibrationTimings[vibrationType], this.vibrationAmplitude[vibrationType], -1));
            }
            else
            {
                this.vibrator.vibrate(this.vibrationTimings[vibrationType], -1);
            }
        }
    }

    public Result GetResult()
    {
        return this.result;
    }

    public static class Result
    {
        int score;
        long time;
        String name;
    }

    public int GetStatus()
    {
        synchronized (this.status)
        {
            return this.status;
        }
    }
}

