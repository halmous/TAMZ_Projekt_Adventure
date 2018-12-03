package com.example.jhalm.tamz_projekt_adventure;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.SimpleTimeZone;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button settings = (Button) findViewById(R.id.btn_settings);
        settings.setOnClickListener(btnSettings);

        Button newGame = (Button) findViewById(R.id.btn_play);
        newGame.setOnClickListener(btnNewGame);

        Button highscore = (Button) findViewById(R.id.btn_score);
        highscore.setOnClickListener(btnHighscore);
    }

    private View.OnClickListener btnSettings = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent( v.getContext(), settings.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener btnNewGame = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent( v.getContext(), MapSelectActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener btnHighscore = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent( v.getContext(), HighscoreShow.class);
            startActivity(intent);
        }
    };
}
