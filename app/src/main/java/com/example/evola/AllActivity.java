package com.example.evola;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.evola.Model.Password;
import com.example.evola.Model.PasswordAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AllActivity extends AppCompatActivity {
    private ListView listView;
    private TextView textView;
    private String userEmail;
    private Button btnNuevo;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        listView = findViewById(R.id.listView);
        textView = findViewById(R.id.textView);
        btnNuevo = findViewById(R.id.btnNuevo);
        userEmail = getIntent().getStringExtra("email");
        if (userEmail != null) {
            fetchUsernameForEmail(userEmail);
        } else {
            textView.setText("Error desconocido, intente más tarde");
        }

        fetchPasswordsForUser(userEmail);

        btnNuevo.setOnClickListener(view -> {
            Intent intent = new Intent(AllActivity.this, CreateEntryActivity.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
            finish();
        });
    }

    private void fetchPasswordsForUser(String email) {
        db.collection("passwords")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            ArrayList<Password> passwordsList = new ArrayList<>();

                            for (DocumentSnapshot document : querySnapshot) {
                                String serviceName = document.getString("service");
                                String userAssignedName = document.getString("name");

                                if (serviceName != null && userAssignedName != null) {
                                    passwordsList.add(new Password(serviceName, userAssignedName));
                                }
                            }

                            PasswordAdapter adapter = new PasswordAdapter(AllActivity.this, passwordsList);
                            listView.setAdapter(adapter);

                            listView.setOnItemClickListener((parent, view, position, id) -> {
                                Password selectedPassword = passwordsList.get(position);

                                Intent intent = new Intent(AllActivity.this, PasswordActivity.class);

                                intent.putExtra("service_name", selectedPassword.getServiceName());
                                intent.putExtra("user_assigned_name", selectedPassword.getUserAssignedName());
                                intent.putExtra("email", email);

                                startActivity(intent);
                            });
                        } else {
                            Toast.makeText(this, "No se encontraron contraseñas para este correo", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error al obtener las contraseñas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUsernameForEmail(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String username = document.getString("username");

                        if (username != null) {
                            textView.setText("Bienvenido " + username);
                        } else {
                            textView.setText("Bienvenido (nombre de usuario no encontrado)");
                        }
                    } else {
                        textView.setText("No se encontró usuario para este correo");
                    }
                })
                .addOnFailureListener(e -> {
                    textView.setText("Error al recuperar el nombre de usuario");
                    Toast.makeText(AllActivity.this, "Error al obtener el nombre de usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}