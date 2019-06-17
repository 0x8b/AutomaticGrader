package com.example.android.opencvproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.opencvproject.KeyManager;
import com.example.android.opencvproject.model.AnswerKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.android.opencvproject.KeyManagerActivity;
import com.example.android.opencvproject.R;

import java.util.List;


public class KeyRecyclerViewAdapter extends RecyclerView.Adapter<KeyRecyclerViewAdapter.ViewHolder> {

    private List<AnswerKey> keysList;
    private Context context;
    private FirebaseFirestore firestoreDB;

    public KeyRecyclerViewAdapter(List<AnswerKey> keysList, Context context, FirebaseFirestore firestoreDB) {
        this.keysList = keysList;
        this.context = context;
        this.firestoreDB = firestoreDB;
    }

    @Override
    public KeyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);

        return new KeyRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(KeyRecyclerViewAdapter.ViewHolder holder, int position) {
        final int itemPosition = position;
        final AnswerKey answerKey = keysList.get(itemPosition);

        holder.name.setText(answerKey.getName());
        /*holder.answer_key.setText(answerKey.getAnswerKey());
        holder.number_of_questions.setText(answerKey.getNumberOfQuestion());
        holder.number_of_answers.setText(answerKey.getNumberOfAnswers());*/

        holder.btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAnswerKey(answerKey);
            }
        });

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAnswerKey(answerKey.getId(), itemPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return keysList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView  name; //, answer_key, number_of_questions, number_of_answers;
        ImageView /*btn_set,*/ btn_edit, btn_delete;

        ViewHolder(View view) {
            super(view);
            name                = view.findViewById(R.id.name);
            /*answer_key          = view.findViewById(R.id.answer_key);
            number_of_questions = view.findViewById(R.id.number_of_questions);
            number_of_answers   = view.findViewById(R.id.number_of_answers);*/

            //btn_set    = view.findViewById(R.id.btn_set);
            btn_edit   = view.findViewById(R.id.btn_edit);
            btn_delete = view.findViewById(R.id.btn_delete);
        }
    }

    private void updateAnswerKey(AnswerKey answerKey) {
        Intent intent = new Intent(context, KeyManagerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("UpdateAnswerKeyId", answerKey.getId());
        intent.putExtra("UpdateAnswerKeyName", answerKey.getName());
        intent.putExtra("UpdateAnswerKeyAnswerKey", answerKey.getAnswerKey());
        intent.putExtra("UpdateAnswerKeyNumOfQuestions", answerKey.getNumberOfQuestion());
        intent.putExtra("UpdateAnswerKeyNumOfAnswers", answerKey.getNumberOfAnswers());
        intent.putExtra("UpdateAnswerKeySerialized", answerKey.getSerialized());
        context.startActivity(intent);
    }

    private void deleteAnswerKey(String id, final int position) {
        firestoreDB.collection(KeyManager.COLLECTION_PATH)
                .document(id)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        keysList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, keysList.size());
                        Toast.makeText(context, "Usunięto klucz odpowiedzi!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}