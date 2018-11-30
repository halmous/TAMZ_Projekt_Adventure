package com.example.jhalm.tamz_projekt_adventure;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class Map
{
    public List<Bitmap> items;
    public List<Room> rooms;
    public List<Bitmap> avatars;
    public int spawnRoom;
    public int spawnX;
    public int spawnY;

    public Map()
    {
        this.rooms = new ArrayList<Room>();
    }
}
