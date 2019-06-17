package com.example.android.opencvproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.opencvproject.model.AnswerKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.android.opencvproject.adapter.KeyRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class KeyManager extends AppCompatActivity {

    public static final String COLLECTION_PATH = "answer_key_test_2";

    private RecyclerView recyclerView;
    private KeyRecyclerViewAdapter mAdapter;

    private FirebaseFirestore firestoreDB;
    private ListenerRegistration firestoreListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_manager);

        setTitle(R.string.manage_key);

        // TODO: dodac do mainactivity gradientowe tlo
        // TODO: dodac menu w mainactivity z ikonka about
        // TODO: poprawic layout listy

        recyclerView = findViewById(R.id.rvAnswerKeyList);
        firestoreDB = FirebaseFirestore.getInstance();

        loadAnswerKeyList();

        firestoreListener = firestoreDB.collection(COLLECTION_PATH)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        List<AnswerKey> keysList = new ArrayList<>();

                        for (DocumentSnapshot doc : documentSnapshots) {
                            AnswerKey answerKey = doc.toObject(AnswerKey.class);
                            answerKey.setId(doc.getId());
                            keysList.add(answerKey);
                        }

                        mAdapter = new KeyRecyclerViewAdapter(keysList, getApplicationContext(), firestoreDB);
                        recyclerView.setAdapter(mAdapter);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        firestoreListener.remove();
    }

    private void loadAnswerKeyList() {
        firestoreDB.collection(COLLECTION_PATH)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<AnswerKey> keysList = new ArrayList<>();

                            for (DocumentSnapshot doc : task.getResult()) {
                                AnswerKey answerKey = doc.toObject(AnswerKey.class);
                                answerKey.setId(doc.getId());
                                keysList.add(answerKey);
                            }

                            mAdapter = new KeyRecyclerViewAdapter(keysList, getApplicationContext(), firestoreDB);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(mLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            recyclerView.setAdapter(mAdapter);
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null) {
            if (item.getItemId() == R.id.add_answer_key) {
                Intent intent = new Intent(this, KeyManagerActivity.class);
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }
}