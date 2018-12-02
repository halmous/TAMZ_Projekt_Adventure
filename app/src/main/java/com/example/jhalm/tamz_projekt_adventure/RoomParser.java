package com.example.jhalm.tamz_projekt_adventure;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class RoomParser extends Thread {

    private XMLNode rootXMLNode;
    private EndHandler endHandler;
    private Room room;
    private List<Bitmap> bitmaps;

    public RoomParser(XMLNode xmlNode, EndHandler endHandler, List<Bitmap> bitmaps)
    {
        this.rootXMLNode = xmlNode;
        this.endHandler = endHandler;
        this.room = new Room();
        this.bitmaps = bitmaps;
    }

    @Override
    public void run()
    {
        if(this.rootXMLNode.GetName().equals("room"))
        {
            this.room.SetSize(Integer.parseInt(this.rootXMLNode.GetAttributeValue("x")), Integer.parseInt(this.rootXMLNode.GetAttributeValue("y")));

            for(int i = 0; i < this.rootXMLNode.ChildCount(); i++)
            {
                if(this.rootXMLNode.GetChild(i).GetName().equals("array"))
                {
                    ParseGround(this.rootXMLNode.GetChild(i));
                }
                else if(this.rootXMLNode.GetChild(i).GetName().equals("npcs"))
                {
                    ParseNPCS(this.rootXMLNode.GetChild(i));
                }
                else if(this.rootXMLNode.GetChild(i).GetName().equals("jumps"))
                {
                    ParseJumps(this.rootXMLNode.GetChild(i));
                }
            }
        }

        this.endHandler.OnEnd();
    }

    private void ParseJumps(XMLNode xmlNode)
    {
        for(int i = 0; i < xmlNode.ChildCount(); i++)
        {
            if(xmlNode.GetChild(i).GetName().equals("jump"))
            {
                int x = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("x"));
                int y = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("y"));
                int sizeX = 1;
                int sizeY = 1;
                int jumpTo = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("jumpto"));
                int jumpToX = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("jumptox"));
                int jumpToY = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("jumptoy"));

                if(xmlNode.GetChild(i).GetAttributeValue("sizex") != null)
                    sizeX = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("sizex"));
                if(xmlNode.GetChild(i).GetAttributeValue("sizey") != null)
                    sizeY = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("sizey"));

                for(int j = 0; j < sizeY; j++)
                {
                    for(int k = 0 ; k < sizeX; k++)
                    {
                        this.room.AddJump(x + k + ((y + j) * this.room.GetSize()[0]), new Room.Jump(jumpTo, jumpToX + k, jumpToY + j));
                    }
                }
            }
        }
    }

    private void ParseNPCS(XMLNode xmlNode)
    {
        for(int i = 0; i < xmlNode.ChildCount(); i++)
        {
            if(xmlNode.GetChild(i).GetName().equals("npc"))
            {
                NPC npc = new NPC();

                npc.type = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("type"));
                npc.x = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("x"));
                npc.y = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("y"));

                if(xmlNode.GetChild(i).GetAttributeValue("direction") != null)
                    npc.direction = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("direction"));
                else
                    npc.direction = 0;

                this.room.AddNPC(npc);
            }
        }
    }

    private void ParseGround(XMLNode xmlNode)
    {
        List<Integer> tiles = new ArrayList<Integer>();

        for(int i = 0; i < xmlNode.ChildCount(); i++)
        {
            if(xmlNode.GetChild(i).GetName().equals("value"))
            {
                int size = 1;
                int collision = 0;
                int item = -1;
                int itemOptions = 0;
                int itemValue = 0;

                if(xmlNode.GetChild(i).GetAttributeValue("size") != null)
                    size = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("size"));

                if(xmlNode.GetChild(i).GetAttributeValue("collision") != null)
                {
                    if(xmlNode.GetChild(i).GetAttributeValue("collision").equals("true"))
                        collision = 1;
                }

                if(xmlNode.GetChild(i).GetAttributeValue("item") != null)
                {
                    item = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("item"));
                    itemOptions = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("itemoptions"));

                    if(xmlNode.GetChild(i).GetAttributeValue("itemvalue") != null)
                        itemValue = Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("itemvalue"));
                }

                for(int j = 0; j < size; j++)
                {
                    tiles.add(Integer.parseInt(xmlNode.GetChild(i).GetAttributeValue("tile")));
                    this.room.AddCollision(collision);
                    if(item >= 0)
                        this.room.AddItem(tiles.size() - 1, item, itemOptions, itemValue);
                }

            }
        }
        CreateGroundBitmap(tiles);
    }

    private void CreateGroundBitmap(List<Integer> tiles)
    {
        this.room.SetGroundBitmap(Bitmap.createBitmap(this.room.GetSize()[0] * 64, this.room.GetSize()[1] * 64, Bitmap.Config.ARGB_8888));

        for(int i = 0; i < tiles.size(); i++)
        {
            int tile = tiles.get(i);
            int[] pixels = new int[64 * 64];
            this.bitmaps.get(tile).getPixels(pixels, 0, 64,0,0,64,64);

            this.room.GetGroundBitmap().setPixels(pixels, 0, 64, (i % this.room.GetSize()[0]) * 64, (i /this.room.GetSize()[0]) * 64, 64, 64 );
        }
    }

    public Room GetResult()
    {
        return this.room;
    }
}
