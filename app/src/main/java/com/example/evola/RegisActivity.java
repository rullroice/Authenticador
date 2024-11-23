package com.example.evola;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisActivity extends AppCompatActivity {
    private EditText txt_emailReg, txt_passReg, txt_usernameReg, txt_passRegValid;
    private TextView txt_errorTextReg;
    private Button btn_signUpReg, btn_backToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupFirebase();
        setupListeners();
    }

    private void initializeViews() {
        txt_emailReg = findViewById(R.id.txt_servName);
        txt_passReg = findViewById(R.id.txt_passServ);
        txt_usernameReg = findViewById(R.id.txt_servDesc);
        txt_passRegValid = findViewById(R.id.txt_passServValid);
        txt_errorTextReg = findViewById(R.id.txt_errorTextEntry);
        btn_signUpReg = findViewById(R.id.btn_createServ);
        btn_backToLogin = findViewById(R.id.btn_backToLogin);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setupListeners() {
        btn_signUpReg.setOnClickListener(view -> registerUser());
        btn_backToLogin.setOnClickListener(view -> navigateToLogin());
    }

    private void registerUser() {
        String email = txt_emailReg.getText().toString().trim();
        String password = txt_passReg.getText().toString().trim();
        String confirmPassword = txt_passRegValid.getText().toString().trim();
        String username = txt_usernameReg.getText().toString().trim();

        if (!validateInputs(email, password, confirmPassword, username)) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser, username, email);
                        }
                    } else {
                        showError("Registration failed. " + task.getException().getMessage());
                    }
                });
    }

    private boolean validateInputs(String email, String password, String confirmPassword, String username) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(username)) {
            showError("All fields are required.");
            return false;
        }

        if (!isValidEmail(email)) {
            showError("Invalid email format.");
            return false;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        return pattern.matcher(email).matches();
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser, String username, String email) {
        String uid = firebaseUser.getUid();
        String salt = generateSalt(uid);
        String encryptionKey = generateKey(uid, salt);

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("salt", salt);
        user.put("encryptionKey", encryptionKey);

        db.collection("users")
                .document(uid)
                .set(user)
                .addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        Toast.makeText(RegisActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        showError("Failed to save user data.");
                    }
                });
    }

    private String generateSalt(String userUID) {
        return userUID.length() > 16 ? userUID.substring(16) + "marmalade" : userUID + "marmalade";
    }

    private String generateKey(String userUID, String salt) {
        String combined = userUID + salt;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return combined.substring(0, Math.min(combined.length(), 16));
        }
    }

    private void showError(String message) {
        txt_errorTextReg.setText(message);
        txt_errorTextReg.setVisibility(View.VISIBLE);
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}