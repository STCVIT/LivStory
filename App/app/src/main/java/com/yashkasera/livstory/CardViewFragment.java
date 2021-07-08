package com.yashkasera.livstory;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.yashkasera.livstory.modal.ListResponseModel;
import com.yashkasera.livstory.modal.RequestModel;
import com.yashkasera.livstory.retrofit.RetrofitInstance;
import com.yashkasera.livstory.retrofit.RetrofitInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.internal.EverythingIsNonNull;

import static com.yashkasera.livstory.Functions.hexStringToByteArray;

public class CardViewFragment extends DialogFragment {
    private static final String TAG = "CardViewFragment";
    private TextView textView;
    private ProgressBar progressBar;
    private ObjectAnimator animator1, animator2;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Context context;
    private boolean isListening = false;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    public CardViewFragment() {
    }

    public static CardViewFragment newInstance() {
        return new CardViewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
        context = getContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String text = "";
        if (getArguments() != null) {
            text = getArguments().getString("text");
        }
        View view = inflater.inflate(R.layout.fragment_cardview, container, false);
        view.findViewById(R.id.close).setOnClickListener(v -> dismiss());
        textView = view.findViewById(R.id.text);
        textView.setText(text);
        if (text != null && text.length() > 0)
            getList(text);
        view.findViewById(R.id.record).setOnClickListener(v -> {
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
        ImageView pulse1 = view.findViewById(R.id.pulse1);
        ImageView pulse2 = view.findViewById(R.id.pulse2);
        progressBar = view.findViewById(R.id.progressBar);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        animator1 = ObjectAnimator.ofPropertyValuesHolder(
                pulse1,
                PropertyValuesHolder.ofFloat("scaleX", 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f));
        animator2 = ObjectAnimator.ofPropertyValuesHolder(
                pulse2,
                PropertyValuesHolder.ofFloat("scaleX", 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f));
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {
                isListening = true;
                textView.setText("");
                textView.setHint("Listening...");
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
                textView.setHint("Press mic button to start speaking...");
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    Snackbar.make(view, "Unable to understand. Please try again",
                            BaseTransientBottomBar.LENGTH_SHORT)
                            .show();
                } else if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    Snackbar.make(view, "Please allow microphone access in to use this app",
                            BaseTransientBottomBar.LENGTH_SHORT)
                            .setAction("ALLOW", v ->
                                    ActivityCompat.requestPermissions(requireActivity(),
                                            new String[]{Manifest.permission.RECORD_AUDIO}, 1001))
                            .show();
                }
            }

            @Override
            public void onResults(Bundle results) {
                isListening = false;
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                textView.setText(data.get(0));
                Log.d(TAG, "onResults() returned: " + data.get(0));
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
                getList(data.get(0));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                textView.setText(data.get(0));
            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
        return view;
    }

    private SpannableString getSpannableString(String text, Map<String, String> sounds) {
        text += " ";
        SpannableString spannableString = new SpannableString(text);
        for (String str : sounds.keySet()) {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    playMp3(sounds.get(str));
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

    private void playMp3(String s) {
        try {
            byte[] mp3SoundByteArray = hexStringToByteArray(s);
            File tempMp3 = File.createTempFile("livstory", "mp3", requireContext().getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();
            FileInputStream fis = new FileInputStream(tempMp3);
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(false);
            progressBar.setProgress(100);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.reset();
                progressBar.setVisibility(View.GONE);
            });
        } catch (IOException | IllegalStateException ex) {
            ex.printStackTrace();
        }
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
                    if (listResponseModel.getSounds().keySet().size() == 0) {
                        Snackbar.make(textView, "Couldn't find any sounds! Please try again",
                                BaseTransientBottomBar.LENGTH_SHORT)
                                .show();
                    }
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "onResponse() returned: " + listResponseModel.getSounds().keySet());
                    textView.setText(getSpannableString(text, listResponseModel.getSounds()));
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<ListResponseModel> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                Snackbar.make(textView, "Couldn't find any sounds! Please try again",
                        BaseTransientBottomBar.LENGTH_SHORT)
                        .show();
            }
        });
    }
}