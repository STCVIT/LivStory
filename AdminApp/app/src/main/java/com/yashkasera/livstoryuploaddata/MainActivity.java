package com.yashkasera.livstoryuploaddata;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.yashkasera.livstoryuploaddata.model.SoundModel;
import com.yashkasera.livstoryuploaddata.model.WordModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public static ArrayList<SoundModel> soundModelArrayList = new ArrayList<>();
    public static ArrayList<WordModel> userWordModelArrayList = new ArrayList<>();
    public static ArrayList<WordModel> modelWordModelArrayList = new ArrayList<>();
    public static DocumentSnapshot lastVisibleSound;
    public static MutableLiveData<Integer> playPosition = new MutableLiveData<>();
    public static MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();
    private final Context context = this;
    private final Timer timer = new Timer();
    private ProgressBar progressBar;
    private TextView keywords, type;
    private ImageButton play;
    private int exitCount = 0;

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
        isPlaying.postValue(false);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager viewPager = findViewById(R.id.viewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        keywords = findViewById(R.id.keywords);
        type = findViewById(R.id.type);
        play = findViewById(R.id.play);
        progressBar = findViewById(R.id.progressBar);
        play.setOnClickListener(v -> {
            if (isPlaying.getValue() && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying.postValue(false);
            } else {
                mediaPlayer.start();
                isPlaying.postValue(true);
            }
        });

        isPlaying.observe(this, playing -> {
            if (playing)
                play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_pause_24));
            else
                play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_play_arrow_24));
        });
        findViewById(R.id.next).setOnClickListener(v -> {
            if (playPosition.getValue() == soundModelArrayList.size() - 1)
                playPosition.postValue(0);
            else
                playPosition.postValue(playPosition.getValue() + 1);

        });
        findViewById(R.id.previous).setOnClickListener(v -> {
            if (playPosition.getValue() == 0)
                playPosition.postValue(soundModelArrayList.size() - 1);
            else
                playPosition.postValue(playPosition.getValue() - 1);
        });
        playPosition.observe(this, this::playMp3);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return true;
        } else if (item.getItemId() == R.id.add) {
            startActivity(new Intent(context, UploadDataActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void playMp3(int position) {
        try {
            if (soundModelArrayList.get(position) == null) throw new Exception();
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.keywords.setText(String.join(", ", soundModelArrayList.get(position).getKeywords()));
            } else {
                String str = "";
                for (String k : soundModelArrayList.get(position).getKeywords()) {
                    str += k + ", ";
                }
                keywords.setText(str);
            }
            type.setText(soundModelArrayList.get(position).getType());
            byte[] mp3SoundByteArray = hexStringToByteArray(soundModelArrayList.get(position).getMedia());
            File tempMp3 = File.createTempFile("livstory", "mp3", context.getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fileOutputStream = new FileOutputStream(tempMp3);
            fileOutputStream.write(mp3SoundByteArray);
            fileOutputStream.close();
            FileInputStream fileInputStream = new FileInputStream(tempMp3);
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fileInputStream.getFD());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(mediaPlayer1 -> {
                mediaPlayer1.start();
                play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_pause_24));
                isPlaying.postValue(true);
            });
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(mediaPlayer.getDuration());

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            progressBar.setProgress(mediaPlayer.getCurrentPosition());
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 100);

            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.reset();
                playPosition.postValue(playPosition.getValue() + 1);
                isPlaying.postValue(false);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(context, "Unable to play sound! " + ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.reset();
            }
        }
    }

    @Override
    public void onBackPressed() {
        exitCount++;
        View view = findViewById(android.R.id.content);
        if (view != null)
            Snackbar.make(context, view, "Press back again to exit",
                    BaseTransientBottomBar.LENGTH_SHORT)
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            exitCount = 0;
                        }
                    }).show();
        if (exitCount == 2) {
            super.onBackPressed();
        }
    }
}
