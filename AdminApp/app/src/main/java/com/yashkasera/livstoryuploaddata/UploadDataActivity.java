package com.yashkasera.livstoryuploaddata;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yashkasera.livstoryuploaddata.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class UploadDataActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    FirebaseFirestore db;
    MediaPlayer mediaPlayer;
    Context context = this;
    Uri uri;
    SeekBar seekBar;
    TextInputEditText keywords, type;
    TextInputLayout keywords1, type1;
    TextView name;
    String[] keyword;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_data);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        seekBar = findViewById(R.id.seekBar);
        keywords = findViewById(R.id.keywords);
        type = findViewById(R.id.type);
        keywords1 = findViewById(R.id.keywords1);
        type1 = findViewById(R.id.type1);
        name = findViewById(R.id.name);
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading");
        db = FirebaseFirestore.getInstance();
        if (context.getPackageManager().checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }
        findViewById(R.id.choose).setOnClickListener(v -> {
            Intent intent_upload = new Intent();
            intent_upload.setType("audio/*");
            intent_upload.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent_upload, 1);
        });

        findViewById(R.id.play).setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                else mediaPlayer.start();
            } else {
                if (uri != null) {
                    mediaPlayer = MediaPlayer.create(context, uri);
                    seekBar.setMax(mediaPlayer.getDuration());
                    seekBar.setProgress(0);
                    mediaPlayer.setOnPreparedListener(MediaPlayer::start);
                    new Timer().scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            }
                        }
                    }, 0, 10);
                } else {
                    Toast.makeText(context, "Please choose a file to play!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    mediaPlayer.start();
                }
            }
        });
        findViewById(R.id.upload).setOnClickListener(v -> {
            keyword = Objects.requireNonNull(keywords.getText()).toString().split(",");
            if (keyword.length == 0) {
                keywords1.setError("Cannot be empty!");
                return;
            }
            if (type.getText().length() == 0) {
                type1.setError("Cannot be empty!");
                return;
            }
            if (uri != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("keywords", Arrays.asList(keyword));
                map.put("media", convert());
                map.put("type", type.getText().toString());
                progressDialog.show();
                db.collection("sounds").add(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "File Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                        keywords.setText("");
                        seekBar.setProgress(0);
                        progressDialog.dismiss();
                        keywords1.setError(null);
                        type1.setError(null);
                        uri = null;
                    } else {
                        Toast.makeText(context, "File could not be uploaded!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            } else
                Toast.makeText(context, "Please choose a file to upload!", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unable to fetch files without permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                uri = data.getData();
                File file = new File(uri.getPath());
                name.setText(file.getName());
                if (file.length() > 512000) {
                    Toast.makeText(context, "File size should be less than 500 KB", Toast.LENGTH_SHORT).show();
                    uri = null;
                }
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String convert() {
        byte[] soundBytes = new byte[0];
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            soundBytes = new byte[inputStream.available()];
            soundBytes = toByteArray(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String mainString = bytesToHex(soundBytes);
        return mainString;
    }

    public byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[1024];
        while (read != -1) {
            read = in.read(buffer);
            if (read != -1)
                out.write(buffer, 0, read);
        }
        out.close();
        return out.toByteArray();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
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
}