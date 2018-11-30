package com.example.jhalm.tamz_projekt_adventure;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class TileParser extends Thread {

    private Bitmap image;
    private List<Bitmap> tiles;
    private int sizeX;
    private int sizeY;
    private EndHandler endHandler;

    public TileParser(Bitmap image, int sizeX, int sizeY, EndHandler endHandler)
    {
        this.image = image;
        this.tiles = new ArrayList<Bitmap>();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.endHandler = endHandler;
    }

    @Override
    public void run()
    {
        int xCount = image.getWidth() / this.sizeX;
        int yCount = image.getHeight() / this.sizeY;

        for(int y = 0; y < yCount; y++)
        {
            for(int x = 0; x < xCount; x++)
            {
                this.tiles.add(Bitmap.createBitmap(this.image, x * sizeX, y * sizeY, sizeX, sizeY));
            }
        }

        if(endHandler != null)
            endHandler.OnEnd();
    }

    public List<Bitmap> GetResult()
    {
        return this.tiles;
    }
}
