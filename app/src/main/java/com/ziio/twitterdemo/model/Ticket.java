package com.ziio.twitterdemo.model;

public class Ticket {

    private String tweetID;
    private String tweetText;
    private String tweetImageURL;
    private String tweetPersonUID;

    public Ticket(String tweetID , String tweetText , String tweetImageURL , String tweetPersonUID){
        this.tweetID = tweetID;
        this.tweetText = tweetText;
        this.tweetImageURL = tweetImageURL;
        this.tweetPersonUID = tweetPersonUID;
    }

    public void setTweetID(String tweetID) {
        this.tweetID = tweetID;
    }

    public void setTweetText(String tweetText) {
        this.tweetText = tweetText;
    }

    public void setTweetImageURL(String tweetImageURL) {
        this.tweetImageURL = tweetImageURL;
    }

    public void setTweetPersonUID(String tweetPersonUID) {
        this.tweetPersonUID = tweetPersonUID;
    }

    public String getTweetID() {
        return tweetID;
    }

    public String getTweetText() {
        return tweetText;
    }

    public String getTweetImageURL() {
        return tweetImageURL;
    }

    public String getTweetPersonUID() {
        return tweetPersonUID;
    }
}
