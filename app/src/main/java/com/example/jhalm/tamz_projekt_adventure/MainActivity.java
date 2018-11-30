package com.example.jhalm.tamz_projekt_adventure;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button settings = (Button) findViewById(R.id.btn_settings);
        settings.setOnClickListener(btnSettings);

        Button newGame = (Button) findViewById(R.id.btn_play);
        newGame.setOnClickListener(btnNewGame);

        try
        {
            /*File externalStorage = Environment.getExternalStorageDirectory();
            File test = new File(externalStorage, "maps.xml");
            InputStream inputStream = new FileInputStream(test);
            XMLParser xmlParser = new XMLParser(inputStream);
            xmlParser.start();
            xmlParser.join();
            xmlParser.GetResult();

            File test = new File(Environment.getExternalStorageDirectory(), "Bricks.png");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            TileParser tileParser = new TileParser(BitmapFactory.decodeFile(test.getAbsolutePath(), options), 64, 64);
            tileParser.start();
            tileParser.join();
            tileParser.GetResult();*/
        }
        catch (Exception e)
        {
            Log.d("TAMZ_Exception", e.toString());
        }
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
            Intent intent = new Intent( v.getContext(), CanvasActivity.class);
            startActivity(intent);
        }
    };
}
