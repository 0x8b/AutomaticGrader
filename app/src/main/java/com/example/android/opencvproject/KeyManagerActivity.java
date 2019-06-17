package com.example.android.opencvproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.android.opencvproject.model.AnswerKey;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyManagerActivity extends AppCompatActivity {

    TextView txt_warning;
    EditText etxt_name;
    EditText etxt_answer_key;
    EditText etxt_number_of_questions;
    EditText etxt_number_of_answers;
    Button btn_add, btn_set;

    private FirebaseFirestore firestoreDB;
    String id = "";
    String serialized = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_key);

        setTitle(R.string.creating_key);

        txt_warning = findViewById(R.id.txt_warning);
        etxt_name = findViewById(R.id.etxt_name);
        etxt_answer_key = findViewById(R.id.etxt_answer_key);
        etxt_number_of_questions = findViewById(R.id.etxt_number_of_questions);
        etxt_number_of_answers = findViewById(R.id.etxt_number_of_answers);
        btn_add = findViewById(R.id.btn_add);
        btn_set = findViewById(R.id.btn_set);

        etxt_answer_key.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetWarning();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etxt_number_of_questions.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetWarning();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etxt_number_of_answers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetWarning();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txt_warning.setTextColor(getResources().getColor(R.color.warning));

        firestoreDB = FirebaseFirestore.getInstance();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            id = bundle.getString("UpdateAnswerKeyId");

            etxt_name.setText(bundle.getString("UpdateAnswerKeyName"));
            etxt_answer_key.setText(bundle.getString("UpdateAnswerKeyAnswerKey"));
            etxt_number_of_questions.setText(bundle.getString("UpdateAnswerKeyNumOfQuestions"));
            etxt_number_of_answers.setText(bundle.getString("UpdateAnswerKeyNumOfAnswers"));
            serialized = bundle.getString("UpdateAnswerKeySerialized");
        }

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etxt_name.getText().toString();
                String answer_key = etxt_answer_key.getText().toString();
                String original_answer_key = answer_key;
                String number_of_questions = etxt_number_of_questions.getText().toString();
                String number_of_answers = etxt_number_of_answers.getText().toString();

                Context ctx = getApplicationContext();

                if (number_of_answers.isEmpty() || number_of_questions.isEmpty() || answer_key.isEmpty() || name.isEmpty()) {
                    setWarning("Uzupełnij wszystkie pola!");
                    return;
                }

                Integer num_of_questions = Integer.parseInt(number_of_questions);
                Integer num_of_answers = Integer.parseInt(number_of_answers);

                if (num_of_questions == 0) {
                    setWarning("Liczba pytań nie może wynosić 0!");
                    return;
                }

                if (num_of_answers == 0) {
                    setWarning("Liczba możliwych wariantów nie może być równa 0!");
                    return;
                }

                if (answer_key.length() == 0) {
                    setWarning("Pusty klucz!");
                    return;
                }

                Pattern tester = Pattern.compile("^([a-z]|\\([a-z]+\\))+$");
                Matcher tester_matcher = tester.matcher(answer_key);

                if (!tester_matcher.find()) {
                    setWarning("Nieprawidłowa struktura klucza!");
                    return;
                }

                List<String> answer_key_list = new ArrayList<>();
                String characters = "(abcdefghijklmnopqrstuvwxyz)";

                String char_range;

                if (num_of_answers == 1) {
                    char_range = "a";
                } else {
                    if (num_of_answers > 26) {
                        setWarning("Niepoprawana liczba wariantów odpowiedzi!");
                        return;
                    }

                    char_range = "a-" + characters.charAt(num_of_answers);
                }

                Pattern multi = Pattern.compile("^\\([" + char_range + "]+\\)");
                Pattern single = Pattern.compile("^[" + char_range + "]");

                while (answer_key.length() > 0) {
                    Matcher m = multi.matcher(answer_key);
                    Matcher s = single.matcher(answer_key);

                    if (m.find(0)) {
                        answer_key_list.add(answer_key.substring(0 + 1, m.end() - 1)); // np. (ab) -> ab
                        answer_key = answer_key.substring(m.end());
                    } else if (s.find(0)) {
                        answer_key_list.add(answer_key.substring(0, s.end()));
                        answer_key = answer_key.substring(s.end());
                    } else {
                        setWarning("Nieprawidłowy znak w kluczu");
                        return;
                    }
                }


                if (num_of_questions != answer_key_list.size()) {
                    setWarning("Nieprawidłowa długość klucza!");
                    return;
                }

                List<Set<Integer>> ans = new ArrayList<>();

                for (String question : answer_key_list) {
                    Set<Integer> set = new HashSet<>();

                    for (Character c : question.toCharArray()) {
                        Integer index = c - 'a';

                        if (set.contains(index)) {
                            setWarning("Zdublowany wariant w odpowiedzi!");
                            return;
                        }

                        set.add(index);
                    }

                    ans.add(set);
                }

                Gson gson = new Gson();
                serialized = gson.toJson(ans);

                if (id.length() > 0) {
                    updateAnswerKey(id, name, original_answer_key, number_of_questions, number_of_answers, serialized);
                } else {
                    addAnswerKey(name, original_answer_key, number_of_questions, number_of_answers, serialized);
                }

                finish();
            }
        });

        btn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString(MainActivity.NAME, etxt_name.getText().toString());
                editor.putString(MainActivity.ANSWER_KEY, etxt_answer_key.getText().toString());
                editor.putString(MainActivity.NUM_OF_QUESTIONS, etxt_number_of_questions.getText().toString());
                editor.putString(MainActivity.NUM_OF_ANSWERS, etxt_number_of_answers.getText().toString());
                editor.putString(MainActivity.SERIALIZED, serialized);
                editor.apply();

                Toast.makeText(getApplicationContext(), "Wybrano klucz", Toast.LENGTH_SHORT).show();

                finish();
            }
        });
    }

    private void updateAnswerKey(String id, String name, String answer_key, String number_of_questions, String number_of_answers, String serialized) {
        Map<String, Object> answerKey = (new AnswerKey(id, name, answer_key, number_of_questions, number_of_answers, serialized)).toMap();

        firestoreDB.collection(KeyManager.COLLECTION_PATH)
                .document(id)
                .set(answerKey)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Toast.makeText(getApplicationContext(), "Klucz odpowiedzi został zaktualizowany.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Klucz odpowiedzi nie został zaktualizowany!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addAnswerKey(String name, String answer_key, String number_of_questions, String number_of_answers, String serialized) {
        Map<String, Object> answerKey = new AnswerKey(name, answer_key, number_of_questions, number_of_answers, serialized).toMap();

        firestoreDB.collection(KeyManager.COLLECTION_PATH)
                .add(answerKey)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "Dodano klucz odpowiedzi.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Klucz odpowiedzi nie został dodany!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setWarning(String message) {
        txt_warning.setText(message);
    }

    public void resetWarning() {
        txt_warning.setText("");
    }
}