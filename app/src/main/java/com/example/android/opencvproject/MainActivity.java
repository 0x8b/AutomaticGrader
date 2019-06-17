package com.example.android.opencvproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.opencvproject.model.AnswerKey;

public class MainActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String NAME = "name";
    public static final String ANSWER_KEY = "answer_key";
    public static final String NUM_OF_QUESTIONS = "num_of_questions";
    public static final String NUM_OF_ANSWERS = "num_of_answers";
    public static final String SERIALIZED = "serialized";

    TextView txt_key_name;//private EditText etxt_key;

    private String key_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_key_name = findViewById(R.id.txt_key_name); //etxt_key = findViewById(R.id.etxt_key);

        loadData();
        updateViews();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        key_name = sharedPreferences.getString(NAME, "");
    }

    public void updateViews() {
        txt_key_name.setText(key_name);
    }

    public void openImageProcessorActivity(View v) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        String key = sharedPreferences.getString(ANSWER_KEY, "");
        String num_of_questions = sharedPreferences.getString(NUM_OF_QUESTIONS, "");
        String num_of_answers = sharedPreferences.getString(NUM_OF_ANSWERS, "");
        String serialized = sharedPreferences.getString(SERIALIZED, "");

        Intent intent = new Intent(this, ImageProcessor.class);
        intent.putExtra(ANSWER_KEY, key);
        intent.putExtra(NUM_OF_QUESTIONS, Integer.parseInt(num_of_questions));
        intent.putExtra(NUM_OF_ANSWERS, Integer.parseInt(num_of_answers));
        intent.putExtra(SERIALIZED, serialized);

        //Toast.makeText(this, serialized, Toast.LENGTH_SHORT).show();

        startActivity(intent);
    }

    public void openKeyManager(View v) {
        Intent intent = new Intent(this, KeyManager.class);

        startActivity(intent);
    }
}
