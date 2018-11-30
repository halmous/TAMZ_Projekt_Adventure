package com.example.jhalm.tamz_projekt_adventure;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class CanvasActivity extends Activity {

    private GameCanvas gameCanvas;
    private MapLoader mapLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);

        this.gameCanvas = findViewById(R.id.canvas);

        this.mapLoader = new MapLoader("test.xml", this.gameCanvas, this.endHandler, getSharedPreferences("settings", Context.MODE_PRIVATE), getBaseContext());
        this.mapLoader.start();
    }

    private EndHandler endHandler = new EndHandler() {
        @Override
        public void OnEnd() {
            GameLoop gameLoop = new GameLoop(gameCanvas, mapLoader.GetResult());
            gameLoop.start();
            mapLoader = null;
        }
    };
}
