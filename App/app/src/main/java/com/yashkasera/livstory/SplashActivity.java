package com.yashkasera.livstory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.yashkasera.livstory.modal.RequestModel;
import com.yashkasera.livstory.modal.SoundResponseModel;
import com.yashkasera.livstory.retrofit.RetrofitInstance;
import com.yashkasera.livstory.retrofit.RetrofitInterface;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private String text = "Warming up resources";
    private Context context = this;
    private TextView textView;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        textView = findViewById(R.id.textView);
        startActivity();
    }

    private void startActivity() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LivStory", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isFirstLaunch", true)) {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                startActivity(new Intent(context, WelcomeActivity.class));
                finish();
            }, 1000);
        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        } else
            startContainer();
    }

    private void startContainer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    count++;
                    switch (count % 3) {
                        case 0:
                            textView.setText(text + ".");
                            break;
                        case 1:
                            textView.setText(text + "..");
                            break;
                        case 2:
                            textView.setText(text + "...");
                            break;
                        default:
                            textView.setText(text + "");
                            break;
                    }
                });
            }
        }, 0, 1000);
        RetrofitInterface retrofitInterface = RetrofitInstance.getRetrofitInstance().create(RetrofitInterface.class);
        Call<SoundResponseModel> call = retrofitInterface.getSound(new RequestModel("lion"));
        call.enqueue(new Callback<SoundResponseModel>() {
            @Override
            public void onResponse(@NonNull Call<SoundResponseModel> call, @NonNull retrofit2.Response<SoundResponseModel> response) {
                startActivity(new Intent(context, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<SoundResponseModel> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                text = "Please make sure you are connected to the internet. Retrying";
                timer.cancel();
                startContainer();
            }
        });
    }

    private void checkPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, 1001);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity();
            } else {
                View view = findViewById(android.R.id.content);
                Snackbar.make(view, "Please allow microphone access to use this app",
                        BaseTransientBottomBar.LENGTH_SHORT)
                        .setAction("Allow", v -> checkPermission())
                        .show();
            }
        }
    }
}