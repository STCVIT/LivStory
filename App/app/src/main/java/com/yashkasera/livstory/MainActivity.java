package com.yashkasera.livstory;

import static com.yashkasera.livstory.Functions.hexStringToByteArray;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yashkasera.livstory.modal.ListResponseModel;
import com.yashkasera.livstory.modal.RequestModel;
import com.yashkasera.livstory.modal.SoundResponseModel;
import com.yashkasera.livstory.retrofit.RetrofitInstance;
import com.yashkasera.livstory.retrofit.RetrofitInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.internal.EverythingIsNonNull;

public class MainActivity extends AppCompatActivity implements RecognitionListener {
    private static final String TAG = "MainActivity";
    private final Context context = this;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ObjectAnimator animator1, animator2;
    private TextView textView;
    private SwitchCompat playAutomatically;
    private ImageButton record;
    private boolean isListening = false;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private ChipGroup chipGroup;
    private ProgressBar progressBar;
    private int exitCount = 0;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        record.setOnClickListener(v -> {
            if (isListening) {
                isListening = false;
                speechRecognizer.stopListening();
                textView.setHint("Press mic button to start speaking...");
            } else {
                isListening = true;
                speechRecognizer.startListening(speechRecognizerIntent);
                textView.setText("");
                textView.setHint("Listening...");
            }
        });
        findViewById(R.id.cardView).setOnClickListener(v -> openDialog());
        findViewById(R.id.text).setOnClickListener(v -> openDialog());
        findViewById(R.id.options).setOnClickListener(v -> {
            Context wrapper = new ContextThemeWrapper(this, R.style.PopUp);
            PopupMenu popup = new PopupMenu(wrapper, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.options, popup.getMenu());
            popup.show();
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.help:
                        startActivity(new Intent(context, WelcomeActivity.class));
                        return true;
                    case R.id.suggest:
                        reportFragment();
                        return true;
                }
                return false;
            });
        });
    }

    private void openDialog() {
        FragmentManager fm = getSupportFragmentManager();
        CardViewFragment cardViewFragment = CardViewFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString("text", textView.getText().toString());
        cardViewFragment.setArguments(bundle);
        cardViewFragment.show(fm, "fullscreen");
    }

    public void getSound(String text) {
        RetrofitInterface retrofitInterface = RetrofitInstance.getRetrofitInstance().create(RetrofitInterface.class);
        Call<SoundResponseModel> call = retrofitInterface.getSound(new RequestModel(text));
        call.enqueue(new Callback<SoundResponseModel>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<SoundResponseModel> call, retrofit2.Response<SoundResponseModel> response) {
                SoundResponseModel soundResponseModel = response.body();
                if (soundResponseModel != null) {
                    Map.Entry<String, String> entry = soundResponseModel
                            .getSound()
                            .entrySet()
                            .iterator()
                            .next();
                    logSearch(entry.getKey());
                    String mp3_64 = entry.getValue();
                    if (Objects.requireNonNull(mp3_64).length() > 0) {
                        textView.setText(getSpannableString(context, text, soundResponseModel.getSound()));
                        playMp3(mp3_64);
                    } else {
                        noSound();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<SoundResponseModel> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                startListening();
            }
        });
    }

    private void logSearch(String search) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, search);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
    }

    private void reportFragment() {
        FragmentManager fm = getSupportFragmentManager();
        ReportFragment reportFragment = ReportFragment.newInstance();
        reportFragment.show(fm, "ReportFragment");
    }

    public void getList(String text) {
        RetrofitInterface retrofitInterface = RetrofitInstance.getRetrofitInstance().create(RetrofitInterface.class);
        Call<ListResponseModel> call = retrofitInterface.getList(new RequestModel(text));
        call.enqueue(new Callback<ListResponseModel>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<ListResponseModel> call, retrofit2.Response<ListResponseModel> response) {
                Log.d(TAG, "onResponse() returned: " + response.body());
                ListResponseModel listResponseModel = response.body();
                if (listResponseModel != null) {
                    progressBar.setVisibility(View.GONE);
                    if (listResponseModel.getSounds().keySet().size() == 0)
                        noSound();
                    Log.d(TAG, "onResponse() returned: " + listResponseModel.getSounds().keySet());
                    addSoundChips(listResponseModel.getSounds());
                    textView.setText(getSpannableString(context, text, listResponseModel.getSounds()));
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<ListResponseModel> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                progressBar.setVisibility(View.GONE);
                noSound();
            }
        });
    }

    private SpannableString getSpannableString(Context context, String text, Map<String, String> sounds) {
        text += " ";
        SpannableString spannableString = new SpannableString(text);
        for (String str : sounds.keySet()) {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {

                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(ContextCompat.getColor(context, R.color.colorAccent));
                    ds.setUnderlineText(true);
                }
            };
            try {
                int o = text.toLowerCase().indexOf(str.toLowerCase());
                spannableString.setSpan(clickableSpan,
                        o,
                        text.indexOf(' ', o),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (IndexOutOfBoundsException ex) {
                ex.printStackTrace();
            }
        }
        return spannableString;
    }

    private void addSoundChips(Map<String, String> sounds) {
        for (String key : sounds.keySet()) {
            logSearch(key);
            Chip chip = new Chip(context);
            chip.setText(key);
            chip.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context,
                    R.color.colorAccent)));
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context,
                    R.color.chipBackgroundColor)));
            chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context,
                    R.color.colorAccent)));
            chip.setTextStartPaddingResource(R.dimen.margin_medium);
            chip.setTextEndPaddingResource(R.dimen.margin_medium);
            chip.setChipStrokeWidthResource(R.dimen.strokeWidth);
            chip.setOnClickListener(v -> playMp3(sounds.get(key)));
            chipGroup.addView(chip);
        }
    }

    private void init() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        playAutomatically = findViewById(R.id.playAutomatically);
        record = findViewById(R.id.record);
        textView = findViewById(R.id.text);
        chipGroup = findViewById(R.id.chipGroup);
        progressBar = findViewById(R.id.progressBar);
        playAutomatically.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked)
                chipGroup.setVisibility(View.GONE);
            else
                chipGroup.setVisibility(View.VISIBLE);
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.setRecognitionListener(this);
        ImageView pulse1 = findViewById(R.id.pulse1);
        ImageView pulse2 = findViewById(R.id.pulse2);

        animator1 = ObjectAnimator.ofPropertyValuesHolder(
                pulse1,
                PropertyValuesHolder.ofFloat("scaleX", 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f));
        animator2 = ObjectAnimator.ofPropertyValuesHolder(
                pulse2,
                PropertyValuesHolder.ofFloat("scaleX", 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f));

        checkVolume();
    }

    private void checkVolume() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int minVolume = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            minVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC);
        }
        if (currentVolume == minVolume) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please turn the volume up!", BaseTransientBottomBar.LENGTH_SHORT)
                    .show();
        }
    }


    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {
        isListening = true;
        Log.i(TAG, "onBeginningOfSpeech: ");
        textView.setText("");
        textView.setHint("Listening...");
        chipGroup.removeAllViews();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        float scale = (Math.abs(rmsdB) / 5);
        animator1.setValues(
                PropertyValuesHolder.ofFloat("scaleX", scale),
                PropertyValuesHolder.ofFloat("scaleY", scale));
        animator1.setInterpolator(new FastOutSlowInInterpolator());
        animator1.start();
        animator2.setValues(
                PropertyValuesHolder.ofFloat("scaleX", scale * 1.4f),
                PropertyValuesHolder.ofFloat("scaleY", scale * 1.4f));
        animator2.setInterpolator(new FastOutSlowInInterpolator());
        animator2.start();
    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {
        isListening = false;
        Log.e(TAG, "onError: " + Functions.getErrorText(error));
        View view = findViewById(android.R.id.content);
        textView.setHint("Press mic button to start speaking...");
        progressBar.setVisibility(View.GONE);
        if (error == SpeechRecognizer.ERROR_NO_MATCH) {
            noSound();
        } else if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            Snackbar.make(view, "Please allow microphone access to use this app",
                    BaseTransientBottomBar.LENGTH_SHORT)
                    .setAction("ALLOW", v ->
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.RECORD_AUDIO}, 1001))
                    .show();
        }
    }

    private void noSound() {
        Toast.makeText(context, "Couldn't find any sounds!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResults(Bundle results) {
        isListening = false;
        ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        textView.setText(data.get(0));
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        if (playAutomatically.isChecked())
            getSound(data.get(0));
        else
            getList(data.get(0));
    }

    private void startListening() {
        if (playAutomatically.isChecked()) {
            speechRecognizer.startListening(speechRecognizerIntent);
            textView.setText("");
            textView.setHint("Listening...");
            isListening = true;
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        textView.setText(data.get(0));
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onPause() {
        super.onPause();
        if (speechRecognizer != null)
            speechRecognizer.stopListening();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }

    private void playMp3(String s) {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            byte[] mp3SoundByteArray = hexStringToByteArray(s);
            File tempMp3 = File.createTempFile("livstory", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();
            FileInputStream fis = new FileInputStream(tempMp3);
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            checkVolume();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(false);
            progressBar.setProgress(100);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.reset();
                progressBar.setVisibility(View.GONE);
                if (playAutomatically.isChecked())
                    startListening();
            });
        } catch (IOException | IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        exitCount++;
        View view = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(context, view, "Press back again to exit", BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                exitCount = 0;
            }
        });
        snackbar.show();
        if (exitCount == 2) {
            super.onBackPressed();
        }
    }

}
