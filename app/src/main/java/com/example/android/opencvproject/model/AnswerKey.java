package com.example.android.opencvproject.model;

import java.util.HashMap;
import java.util.Map;

public class AnswerKey {

    private String id;
    private String name;
    private String answerKey;
    private String numberOfQuestion;
    private String numberOfAnswers;
    private String serialized;

    public AnswerKey() {
    }

    public AnswerKey(String id, String name, String answerKey, String number_of_questions, String numberOfAnswers, String serialized) {
        this.id = id;
        this.name = name;
        this.answerKey = answerKey;
        this.numberOfQuestion = number_of_questions;
        this.numberOfAnswers = numberOfAnswers;
        this.serialized = serialized;
    }

    public AnswerKey(String name, String answerKey, String number_of_questions, String numberOfAnswers, String serialized) {
        this.name = name;
        this.answerKey = answerKey;
        this.numberOfQuestion = number_of_questions;
        this.numberOfAnswers = numberOfAnswers;
        this.serialized = serialized;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAnswerKey() {
        return answerKey;
    }

    public void setAnswerKey(String answerKey) {
        this.answerKey = answerKey;
    }

    public String getNumberOfQuestion() {
        return numberOfQuestion;
    }

    public void setNumberOfQuestion(String numberOfQuestion) {
        this.numberOfQuestion = numberOfQuestion;
    }

    public String getNumberOfAnswers() {
        return numberOfAnswers;
    }

    public void setNumberOfAnswers(String numberOfAnswers) {
        this.numberOfAnswers = numberOfAnswers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialized() {
        return serialized;
    }

    public void setSerialized(String serialized) {
        this.serialized = serialized;
    }

    public Map<String, Object> toMap() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", this.name);
        map.put("answerKey", this.answerKey);
        map.put("numberOfQuestion", this.numberOfQuestion);
        map.put("numberOfAnswers", this.numberOfAnswers);
        map.put("serialized", this.serialized);

        return map;
    }
}