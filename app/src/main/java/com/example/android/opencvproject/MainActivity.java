package com.example.android.opencvproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_KEY = "com.example.android.opencvproject.EXTRA_KEY";

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String KEY = "key";

    private EditText keyText;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        keyText = findViewById(R.id.etxt_key);

        loadData();
        updateViews();
    }

    public void saveLastKey(View v) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY, keyText.getText().toString());
        editor.apply();

        Toast.makeText(this, "Zapisano klucz", Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        key = sharedPreferences.getString(KEY, "");

        keyText.setText(key);
    }

    public void updateViews() {
        keyText.setText(key);
    }

    public void openImageProcessorActivity(View v) {
        String key = keyText.getText().toString().toLowerCase();

        Intent intent = new Intent(this, ImageProcessor.class);
        intent.putExtra(EXTRA_KEY, key);

        startActivity(intent);
    }

    public void openKeyManagerActivity(View v) {
        Intent intent = new Intent(this, KeyManager.class);

        startActivity(intent);
    }
}
