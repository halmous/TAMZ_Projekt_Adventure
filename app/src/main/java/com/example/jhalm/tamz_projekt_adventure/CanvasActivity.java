package com.example.jhalm.tamz_projekt_adventure;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class CanvasActivity extends Activity {

    private final long[] frameRates = {GameLoop.MAX_30_FPS, GameLoop.MAX_60_FPS, GameLoop.MAX_120_FPS};

    private GameCanvas gameCanvas;
    private MapLoader mapLoader;
    private GameLoop gameLoop;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);

        this.gameCanvas = findViewById(R.id.canvas);

        Intent intent = getIntent();

        this.sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        this.mapLoader = new MapLoader(intent.getStringExtra("mapName"), this.gameCanvas, this.endHandler, this.sharedPreferences, getBaseContext());
        this.mapLoader.start();
    }

    @Override
    protected void onDestroy()
    {
        this.gameLoop.End();
        super.onDestroy();
    }

    private EndHandler endHandler = new EndHandler() {
        @Override
        public void OnEnd() {


            gameLoop = new GameLoop(gameCanvas, mapLoader.GetResult(), frameRates[sharedPreferences.getInt("frameRates", 1)], gameEndHandler);
            gameLoop.start();
            mapLoader = null;
        }
    };

    private EndHandler gameEndHandler = new EndHandler()
    {
        @Override
        public void OnEnd()
        {
            if(gameLoop.GetResult() == null)
            {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            else
            {
                GameLoop.Result result = gameLoop.GetResult();
                Intent intent = new Intent(getApplicationContext(), NewScore.class);
                intent.putExtra("time", result.time);
                intent.putExtra("score", result.score);
                intent.putExtra("mapName", result.name);
                startActivity(intent);
            }
        }
    };
}
