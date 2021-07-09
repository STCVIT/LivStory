package com.yashkasera.livstoryuploaddata.model;

import java.util.List;

public class SoundModel {
    private final String id;
    private final String type;
    private final String media;
    private final List<String> keywords;

    public SoundModel(String id,String type, String media, List<String> keywords) {
        this.id = id;
        this.type = type;
        this.media = media;
        this.keywords = keywords;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMedia() {
        return media;
    }

    public List<String> getKeywords() {
        return keywords;
    }

}
