package com.yashkasera.livstory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements RecognitionListener {
    private static final String TAG = "MainActivity";
    Context context = this;
    private SwitchCompat playAutomatically;
    private ImageButton record;
    private TextView sound, text;
    private boolean isListening = false;
    private SpeechRecognizer speechRecognizer;
    private SpeechProgressView progressView;
    private Intent speechRecognizerIntent;

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
        sound = findViewById(R.id.sound);
        text = findViewById(R.id.text);
        progressView = findViewById(R.id.progress);
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
        progressView.onBeginningOfSpeech();
        text.setText("");
        text.setHint("Listening...");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
//        progressBar.setProgress((int) rmsdB);
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

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        text.setText(data.get(0));
        progressView.onResultOrOnError();
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

