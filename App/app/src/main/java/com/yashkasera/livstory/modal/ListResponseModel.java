package com.yashkasera.livstory.modal;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ListResponseModel {

    @SerializedName("sounds")

    Map<String, String> sounds;

    public ListResponseModel(Map<String, String> sounds) {
        this.sounds = sounds;
    }

    public Map<String, String> getSounds() {
        return sounds;
    }

}
