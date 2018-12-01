package com.example.jhalm.tamz_projekt_adventure;

import android.graphics.Bitmap;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Room {

    public static final int ITEM_COLLISION = 1;
    public static final int ITEM_OPENED_BY_KEY = 2;
    public static final int ITEM_KEY = 4;
    public static final int ITEM_CASH = 8;


    private List<Integer> collision;
    private Bitmap ground;
    private Map<Integer, Integer[]> items;
    private int sizeX;
    private int sizeY;
    private List<NPC> npcs;

    public Room()
    {
        this.collision = new ArrayList<Integer>();
        this.items = new ArrayMap<Integer, Integer[]>();
        this.npcs = new ArrayList<NPC>();
    }

    public void AddCollision(int colision)
    {
        this.collision.add(colision);
    }

    public int GetCollision(int i)
    {
        return this.collision.get(i);
    }

    public int SizeCollision()
    {
        return this.collision.size();
    }

    public void SetGroundBitmap(Bitmap bitmap)
    {
        this.ground = bitmap;
    }

    public Bitmap GetGroundBitmap()
    {
        return this.ground;
    }

    public void AddItem( int key, int item, int itemDesc)
    {
        this.items.put(key, new Integer[] {item, itemDesc});
    }

    public int ItemSize()
    {
        return this.items.size();
    }

    public Integer[] GetItem(int i)
    {
        return this.items.get(i);
    }

    public void SetSize(int x, int y)
    {
        this.sizeX = x;
        this.sizeY = y;
    }

    public int[] GetSize()
    {
        return new int[] {this.sizeX, this.sizeY};
    }

    public NPC GetNPC(int i)
    {
        return this.npcs.get(i);
    }

    public void AddNPC(NPC npc)
    {
        this.npcs.add(npc);
    }

    public int NPCCount()
    {
        return this.npcs.size();
    }
}
