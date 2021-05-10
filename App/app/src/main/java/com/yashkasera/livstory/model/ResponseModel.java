package com.yashkasera.livstory.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ResponseModel {

    @SerializedName("sounds")
    ArrayList<String> sounds;

    public ResponseModel(ArrayList<String> sounds) {
        this.sounds = sounds;
    }

    public ArrayList<String> getSounds() {
        return sounds;
    }

    public void setSounds(ArrayList<String> sounds) {
        this.sounds = sounds;
    }
}
