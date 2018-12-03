package com.example.jhalm.tamz_projekt_adventure;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;

public class Highscore extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "highscore.sql";

    public Highscore(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE Highscore (id INTEGER PRIMARY KEY, mapname TEXT, score INTEGER, duration LONG, player TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE Highscore");
    }

    public void Insert(DTO dto)
    {
        ContentValues values = new ContentValues();
        values.put("mapname", dto.mapName);
        values.put("score", dto.score);
        values.put("duration", dto.time);
        values.put("player", dto.player);
        this.getWritableDatabase().insert("Highscore", null, values);
    }

    public List<DTO> SelectAll()
    {
        List<DTO> result = new ArrayList<DTO>();

        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT mapname, score, duration, player FROM Highscore ORDER BY mapname, duration, score", null);

        for(int i = 0; i < cursor.getCount(); i++)
        {
            DTO dto = new DTO();
            cursor.moveToNext();
            dto.mapName = cursor.getString(cursor.getColumnIndex("mapname"));
            dto.score = cursor.getInt(cursor.getColumnIndex("score"));
            dto.time = cursor.getLong(cursor.getColumnIndex("duration"));
            dto.player = cursor.getString(cursor.getColumnIndex("player"));
            result.add(dto);
        }

        return result;
    }

    public static class DTO
    {
        public String mapName;
        public String player;
        public int score;
        public long time;
    }
}
