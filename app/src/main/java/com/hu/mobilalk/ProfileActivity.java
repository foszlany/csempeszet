package com.hu.mobilalk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // UNAUTHENTICATED USER
        if(user == null) {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
        }

        // READ EMAIL
        TextView email = findViewById(R.id.profile_email);
        email.setText(user.getEmail());

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
    }

    // MENU BAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shop_menu_profile, menu);
        return true;
    }

    // MENU STUFF
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.shop_logout_alter) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.shop_cart_alt) {
            Intent intent = new Intent(ProfileActivity.this, CartActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.shop_home_alt) {
            Intent intent = new Intent(ProfileActivity.this, ShopActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // DELETE ACCOUNT WITH CONFIRMATION
    public void delete_account(View view) {
        AlertDialog dialog = new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Profil törlése")
                .setMessage("Biztos szeretnéd törölni a profilod?\nEZ A MŰVELET VÉGLEGES!\n\n(Az adataidat továbbra is árusítani fogjuk harmadik feleknek)")
                .setPositiveButton("Igen", (d, which) -> {
                    user.delete();
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Nem", (d, which) -> {})
                .setCancelable(true)
                .create();
        dialog.show();
    }

    // UPDATE PASSWORD
    public void change_password(View view) {
        View rootView = findViewById(R.id.main);

        EditText passwordEditText = rootView.findViewById(R.id.profile_edittext_password);
        EditText verifyEditText = rootView.findViewById(R.id.profile_edittext_password_verify);

        String password_1 = passwordEditText.getText().toString();
        String password_2 = verifyEditText.getText().toString();

        // EMPTY PASSWORD
        if(password_1.isBlank()) {
            Toast.makeText(ProfileActivity.this, "Nincs jelszó megadva!", Toast.LENGTH_LONG).show();
            return;
        }

        // EMPTY PASSWORD
        if(password_2.isBlank()) {
            Toast.makeText(ProfileActivity.this, "Nincs megerősítő jelszó megadva!", Toast.LENGTH_LONG).show();
            return;
        }

        // PASSWORDS DO NOT MATCH
        if(!password_1.equals(password_2)) {
            Toast.makeText(ProfileActivity.this, "A jelszók nem egyeznek!", Toast.LENGTH_LONG).show();
            return;
        }

        // SHORT PASSWORD
        if(password_1.length() < 8) {
            Toast.makeText(ProfileActivity.this, "A jelszó túl rövid!", Toast.LENGTH_LONG).show();
            return;
        }

        // LONG PASSWORD
        if(password_1.length() > 32) {
            Toast.makeText(ProfileActivity.this, "A jelszó túl hosszú!", Toast.LENGTH_LONG).show();
            return;
        }

        user.updatePassword(password_1)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Jelszó sikeresen megváltoztatva!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ProfileActivity.this, "Sikertelen jelszóváltoztatás!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}