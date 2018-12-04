package com.example.jhalm.tamz_projekt_adventure;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;

public class MapSelectActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_select);

        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        File mapsDirectory = new File(Environment.getExternalStorageDirectory(), sharedPreferences.getString("externalDirectory", "projectMaps") + "/maps");
        if(mapsDirectory.exists())
        {
            File[] maps = mapsDirectory.listFiles(this.filenameFilter);
            ArrayAdapter<String> mapsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            for(int i = 0; i < maps.length; i++)
            {
                mapsAdapter.add(maps[i].getName());
            }
            ListView listView = (ListView) findViewById(R.id.map_list_view);
            listView.setAdapter(mapsAdapter);
            listView.setOnItemClickListener(this.onMapClick);
        }

    }

    private FilenameFilter filenameFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".xml");
        }
    };

    private AdapterView.OnItemClickListener onMapClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) parent.getAdapter();
            Intent intent = new Intent(view.getContext(), CanvasActivity.class);
            intent.putExtra("mapName", arrayAdapter.getItem(position));
            startActivity(intent);
            finish();
        }
    };
}
