package com.yashkasera.livstory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.yashkasera.livstory.model.RequestModel;
import com.yashkasera.livstory.model.ResponseModel;
import com.yashkasera.livstory.retrofit.RetrofitInstance;
import com.yashkasera.livstory.retrofit.RetrofitInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;


public class MainActivity extends AppCompatActivity implements RecognitionListener {
    private static final String TAG = "MainActivity";
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    Context context = this;
    Button play;
    private SwitchCompat playAutomatically;
    private ImageButton record;
    private TextView text;
    private boolean isListening = false;
    private SpeechRecognizer speechRecognizer;
    private SpeechProgressView progressView;
    private Intent speechRecognizerIntent;

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewByIds();
        playAutomatically.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                findViewById(R.id.relativeLayout).setVisibility(View.GONE);
            } else {
                findViewById(R.id.relativeLayout).setVisibility(View.VISIBLE);
            }
        });
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.setRecognitionListener(this);

        record.setOnClickListener(v -> {
            if (isListening) {
                record.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_mic_off_24));
                isListening = false;
                progressView.setVisibility(View.GONE);
                speechRecognizer.stopListening();

            } else {
                record.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_mic_24));
                speechRecognizer.startListening(speechRecognizerIntent);
                isListening = true;
            }
        });
    }

    private void findViewByIds() {
        playAutomatically = findViewById(R.id.playAutomatically);
        record = findViewById(R.id.record);
        text = findViewById(R.id.text);
        progressView = findViewById(R.id.progress);
        play = findViewById(R.id.play);
    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG, "onBeginningOfSpeech ");
        play.setEnabled(false);
        progressView.onBeginningOfSpeech();
        text.setText("");
        text.setHint("Listening...");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        progressView.onRmsChanged(rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech()");
        isListening = false;
        progressView.onEndOfSpeech();
        record.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_mic_off_24));
    }

    private void callAPI(String text) {
        View view = findViewById(android.R.id.content);
        RetrofitInterface retrofitInterface = RetrofitInstance.getRetrofitInstance().create(RetrofitInterface.class);
        Call<ResponseModel> call = retrofitInterface.getSound(new RequestModel(text));
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, retrofit2.Response<ResponseModel> response) {
                if (response == null) return;
                ResponseModel responseModel = response.body();
                if (responseModel != null) {
                    if (responseModel.getSounds() == null || responseModel.getSounds().size() == 0) {
                        Snackbar.make(view, "Could not find any suitable audio! Please try a longer paragraph",
                                BaseTransientBottomBar.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    play.setEnabled(true);
                    if (playAutomatically.isChecked())
                        playMp3(responseModel.getSounds().get(0));
                    else {
                        play.setOnClickListener(v -> {
                            playMp3(responseModel.getSounds().get(0));
                        });
                    }
                } else {
                    Snackbar.make(view, "Could not fetch data from our server",
                            BaseTransientBottomBar.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                Toast.makeText(MainActivity.this, "Could not fetch data from our server", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onError(int error) {
        Log.e(TAG, "onError: " + getErrorText(error));
        View view = findViewById(android.R.id.content);
        if (error == SpeechRecognizer.ERROR_NO_MATCH) {
            Snackbar.make(view, "Unable to understand. Please try again",
                    BaseTransientBottomBar.LENGTH_SHORT)
                    .show();
        } else if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            Snackbar.make(view, "Please allow microphone access in to use this app",
                    BaseTransientBottomBar.LENGTH_SHORT)
                    .setAction("ALLOW", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1001);
                        }
                    })
                    .show();
        }
    }

    private void playMp3(String s) {
        try {
            byte[] mp3SoundByteArray = hexStringToByteArray(s);
            File tempMp3 = File.createTempFile("livstory", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            mediaPlayer.reset();
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        text.setText(data.get(0));
        progressView.onResultOrOnError();
        callAPI(data.get(0));

    }

    @Override
    public void onPartialResults(Bundle bundle) {
        ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        text.setText(data.get(0));
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onPause() {
        Log.i(TAG, "pause");
        super.onPause();
        speechRecognizer.stopListening();
    }

}