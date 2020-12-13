package com.example.geoplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    ImageView backButton;
    TextView register, forgotpass;
    EditText emailID, pass;
    Button btnLogin;

    Dialog dialog;

    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        backButton = findViewById(R.id.btnBack);
        register = findViewById(R.id.lnkRegister);
        forgotpass = findViewById(R.id.txtForgotPass);
        emailID = findViewById(R.id.txtID);
        pass = findViewById(R.id.txtPass);
        btnLogin = findViewById(R.id.loginBtn);

        fAuth = FirebaseAuth.getInstance();
        

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailID.getText().toString().trim();
                String password = pass.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    emailID.setError("Email ID Required!");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    pass.setError("Password Required!");
                    return;
                }

                if(password.length() < 8){
                    pass.setError("Minimum 8 characters required!");
                    return;
                }

                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            if(fAuth.getCurrentUser().isEmailVerified()) {
                                Toast.makeText(LoginActivity.this, "Login Successfull!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
                            }else{
                                Toast.makeText(LoginActivity.this, "Please verify your E-mail", Toast.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut();
                            }
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Email ID or Password Incorrect!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog = new Dialog(LoginActivity.this);
                dialog.setContentView(R.layout.forgot_pass_dialog);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
                }
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(false);

                Button btnResetPass, btnCancel;
                final EditText txtEmail;

                btnResetPass = dialog.findViewById(R.id.resetBtn);
                btnCancel = dialog.findViewById(R.id.cancelBtn);
                txtEmail = dialog.findViewById(R.id.txtID);

                btnResetPass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String fgtPassEmail = txtEmail.getText().toString();

                        //testing

                        if(TextUtils.isEmpty(fgtPassEmail)){
                            txtEmail.setError("Email ID Required!");
                            return;
                        }

                        fAuth.sendPasswordResetEmail(fgtPassEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Reset Link Sent! Please check your email", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                        dialog.dismiss();
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}