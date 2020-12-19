package com.example.geoplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button loginButton;
    TextView register;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.btnLoginPage);
        register = findViewById(R.id.lnkRegister);

        fAuth = FirebaseAuth.getInstance();     //Get current firebase instance

        //check if any logged in or not
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
            finish();
        }

        //Click event on login button.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);      //Redirect to LoginActivity
                finish();

            }
        });

        //Click event on register.
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);      //Redirect to RegisterActivity
                finish();
            }
        });
    }
}