package com.socialcodia.sherewatan.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.Utils.Utils;

public class ForgotPasswordActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    private EditText inputForgotEmail;
    private Button btnForgotPassword;
    ProgressBar progressBar;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        inputForgotEmail= findViewById(R.id.inputForgotEmail);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        progressBar = findViewById(R.id.forgotPasswordProgressBar);

        progressBar.setVisibility(View.INVISIBLE);

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateEmail();
            }
        });


        //Firebase Init
        mAuth= FirebaseAuth.getInstance();
    }

    private void ValidateEmail()
    {
        email = inputForgotEmail.getText().toString().trim();
        if (email.isEmpty())
        {
            inputForgotEmail.setError("Enter Email Address");
            inputForgotEmail.requestFocus();
        }
        else if (email.length()<10)
        {
            inputForgotEmail.setError("Enter A Valid Email Address");
            inputForgotEmail.requestFocus();
        }
        else
        {
            checkNetwork(); //if network available call the sendPasswordResetEmail method from the checkNetwork method
        }
    }

    private void sendPasswordResetEmail(String email)
    {
        progressBar.setVisibility(View.VISIBLE);
        btnForgotPassword.setEnabled(false);
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    btnForgotPassword.setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this, "Password Reset Link Has Been Sent", Toast.LENGTH_SHORT).show();
                    sendToLogin();
                }
                else
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    btnForgotPassword.setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this, "Oops! Something went wrong.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendToLogin()
    {
        Intent sendToLoginIntent = new Intent(getApplicationContext(),LoginActivity.class);
        sendToLoginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendToLoginIntent);
    }

    private void checkNetwork()
    {
        if (Utils.isNetworkAvailable(getApplicationContext()))
        {
            sendPasswordResetEmail(email);
        }
        else
        {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }

    }
}
