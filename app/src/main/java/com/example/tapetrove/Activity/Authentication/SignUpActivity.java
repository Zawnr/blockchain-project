package com.example.tapetrove.Activity.Authentication;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tapetrove.Database.Users;
import com.example.tapetrove.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
  private EditText etEmail, etPassword, etPassConfirm, etUsername, etAddress, etTelephone;
  private Button btSignUp;
  private TextView tvSignIn;
  private String email, password, passwordConfirm, username, address, telephone;
  private FirebaseAuth mAuth;
  private FirebaseDatabase database;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);

    mAuth = FirebaseAuth.getInstance();
    database = FirebaseDatabase.getInstance();

    etUsername = findViewById(R.id.etUsername);
    etAddress = findViewById(R.id.etAddress);
    etTelephone = findViewById(R.id.etTelephone);
    etEmail = findViewById(R.id.etEmail);
    etPassword = findViewById(R.id.etPassword);
    etPassConfirm = findViewById(R.id.etPassConfirm);

    btSignUp = findViewById(R.id.btSignUp);
    tvSignIn = findViewById(R.id.tvSignIn);

    btSignUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        username = etUsername.getText().toString();
        address = etAddress.getText().toString();
        telephone = etTelephone.getText().toString();
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();
        passwordConfirm = etPassConfirm.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
          Toast.makeText(SignUpActivity.this, "Email and password must not be empty", Toast.LENGTH_SHORT).show();
          return;
        }

        if (password.equals(passwordConfirm)) {
          SignUp(username, address, telephone, email, password);
        } else {
          Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
        }
        
      }
    });

    tvSignIn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
      }
    });
  }


  private void SignUp(String username, String address, String telephone, String email, String password) {
    mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  Log.d(TAG, "createUserWithEmail:success");
                  FirebaseUser user = mAuth.getCurrentUser();
                  String userId = user.getUid();

                  Users newUser = new Users(userId, email, password, username, address, telephone);

                  database.getReference().child("users").child(user.getUid()).setValue(newUser)
                          .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                              if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Pendaftaran berhasil! Silakan masuk.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                              } else {
                                Toast.makeText(SignUpActivity.this, "Pendaftaran gagal. Silakan coba lagi.", Toast.LENGTH_LONG).show();
                              }
                            }
                          });
                } else {
                  Log.w(TAG, "createUserWithEmail:failure", task.getException());
                  Toast.makeText(SignUpActivity.this, "Pendaftaran gagal. Silakan coba lagi.", Toast.LENGTH_LONG).show();
                }
              }
            });
  }
}