package com.hu.mobilalk;

import static com.hu.mobilalk.NotificationHandler.NOTIFICATION_CART;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final int SPEECH_REQUEST_CODE = 888;

    private EditText email;
    private EditText password;
    private ImageButton btn_mic;

    private FirebaseAuth mAuth;

    private NotificationHandler mNotificationHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        mNotificationHandler = new NotificationHandler(this);
        mNotificationHandler.cancel(NOTIFICATION_CART);

        this.email = findViewById(R.id.login_edittext_email);
        this.password = findViewById(R.id.login_edittext_password);

        btn_mic = findViewById(R.id.login_btn_mic);
        btn_mic.setOnClickListener(v -> startSpeechToText(v));
    }

    // ENGLISH SPEECH TO TEXT
    public void startSpeechToText(View view) {
        // GET PERMISSION
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setTitle("Mikrofonhasználat")
                    .setMessage("Ha szeretnéd használni a mikrofont, engedélyezned kell azt.")
                    .setPositiveButton("Ok", (dialog, which) -> {
                        ActivityCompat.requestPermissions(
                                this,
                                new String[]{android.Manifest.permission.RECORD_AUDIO},
                                888
                        );
                        dialog.dismiss();
                    })
                    .setNegativeButton("Kihagyom", (dialog, which) -> dialog.dismiss())
                    .show();
        }
        else {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Beszélj angolul...");

            try {
                startActivityForResult(intent, SPEECH_REQUEST_CODE);
            }
            catch(ActivityNotFoundException e) {
                Toast.makeText(this, "Nem támogatott a beszédérzékelés!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(result != null && !result.isEmpty()) {
                email.setText(result.get(0));
            }
        }
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

    @Override
    protected void onResume() {
        mNotificationHandler.cancel(NOTIFICATION_CART);
        super.onResume();
    }
}