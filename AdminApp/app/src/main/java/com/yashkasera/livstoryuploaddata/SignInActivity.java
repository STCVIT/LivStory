package com.yashkasera.livstoryuploaddata;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yashkasera.livstoryuploaddata.R;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private TextInputEditText email, password;
    private TextInputLayout emailError, passwordError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        emailError = findViewById(R.id.email1);
        passwordError = findViewById(R.id.password1);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing In");
        findViewById(R.id.login).setOnClickListener(v -> {
            if (email.getText().toString().length() == 0) {
                emailError.setError("Invalid Email!");
            } else if (password.getText().toString().length() == 0) {
                passwordError.setError("Invalid Email!");
            } else {
                progressDialog.show();
                mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(this, task -> {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Authentication failed.",
                                        BaseTransientBottomBar.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}

