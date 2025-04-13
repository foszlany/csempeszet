package com.hu.mobilalk;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();

    private EditText email;
    private EditText password_1;
    private EditText password_2;
    private CheckBox data;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        View text_decoration = findViewById(R.id.text_decoration);
        text_decoration.startAnimation(fadeIn);

        this.email = findViewById(R.id.register_edittext_email);
        this.password_1 = findViewById(R.id.register_edittext_password);
        this.password_2 = findViewById(R.id.register_edittext_password_verify);
        this.data = findViewById(R.id.register_checkbox_data);
    }

    // ANIMATES CHECKBOX TEXT COLOR
    public void animate(View view) {
        boolean val = this.data.isChecked();
        ValueAnimator animator;

        if(val) {
            animator = ValueAnimator.ofArgb(Color.BLACK, Color.MAGENTA);
        }
        else {
            animator = ValueAnimator.ofArgb(Color.MAGENTA, Color.BLACK);
        }

        animator.setDuration(250);

        animator.addUpdateListener(animation -> ((TextView) view).setTextColor((int) animation.getAnimatedValue()));

        animator.start();
    }

    // OPENS LOGIN ACTIVITY
    public void activity_login(View view) {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
    }

    // OPENS LOGIN ACTIVITY WHEN SUCCESSFUL
    public void register_button(View view) {
        String email = this.email.getText().toString();
        String password_1 = this.password_1.getText().toString();
        String password_2 = this.password_2.getText().toString();
        boolean data = this.data.isChecked();

        // EMPTY EMAIL
        if(email.isBlank()) {
            Toast.makeText(RegisterActivity.this, "Nincs email megadva!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "User did not input an email address.");
            return;
        }

        // WRONG EMAIL FORMAT
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(RegisterActivity.this, "Helytelen email formátum!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "User input a badly formatted email address.");
            return;
        }

        // EMPTY PASSWORD
        if(password_1.isBlank()) {
            Toast.makeText(RegisterActivity.this, "Nincs jelszó megadva!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "User did not input password.");
            return;
        }

        // EMPTY PASSWORD
        if(password_2.isBlank()) {
            Toast.makeText(RegisterActivity.this, "Nincs megerősítő jelszó megadva!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "User did not input the second password.");
            return;
        }

        // PASSWORDS DO NOT MATCH
        if(!password_1.equals(password_2)) {
            Toast.makeText(RegisterActivity.this, "A jelszók nem egyeznek!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "User failed to input the same password twice.");
            return;
        }

        // SHORT PASSWORD
        if(password_1.length() < 8) {
            Toast.makeText(RegisterActivity.this, "A jelszó túl rövid!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "User input a short password.");
            return;
        }

        // LONG PASSWORD
        if(password_1.length() > 32) {
            Toast.makeText(RegisterActivity.this, "A jelszó túl hosszú!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "User input a long password.");
            return;
        }

        // DATA USAGE FALSE
        if(!data) {
            Toast.makeText(RegisterActivity.this, "El kell fogadnia az adatainak az értékesítését!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "User did not accept the data policy.");
            return;
        }


        // CORRECT INFO, ATTEMPT REGISTRATION
        Log.i(LOG_TAG, "User is attempting to register with well formatted info...");
        mAuth.createUserWithEmailAndPassword(email, password_1).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()) {
                Log.i(LOG_TAG, "User created successfully!");
                Toast.makeText(RegisterActivity.this, "Sikeres regisztráció!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(RegisterActivity.this, ShopActivity.class);
                startActivity(intent);
            }
            else {
                Log.e(LOG_TAG, "User creation failed!");
                Toast.makeText(RegisterActivity.this, "Sikertelen regisztráció!", Toast.LENGTH_LONG).show();
            }
        });
    }
}