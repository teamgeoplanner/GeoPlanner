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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText uName, emailID, pass, confpass;
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
        confpass = findViewById(R.id.txtConfirmPass);
        btnRegister = findViewById(R.id.btnSignup);

        // get current instance(state) of Firebase
        fAuth = FirebaseAuth.getInstance();


        //Click event of Register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = uName.getText().toString().trim();
                final String email = emailID.getText().toString().trim();
                String password = pass.getText().toString().trim();
                String confpassword = confpass.getText().toString().trim();

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

                if(!password.equals(confpassword)) {
                    confpass.setError("Password does not match!");
                    return;
                }

                //Create user account with email and password
                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser curUser = FirebaseAuth.getInstance().getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build();

                            curUser.updateProfile(profileUpdates);
                            User user = new User(name, email);      //create instance of User Class

                            //Create record using Uid created in Database from User class
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        //Send verification link to User's EmailID
                                        fAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(RegisterActivity.this, "Account Created. Check E-mail for Verification Link", Toast.LENGTH_SHORT).show();
                                                    uName.setText("");
                                                    emailID.setText("");
                                                    pass.setText("");
                                                    FirebaseAuth.getInstance().signOut();   //sign out from user account
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

        //Click event of Back Button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //Click event of Login Text
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


    }
}