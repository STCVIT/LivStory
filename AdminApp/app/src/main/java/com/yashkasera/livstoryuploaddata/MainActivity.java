package com.yashkasera.livstoryuploaddata;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yashkasera.livstoryuploaddata.adapter.RecyclerAdapter;
import com.yashkasera.livstoryuploaddata.model.SoundModel;
import com.yashkasera.livstoryuploaddata.util.ClickListener;
import com.yashkasera.livstoryuploaddata.util.RecyclerTouchListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    private final Context context = this;
    RecyclerAdapter recyclerAdapter;
    ArrayList<SoundModel> list;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageButton play;
    private TextView keywords, type;
    private int playPosition = 0;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.add).setOnClickListener(v ->
                startActivity(new Intent(context, UploadDataActivity.class)));
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Fetching Data");
        keywords = findViewById(R.id.keywords);
        type = findViewById(R.id.type);
        list = new ArrayList<>();
        recyclerAdapter = new RecyclerAdapter(context, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(recyclerAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        progressDialog.show();
        db.collection("sounds")
                .orderBy("type")
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            SoundModel soundModel = new SoundModel(
                                    document.getId(),
                                    document.getString("type"),
                                    document.getString("media"),
                                    (List<String>) document.get("keywords")
                            );
                            recyclerAdapter.addItem(soundModel);
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Snackbar.make(findViewById(android.R.id.content), "Unable to fetch records!",
                                BaseTransientBottomBar.LENGTH_INDEFINITE).show();
                    }
                });
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                playMp3(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you  sure you want to delete this sound? This action cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> db.collection("sounds")
                                .document(list.get(position).getId())
                                .delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Sound deleted Successfully!", Toast.LENGTH_SHORT).show();
                                        recyclerAdapter.notifyItemRemoved(position);
                                        list.remove(position);
                                    } else {
                                        Toast.makeText(context, "Could not delete sound! Please try again later", Toast.LENGTH_SHORT).show();
                                    }

                                }))
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }));
        findViewById(R.id.next).setOnClickListener(v -> {
            if (playPosition == list.size() - 1)
                playMp3(0);
            else
                playMp3(playPosition + 1);
        });
        findViewById(R.id.previous).setOnClickListener(v -> {
            if (playPosition == 0)
                playMp3(list.size() - 1);
            else
                playMp3(playPosition - 1);
        });
        play = findViewById(R.id.play);
        play.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_play_arrow_24));
            } else {
                mediaPlayer.start();
                play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_pause_24));
            }
        });
    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private void playMp3(int position) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                keywords.setText(String.join(", ", list.get(position).getKeywords()));
            } else {
                String str = "";
                for (String k : list.get(position).getKeywords()) {
                    str += k + ", ";
                }
                keywords.setText(str);
            }
            type.setText(list.get(position).getType());
            playPosition = position;
            byte[] mp3SoundByteArray = hexStringToByteArray(list.get(position).getMedia());
            File tempMp3 = File.createTempFile("livstory", "mp3", context.getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();
            FileInputStream fis = new FileInputStream(tempMp3);
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(mediaPlayer1 -> {
                mediaPlayer1.start();
                play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_pause_24));
            });
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(mediaPlayer.getDuration());

            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        progressBar.setProgress(mediaPlayer.getCurrentPosition());
                    }
                }
            }, 0, 10);

            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.reset();
                playMp3(playPosition + 1);
            });
        } catch (IOException | IllegalStateException ex) {
            ex.printStackTrace();
        }
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_play_arrow_24));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            mediaPlayer.start();
            play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_pause_24));
        }
    }
}