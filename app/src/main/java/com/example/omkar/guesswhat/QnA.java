package com.example.omkar.guesswhat;

import java.util.ArrayList;

/**
 * Created by omkar on 12/17/2017.
 */

public class QnA {

    // Fields
    private String question;
    private String photoUrl;
    private ArrayList<String> answer;
    private int noOfAns;

    public QnA() {

    }

    // Constructor
    public QnA(String question, String photoUrl, ArrayList<String> answer, int noOfAns){
        this.question = question;
        this.photoUrl = photoUrl;
        this.answer = answer;
        this.noOfAns = noOfAns;
    }

    // Helper functions for QnA object
    public String getQuestion(){
        return this.question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public ArrayList<String> getAnswer() {
        return this.answer;
    }

    public void setAnswer(ArrayList<String> answer) {
        this.answer = answer;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getNoOfAns() {
        return this.noOfAns;
    }

    public void setNoOfAns(int noOfAns) {
        this.noOfAns = noOfAns;
    }
}
