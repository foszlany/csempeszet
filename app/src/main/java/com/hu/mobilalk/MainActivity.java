package com.hu.mobilalk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();

    private EditText email;
    private EditText password;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        this.email = findViewById(R.id.login_edittext_email);
        this.password = findViewById(R.id.login_edittext_password);
    }

    // OPENS REGISTER ACTIVITY
    public void btn_register(View view) {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    // OPENS SHOP ACTIVITY WHEN SUCCESSFUL
    public void btn_login(View view) {
        String email = this.email.getText().toString();
        String password = this.password.getText().toString();

        // EMPTY EMAIL
        if(email.isBlank()) {
            Toast.makeText(MainActivity.this, "Nincs email megadva!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "User did not input an email address.");
            return;
        }

        // WRONG EMAIL FORMAT
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(MainActivity.this, "Helytelen email formátum!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "User input a badly formatted email address.");
            return;
        }

        // EMPTY PASSWORD
        if(password.isBlank()) {
            Toast.makeText(MainActivity.this, "Nincs jelszó megadva!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "User did not input password.");
            return;
        }


        // CORRECT INFO, ATTEMPT LOGIN
        Log.d(LOG_TAG, "User is attempting to login with well formatted info...");
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Sikeres bejelentkezés!", Toast.LENGTH_LONG).show();
                Log.d(LOG_TAG, "User logged in!");

                Intent intent = new Intent(MainActivity.this, ShopActivity.class);
                startActivity(intent);
            }
            else {
                Log.e(LOG_TAG, "User creation failed!");
                Toast.makeText(MainActivity.this, "Sikertelen bejelentkezés!", Toast.LENGTH_LONG).show();
            }
        });
    }
}