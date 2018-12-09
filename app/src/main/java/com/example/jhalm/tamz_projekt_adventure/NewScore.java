package com.example.jhalm.tamz_projekt_adventure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NewScore extends Activity
{

    private long time;
    private int score;
    private String mapName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_score);

        Intent intent = getIntent();
        this.time = intent.getLongExtra("time", 0);
        this.score = intent.getIntExtra("score", 0);
        this.mapName = intent.getStringExtra("mapName");

        TextView timeView = findViewById(R.id.textTime);
        timeView.setText( "Time: " + Long.toString(this.time / 60000) + ":" + Long.toString((this.time % 60000) / 1000) + "." + Long.toString(this.time % 1000));

        TextView scoreView = findViewById(R.id.textScore);
        scoreView.setText("Score: " + Integer.toString(this.score));

        Button button = findViewById(R.id.btn_new_score);
        button.setOnClickListener(this.newScore);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    View.OnClickListener newScore = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Highscore.DTO dto = new Highscore.DTO();
            dto.mapName = mapName;
            dto.time = time;
            dto.score = score;
            EditText playerName = findViewById(R.id.editText_player);
            dto.player = playerName.getText().toString();

            Highscore highscore = new Highscore(getApplicationContext());
            highscore.Insert(dto);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    };
}
