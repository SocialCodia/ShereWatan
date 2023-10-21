package com.socialcodia.sherewatan.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.storage.Constants;
import com.socialcodia.sherewatan.Utils.Utils;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private TextView tvRegister, tvForgotPassword;
    private Button btnLogin;
    ProgressBar loginProgressBar;
    Intent intent;
    String intentEmail, intentPassword;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        mAuth= FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("Users");


        //if user not null means user already login.
        if (mUser!=null)
        {
            sendUserToHome();
        }



        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateDataAndLogin();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegister();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                sendToForgotPassword();
            }
        });

        setIntentDataIntoLogin();

    }

    private void setIntentDataIntoLogin()
    {
        intent = getIntent();
        if (intent.getStringExtra("email")!=null)
        {
            intentEmail = intent.getStringExtra("email");
            inputEmail.setText(intentEmail);
        }
        if (intent.getStringExtra("password")!=null)
        {
            intentPassword = intent.getStringExtra("password");
            inputPassword.setText(intentPassword);
        }


    }

    private void validateDataAndLogin()
    {
        email = inputEmail.getText().toString().trim();
        password =inputPassword.getText().toString().trim();
        if (email.isEmpty())
        {
            inputEmail.setError("Enter Email Address");
            inputEmail.requestFocus();
        }
        else if (email.length()<10)
        {
            inputEmail.setError("Enter A Valid Email");
            inputEmail.requestFocus();
        }
        else if (password.isEmpty())
        {
            inputPassword.setError("Enter Password");
            inputPassword.requestFocus();
        }
        else if (password.length()<7)
        {
            inputPassword.setError("Enter Your Password");
            inputPassword.requestFocus();
        }
        else
        {
            isNetworkAvailable();
        }
    }

    private void doLogin(String email, String password)
    {
        btnLogin.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            btnLogin.setEnabled(true);
                            String uid = mAuth.getCurrentUser().getUid();
                            isEmailVerified();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException)
                        {
                            btnLogin.setEnabled(true);
                            inputPassword.setError("Invalid Password");
                            inputPassword.requestFocus();
                        }
                        else if (e instanceof FirebaseAuthInvalidUserException)
                        {
                            btnLogin.setEnabled(true);
                            inputEmail.setError("Email Not Registered");
                            inputEmail.requestFocus();
                        }
                    }
                });
    }

    private void sendUserToRegister()
    {
        Intent sendUserToRegisterIntent = new Intent(getApplicationContext(),RegisterActivity.class);
        startActivity(sendUserToRegisterIntent);
    }

    private void sendUserToHome()
    {
        Intent sendUserToHomeIntent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(sendUserToHomeIntent);
        finish();
    }

    private void sendToForgotPassword()
    {
        Intent sendToForgotPassword = new Intent(getApplicationContext(),ForgotPasswordActivity.class);
        startActivity(sendToForgotPassword);
    }

    private void checkLoginState()
    {
        mRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child(Constants.USER_NAME).getValue(String.class);
                int loginState = dataSnapshot.child(Constants.LOGIN_STATE).getValue(Integer.class);
                if (loginState == 0)
                {
                    Intent SProfileIntent = new Intent(getApplicationContext(),secondStepUpdateProfileActivity.class);
                    SProfileIntent.putExtra("name",name);
                    startActivity(SProfileIntent);
                }
                else if (loginState == 1)
                {
                    sendUserToHome();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isEmailVerified()
    {
        if (mAuth.getCurrentUser()!=null)
        {
            boolean isEmailVerified = mAuth.getCurrentUser().isEmailVerified();
            if (isEmailVerified)
            {
                checkLoginState();
            }
            else
            {
                sendToEmailVerificationActivity();
            }
        }
    }

    private void sendToEmailVerificationActivity()
    {
        Intent intent = new Intent(getApplicationContext(),EmailVerificationActivity.class);
        startActivity(intent);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    private void isNetworkAvailable()
    {
        boolean isNetworkAvailable = Utils.isNetworkAvailable(getApplicationContext());
        if (isNetworkAvailable)
        {
            doLogin(email,password);
        }
        else
        {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }
    }
}
