package com.example.evola;package com.example.evola;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassActivity extends AppCompatActivity {

    private EditText etSiteName, etUsername, etPassword;
    private Button btnAdd;
    private ListView lvPasswords;
    private List<String> passwordList;
    private ArrayAdapter<String> adapter;
    private DatabaseReference databaseReference;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);

        initializeViews();
        setupFirebase();
        setupListeners();
        loadPasswords();
    }

    private void initializeViews() {
        etSiteName = findViewById(R.id.etSiteName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnAdd = findViewById(R.id.btnAdd);
        lvPasswords = findViewById(R.id.lvPasswords);

        passwordList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, passwordList);
        lvPasswords.setAdapter(adapter);
    }

    private void setupFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userEmail = mAuth.getCurrentUser().getEmail();
            String userId = mAuth.getCurrentUser().getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("passwords").child(userId);
        } else {
            // Handle the case when the user is not logged in
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        btnAdd.setOnClickListener(v -> addPassword());

        lvPasswords.setOnItemLongClickListener((parent, view, position, id) -> {
            showPasswordOptions(position);
            return true;
        });
    }

    private void addPassword() {
        String siteName = etSiteName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (siteName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("siteName", siteName);
        passwordMap.put("username", username);
        passwordMap.put("password", password);

        databaseReference.push().setValue(passwordMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PassActivity.this, "Contraseña agregada exitosamente", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                })
                .addOnFailureListener(e -> Toast.makeText(PassActivity.this, "Error al agregar la contraseña", Toast.LENGTH_SHORT).show());
    }

    private void loadPasswords() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                passwordList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, String> passwordMap = (Map<String, String>) snapshot.getValue();
                    if (passwordMap != null) {
                        String siteName = passwordMap.get("siteName");
                        String username = passwordMap.get("username");
                        passwordList.add(siteName + " - " + username);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PassActivity.this, "Error al cargar las contraseñas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPasswordOptions(int position) {
        String[] options = {"Editar", "Eliminar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones de contraseña");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Editar
                    editPassword(position);
                    break;
                case 1: // Eliminar
                    deletePassword(position);
                    break;
            }
        });
        builder.show();
    }

    private void editPassword(int position) {
        // Implement edit functionality
        // You'll need to show a dialog or navigate to another activity to edit the password
        Toast.makeText(this, "Editar contraseña: " + passwordList.get(position), Toast.LENGTH_SHORT).show();
    }

    private void deletePassword(int position) {
        String key = getKeyAtPosition(position);
        if (key != null) {
            databaseReference.child(key).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(PassActivity.this, "Contraseña eliminada exitosamente", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(PassActivity.this, "Error al eliminar la contraseña", Toast.LENGTH_SHORT).show());
        }
    }

    private String getKeyAtPosition(int position) {
        // This method assumes that the keys in Firebase are in the same order as the passwordList
        // You might need to implement a more robust solution if this assumption doesn't hold
        final String[] key = new String[1];
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (i == position) {
                        key[0] = snapshot.getKey();
                        break;
                    }
                    i++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PassActivity.this, "Error al obtener la clave", Toast.LENGTH_SHORT).show();
            }
        });
        return key[0];
    }

    private void clearInputFields() {
        etSiteName.setText("");
        etUsername.setText("");
        etPassword.setText("");
    }
}