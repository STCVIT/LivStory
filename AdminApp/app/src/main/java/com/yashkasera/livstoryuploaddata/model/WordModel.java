package com.yashkasera.livstoryuploaddata.model;

import java.io.Serializable;

public class WordModel implements Serializable {
    private String word;
    private long count;

    public WordModel(String word, long count) {
        this.word = word;
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public long getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
