package com.example.jhalm.tamz_projekt_adventure;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class HighscoreShow extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore_show);

        ListView listView = findViewById(R.id.highscore_listview);
        listView.setAdapter(new OuterScoreAdapter(this));
    }
}
