package com.example.jhalm.tamz_projekt_adventure;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
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

    private double playerX;
    private double playerY;
    private int playerRoom;

    private final double movePerMs = 0.2;
    private final double npcRatio = 0.9;

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

        this.playerX = this.map.spawnX * 64;
        this.playerY = this.map.spawnY * 64;
        this.playerRoom = this.map.spawnRoom;

        for(int i = 0; i < this.map.rooms.size(); i++)
        {
            for(int j = 0; j < this.map.rooms.get(i).NPCCount(); j++)
            {
                this.map.rooms.get(i).GetNPC(j).x *= 64;
                this.map.rooms.get(i).GetNPC(j).y *= 64;
            }
        }

        this.gameCanvas.setOnTouchListener(this.touchListener);
    }

    @Override
    public void run() {
        active = true;


        long frameTime = 16;
        long fps = 60;
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

            double oldPlayerX = this.playerX;
            this.playerX += ((actualX - startX) / 32) * movePerFrame;
            if(this.playerX < 0)
            {
                this.playerX = 0;
            }
            else if (this.playerX > ((this.map.rooms.get(this.playerRoom).GetSize()[0] - 1) * 64))
            {
                this.playerX = (this.map.rooms.get(this.playerRoom).GetSize()[0] - 1) * 64;
            }
            else if((this.playerX - oldPlayerX) < 0 && this.map.rooms.get(playerRoom).GetCollision(((int)(this.playerX / 64)) + ((int)(this.playerY / 64)) * this.map.rooms.get(playerRoom).GetSize()[0] ) > 0)
            {
                this.playerX = Math.floor(oldPlayerX);
            }
            else if((this.playerX - oldPlayerX) > 0 && this.map.rooms.get(playerRoom).GetCollision(((int)(this.playerX / 64)) + ((int)(this.playerY / 64)) * this.map.rooms.get(playerRoom).GetSize()[0]  + 1 ) > 0)
            {
                this.playerX = Math.ceil(oldPlayerX);
            }

            double oldPlayerY = this.playerY;
            this.playerY += ((actualY - startY) / 32) * movePerFrame;
            if(this.playerY < 0)
            {
                this.playerY = 0;
            }
            else if(this.playerY > ((this.map.rooms.get(this.playerRoom).GetSize()[1] - 1) * 64))
            {
                this.playerY = (this.map.rooms.get(this.playerRoom).GetSize()[1] - 1) * 64;
            }
            else if((this.playerY - oldPlayerY) < 0 && this.map.rooms.get(playerRoom).GetCollision(((int)(this.playerX / 64)) + ((int)(this.playerY / 64)) * this.map.rooms.get(playerRoom).GetSize()[0] ) > 0)
            {
                this.playerY = Math.floor(oldPlayerY);
            }
            else if((this.playerY - oldPlayerY) > 0 && this.map.rooms.get(playerRoom).GetCollision(((int)(this.playerX / 64)) + ((int)(this.playerY / 64)) * this.map.rooms.get(playerRoom).GetSize()[0] + this.map.rooms.get(playerRoom).GetSize()[0] ) > 0)
            {
                this.playerY = Math.ceil(oldPlayerY);
            }



            float displayGroundX;
            float displayGroundY;
            float displayPlayerX;
            float displayPlayerY;

            if((8 * 64) >= this.playerX)
            {
                displayGroundX = 0;
                displayPlayerX = (float) this.playerX;
            }
            else if((map.rooms.get(this.playerRoom).GetGroundBitmap().getWidth() - (8 * 64)) <= this.playerX)
            {
                displayGroundX = ((16 * 64) - map.rooms.get(this.playerRoom).GetGroundBitmap().getWidth());
                displayPlayerX = (float) ((16 * 64) - (map.rooms.get(this.playerRoom).GetGroundBitmap().getWidth() - this.playerX));
            }
            else
            {
                displayGroundX =(float) (8 * 64 - this.playerX);
                displayPlayerX = 8 * 64;
            }

            if((4.5 * 64) >= this.playerY)
            {
                displayGroundY = 0;
                displayPlayerY = (float) this.playerY;
            }
            else if((map.rooms.get(this.playerRoom).GetGroundBitmap().getHeight() - (4.5 * 64)) <= this.playerY)
            {
                displayGroundY = (float) ((9 * 64) - map.rooms.get(this.playerRoom).GetGroundBitmap().getHeight());
                displayPlayerY = (float) ((9 * 64) - (map.rooms.get(this.playerRoom).GetGroundBitmap().getHeight() - this.playerY));
            }
            else
            {
                displayGroundY =(float) (4.5 * 64 - this.playerY);
                displayPlayerY = (float) (4.5 * 64);
            }

            canvas.drawBitmap(map.rooms.get(this.playerRoom).GetGroundBitmap(), displayGroundX, displayGroundY, null);
            canvas.drawBitmap(map.avatars.get(0), displayPlayerX, (float) displayPlayerY, null);

            for(int i = 0; i < map.rooms.get(this.playerRoom).NPCCount(); i++)
            {
                NPCMove(map.rooms.get(this.playerRoom).GetNPC(i), movePerFrame);
                canvas.drawBitmap(map.avatars.get(map.rooms.get(this.playerRoom).GetNPC(i).type), (float) (map.rooms.get(this.playerRoom).GetNPC(i).x + displayGroundX), (float)(map.rooms.get(this.playerRoom).GetNPC(i).y + displayGroundY), null);
            }

            if (touchStartX > -1) {

                canvas.drawBitmap(this.joystickOut, (float) (startX - 96), (float) (startY - 96), null);
                canvas.drawBitmap(this.joystickIn, (float) (actualX - 64), (float) (actualY - 64), null);
            }
            //Log.d("TAMZ",  Double.toString(displayGroundX) + "  " + Double.toString(displayGroundY) + "  " + Double.toString(displayPlayerX) + "  " + Double.toString(displayPlayerY));

            canvas.drawBitmap(this.pauseButton, 0, 0, null);

            fps = (fps + 1000/ frameTime) / 2;

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(10);
            canvas.drawText("FPS: " + Long.toString(fps), (float) (canvas.getWidth() * 0.9), 10, paint);

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

    public void End()
    {
        this.active = false;
    }

    private void NPCMove(NPC npc, double movePerFrame)
    {
        if(npc.type == 1)
        {
            double oldX = npc.x;
            double oldY = npc.y;


            //Log.d("TAMZ", "cos " + Double.toString(Math.cos(Math.toRadians(npc.direction))) + " sin " + Double.toString(Math.sin(Math.toRadians(npc.direction))));

            double differenceX = npc.x - this.playerX;
            double differenceY = npc.y - this.playerY;
            double distance = Math.sqrt(differenceX * differenceX + differenceY * differenceY);

            if(differenceY >= 0)
            {
                npc.direction = 180 - (int) Math.toDegrees(Math.acos(differenceX / distance));
            }
            else if(differenceY < 0)
            {
                npc.direction = 180 + (int) Math.toDegrees(Math.acos(differenceX / distance));
            }

            npc.x += movePerFrame * Math.cos(Math.toRadians(npc.direction)) * npcRatio;
            npc.y += movePerFrame * ( - Math.sin(Math.toRadians(npc.direction))) * npcRatio;

            if(npc.x < 0)
            {
                npc.x = 0;
            }
            else if (npc.x > ((this.map.rooms.get(this.playerRoom).GetSize()[0] - 1) * 64))
            {
                npc.x = (this.map.rooms.get(this.playerRoom).GetSize()[0] - 1) * 64;
            }
            else if((npc.x - oldX) < 0 && this.map.rooms.get(playerRoom).GetCollision(((int)(npc.x / 64)) + ((int)(npc.y / 64)) * this.map.rooms.get(playerRoom).GetSize()[0] ) > 0)
            {
                npc.x = Math.floor(oldX);
            }
            else if((npc.x - oldX) > 0 && this.map.rooms.get(playerRoom).GetCollision(((int)(npc.x / 64)) + ((int)(npc.y / 64)) * this.map.rooms.get(playerRoom).GetSize()[0]  + 1 ) > 0)
            {
                npc.x = Math.ceil(oldX);
            }

            if(npc.y < 0)
            {
                npc.y = 0;
            }
            else if (npc.y > ((this.map.rooms.get(this.playerRoom).GetSize()[1] - 1) * 64))
            {
                npc.y = (this.map.rooms.get(this.playerRoom).GetSize()[1] - 1) * 64;
            }
            else if((npc.y - oldY) < 0 && this.map.rooms.get(playerRoom).GetCollision(((int)(npc.x / 64)) + ((int)(npc.y / 64)) * this.map.rooms.get(playerRoom).GetSize()[0] ) > 0)
            {
                npc.y = Math.floor(oldY);
            }
            else if((npc.y - oldY) > 0 && this.map.rooms.get(playerRoom).GetCollision(((int)(npc.x / 64)) + ((int)(npc.y / 64)) * this.map.rooms.get(playerRoom).GetSize()[0]  + this.map.rooms.get(playerRoom).GetSize()[0] ) > 0)
            {
                npc.y = Math.ceil(oldY);
            }

            Log.d("TAMZ", "Direction" + Integer.toString(npc.direction));
        }
    }
}
