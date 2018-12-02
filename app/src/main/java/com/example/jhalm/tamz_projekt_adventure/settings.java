package com.example.jhalm.tamz_projekt_adventure;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

public class settings extends Activity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);

        CheckBox sound = (CheckBox) findViewById(R.id.check_sound);
        sound.setOnClickListener(soundEnable);
        sound.setChecked(this.sharedPreferences.getBoolean("enableSound", true));

        ImageButton back = (ImageButton) findViewById(R.id.btn_settings_back);

        EditText editText = (EditText) findViewById(R.id.external_directory);
        editText.setText(this.sharedPreferences.getString("externalDirectory", "/projectMaps"));
        editText.addTextChangedListener(externalDirectoryWatcher);

        Spinner spinner = (Spinner) findViewById(R.id.maxfps);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.maxfpsarray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(onFPSChange);
        spinner.setSelection(this.sharedPreferences.getInt("frameRates", 0));

    }

    private AdapterView.OnItemSelectedListener onFPSChange = new AdapterView.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("frameRates", position);
            editor.apply();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private View.OnClickListener soundEnable = new View.OnClickListener() {
        @Override
        public void onClick(View v){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("enableSound", ((CheckBox) v).isChecked());
            editor.apply();
        }
    };

    private View.OnClickListener back = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private TextWatcher externalDirectoryWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("externalDirectory", s.toString());
            editor.apply();
        }
    };
}
