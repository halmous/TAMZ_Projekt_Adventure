package com.example.jhalm.tamz_projekt_adventure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.List;

public class HighscoreShow extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore_show);

        ListView listView = findViewById(R.id.highscore_listview);
        listView.setAdapter(new ScoreAdapter(this));

        ImageButton back = (ImageButton) findViewById(R.id.btn_highscore_show_back);
        back.setOnClickListener(this.onBackClick);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private View.OnClickListener onBackClick = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            onBackPressed();
        }
    };
}
