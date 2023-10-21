package com.socialcodia.sherewatan.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.socialcodia.sherewatan.R;

public class EmailVerificationActivity extends AppCompatActivity {

    private TextView tvEmailAddress;
    private Button btnSendVerificationEmail, btnSignOut;

    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        //Init
        tvEmailAddress = findViewById(R.id.tvEmailAddress);
        btnSendVerificationEmail = findViewById(R.id.btnSendVerificationEmail);
        btnSignOut = findViewById(R.id.btnSignOut);

        //Firebase Init

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();


        mAuth.getCurrentUser().reload();

        email = mUser.getEmail();

        //set email address of currently login's user.
        tvEmailAddress.setText(email);

        //if user null send to login
        if (mUser==null)
        {
            sendToLogin();
        }

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        btnSendVerificationEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEmailVerified();
            }
        });

    }


    private void isEmailVerified()
    {
        mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (mAuth.getCurrentUser()!=null)
                {
                    boolean isEmailVerified = mAuth.getCurrentUser().isEmailVerified();
                    if (isEmailVerified)
                    {
                        Toast.makeText(getApplicationContext(), "Your email has been verified, Now you can login.", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        sendVerificationEmail();
                    }
                }
            }
        });
    }

    private void sendVerificationEmail()
    {
        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(EmailVerificationActivity.this, "Email verification link has been sent", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(EmailVerificationActivity.this, "Oops! Failed to send email verification link", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendToLoginWithEmail()
    {
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        intent.putExtra("email",email);
        startActivity(intent);
        finish();
    }

    private void sendToLogin()
    {
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void signOut()
    {
        mAuth.signOut();
        sendToLoginWithEmail();
    }
}
