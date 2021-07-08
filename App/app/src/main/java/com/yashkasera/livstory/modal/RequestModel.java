package com.yashkasera.livstory.modal;

import com.google.gson.annotations.SerializedName;

public class RequestModel {

    @SerializedName("text")
    String text;

    public RequestModel(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
