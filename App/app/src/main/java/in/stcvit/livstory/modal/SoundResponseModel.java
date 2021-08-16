package in.stcvit.livstory.modal;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class SoundResponseModel {

    @SerializedName("sound")
    Map<String, String> sound;

    public SoundResponseModel(Map<String, String> sound) {
        this.sound = sound;
    }

    public Map<String, String> getSound() {
        return sound;
    }

}