package com.yashkasera.livstory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class SplashActivity extends AppCompatActivity {
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
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
        } else {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                startActivity(new Intent(context, MainActivity.class));
                finish();
            }, 1000);
        }
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