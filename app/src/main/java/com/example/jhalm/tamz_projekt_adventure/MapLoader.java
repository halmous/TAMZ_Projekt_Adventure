package com.example.jhalm.tamz_projekt_adventure;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MapLoader extends Thread {

    private String mapName;
    private GameCanvas gameCanvas;
    private EndHandler loadEndHandler;
    private Map map;
    private SharedPreferences sharedPreferences;
    private Integer threadCounter = 0;
    private Semaphore semaphore;
    private List<Bitmap> tiles;

    public MapLoader(String mapName, GameCanvas gameCanvas, EndHandler loadEndHandler, SharedPreferences sharedPreferences) {
        this.mapName = mapName;
        this.gameCanvas = gameCanvas;
        this.loadEndHandler = loadEndHandler;
        this.sharedPreferences = sharedPreferences;
        this.map = new Map();
        this.semaphore = new Semaphore(0);
    }

    @Override
    public void run() {
        try {
            String externalDirectory = this.sharedPreferences.getString("externalDirectory", "projectMaps");
            File xmlMap = new File(Environment.getExternalStorageDirectory(), externalDirectory + "/maps/" + this.mapName);
            XMLParser xmlParser = new XMLParser(new FileInputStream(xmlMap), onEnd);
            this.threadCounter = 1;
            xmlParser.run();
            this.WaitForThreadsEnd();

            XMLNode rootNode = xmlParser.GetResult();

            if (rootNode.GetName().equals("map")) {
                this.threadCounter = 2;
                TileParser tilesParser = null;
                TileParser itemsParser = null;
                List<RoomParser> roomParsers = new ArrayList<RoomParser>();

                for (int i = 0; i < rootNode.ChildCount(); i++) {
                    if (rootNode.GetChild(i).GetName().equals("tilesImage"))
                    {
                        File tilesImage = new File(Environment.getExternalStorageDirectory(), externalDirectory + "/tiles/" + rootNode.GetChild(i).GetAttributeValue("src"));
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        tilesParser = new TileParser(BitmapFactory.decodeFile(tilesImage.getAbsolutePath(), options), 64, 64, onEnd);
                    }
                    else if (rootNode.GetChild(i).GetName().equals("itemsImage")) {
                        File itemsImage = new File(Environment.getExternalStorageDirectory(), externalDirectory + "/items/" + rootNode.GetChild(i).GetAttributeValue("src"));
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        itemsParser = new TileParser(BitmapFactory.decodeFile(itemsImage.getAbsolutePath(), options), 64, 64, onEnd);
                    }
                }

                if(tilesParser != null)
                    tilesParser.start();
                if(itemsParser != null)
                    itemsParser.start();

                this.WaitForThreadsEnd();

                if(tilesParser != null)
                    this.tiles = tilesParser.GetResult();
                if(itemsParser != null)
                    this.map.items = itemsParser.GetResult();

                for (int i = 0; i < rootNode.ChildCount(); i++)
                {
                    if(rootNode.GetChild(i).GetName().equals("room"))
                    {
                        roomParsers.add(new RoomParser(rootNode.GetChild(i), this.onEnd, this.tiles));
                    }
                }

                this.threadCounter = roomParsers.size();

                for(int i = 0; i < roomParsers.size(); i++)
                    roomParsers.get(i).start();

                this.WaitForThreadsEnd();

                for(int i = 0; i < roomParsers.size(); i++)
                    this.map.rooms.add(roomParsers.get(i).GetResult());
            }
            else {

            }
        } catch (Exception e) {
            Log.d("TAMZ_Exception", e.toString());
        }

        this.loadEndHandler.OnEnd();
    }

    private void WaitForThreadsEnd() {
        try
        {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Map GetResult() {
        return this.map;
    }


    public EndHandler onEnd = new EndHandler() {
        @Override
        public void OnEnd() {
            synchronized (threadCounter)
            {
                threadCounter--;

                if (threadCounter == 0)
                {
                    semaphore.release();
                }
            }
        }
    };

}
