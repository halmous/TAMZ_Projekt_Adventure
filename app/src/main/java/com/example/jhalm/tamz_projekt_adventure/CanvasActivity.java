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
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

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
    public void onBackPressed()
    {
        if(gameLoop.GetStatus() == GameLoop.STATUS_RUN)
        {
            gameLoop.PauseOrResume();
        }
        else if(gameLoop.GetStatus() == GameLoop.STATUS_PAUSE)
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
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

            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            if(!vibrator.hasVibrator() || !sharedPreferences.getBoolean( "enableVibration",true))
                vibrator = null;
            gameLoop = new GameLoop(gameCanvas, mapLoader.GetResult(), frameRates[sharedPreferences.getInt("frameRates", 1)], gameEndHandler, vibrator);

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

            finish();
        }
    };

    View.OnKeyListener onBackPressed = new View.OnKeyListener()
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
            {
                gameLoop.PauseOrResume();
            }
            return true;
        }
    };
}
