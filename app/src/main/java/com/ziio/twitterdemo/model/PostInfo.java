package com.ziio.twitterdemo.model;

public class PostInfo {

    private String userUID;

    private String text;

    private String postImage;

    public PostInfo(String userUID, String text, String postImage) {
        this.userUID = userUID;
        this.text = text;
        this.postImage = postImage;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getUserUID() {
        return userUID;
    }

    public String getText() {
        return text;
    }

    public String getPostImage() {
        return postImage;
    }
}
