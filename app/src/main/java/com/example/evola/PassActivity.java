package com.example.evola;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PassActivity extends AppCompatActivity {
    private EditText editTextPassword;
    private Button buttonChangePassword;
    private TextView textViewPasswordStatus;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);

        mAuth = FirebaseAuth.getInstance();

        editTextPassword = findViewById(R.id.editTextPassword);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        textViewPasswordStatus = findViewById(R.id.textViewPasswordStatus);

        buttonChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String newPassword = editTextPassword.getText().toString().trim();


    }
}