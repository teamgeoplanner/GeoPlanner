package com.example.geoplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText uName, emailID, pass;
    Button btnRegister;
    ImageView backButton;
    TextView login;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        backButton = findViewById(R.id.btnBack);
        login = findViewById(R.id.lnkLogin);
        uName = findViewById(R.id.txtName);
        emailID = findViewById(R.id.txtID);
        pass = findViewById(R.id.txtPass);
        btnRegister = findViewById(R.id.btnSignup);

        // get current instance(state) of Firebase
        fAuth = FirebaseAuth.getInstance();


        // code to executed on clicking Sign Up button

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = uName.getText().toString().trim();
                final String email = emailID.getText().toString().trim();
                String password = pass.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    emailID.setError("Email ID Required!");
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailID.setError("Enter valid Email ID!");
                }

                if(TextUtils.isEmpty(password)){
                    pass.setError("Password Required!");
                    return;
                }

                if(password.length() < 8){
                    pass.setError("Minimum 8 characters required!");
                    return;
                }

//                {
//                    "rules": {
//                    ".read": "now < 1609871400000",  // 2021-1-6
//                            ".write": "now < 1609871400000",  // 2021-1-6
//                }
//                }

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            User user = new User(name, email);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        fAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(RegisterActivity.this, "Account Created. Check E-mail for Verification Link", Toast.LENGTH_SHORT).show();
                                                    uName.setText("");
                                                    emailID.setText("");
                                                    pass.setText("");
                                                    //startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
                                                    FirebaseAuth.getInstance().signOut();
                                                    //startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                                                    finish();
                                                }else{
                                                    Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else{
                                        Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }

                    }
                });
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


    }
}