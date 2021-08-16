package in.stcvit.livstory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class WelcomeActivity extends AppCompatActivity {
    private final Context context = this;
    private final int NoOfSlides = 2;
    private final int[] images = {R.drawable.ic_onboarding_1, R.drawable.ic_onboarding_2};
    private final int[] texts = {R.string.onboarding1, R.string.onboarding2};
    SharedPreferences.Editor editor;
    private int position = 0;
    private ImageView imageView;
    private TextView textView;
    private Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        SharedPreferences sharedPreferences = context.getSharedPreferences("LivStory", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        next = findViewById(R.id.next);
        Animation slide_in_right = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        next.setOnClickListener(v -> {
            position++;
            if (position < NoOfSlides) {
                imageView.setImageDrawable(ContextCompat.getDrawable(context, images[position]));
                imageView.startAnimation(slide_in_right);
                textView.setText(getString(texts[position]));
                textView.startAnimation(slide_in_right);
                if (position == NoOfSlides - 1)
                    next.setText(getString(R.string.continue_btn));
                else
                    next.setText(R.string.next);
            } else {
                editor.putBoolean("isFirstLaunch", false);
                editor.apply();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, 1001);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(context, MainActivity.class));
                finish();
            } else {
                View view = findViewById(android.R.id.content);
                Snackbar.make(view, "Please allow microphone access to use this app",
                        BaseTransientBottomBar.LENGTH_SHORT)
                        .setAction("Allow", v -> ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.RECORD_AUDIO}, 1001))
                        .show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (position > 0) {
            position--;
            Animation slide_in_left = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
            imageView.setImageDrawable(ContextCompat.getDrawable(context, images[position]));
            imageView.startAnimation(slide_in_left);
            textView.setText(getString(texts[position]));
            textView.startAnimation(slide_in_left);
            if (position != NoOfSlides - 1)
                next.setText(getString(R.string.next));
        } else {
            super.onBackPressed();
        }
    }
}