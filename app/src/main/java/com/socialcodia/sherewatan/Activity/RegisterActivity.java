package com.socialcodia.sherewatan.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.storage.Constants;
import com.socialcodia.sherewatan.Utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputName, inputEmail, inputPassword, inputConfirmPassword;
    private TextView tvLogin;
    private Button btnRegister;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    FirebaseUser mUser;


    String name, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("Users");
        mUser = mAuth.getCurrentUser();


        //if user not null means user already.
        if (mUser!=null)
        {
            sendUserToHome();
        }


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDataAndRegister();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLogin();
            }
        });


    }

    private void checkDataAndRegister()
    {
        name = inputName.getText().toString().trim();
        email = inputEmail.getText().toString().trim();
        password = inputPassword.getText().toString().trim();
        String confirmPassword = inputConfirmPassword.getText().toString().trim();
        if (name.isEmpty())
        {
            inputName.setError("Enter Your Name");
            inputName.requestFocus();
        }
        else if (inputName.length()<3)
        {
            inputName.setError("Name should be greater than 3 character");
            inputName.requestFocus();
        }
        else if (email.isEmpty())
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
            inputPassword.setError("Password too short");
            inputPassword.requestFocus();
        }
        else if (confirmPassword.isEmpty())
        {
            inputConfirmPassword.setError("Enter Confirm Password");
            inputConfirmPassword.requestFocus();
        }
        else if (confirmPassword.length()<7)
        {
            inputConfirmPassword.setError("Password too short");
            inputConfirmPassword.requestFocus();
        }
        else if (!password.equals(confirmPassword))
        {
            inputPassword.setError("Password not matched");
            inputPassword.requestFocus();
            inputConfirmPassword.setError("Password not matched");
            inputConfirmPassword.requestFocus();
            inputPassword.setText("");
            inputConfirmPassword.setText("");
        }
        else
        {
            isNetworkAvailable();
        }
    }

    private void doRegister(final String name, final String email, String password)
    {
        btnRegister.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            sendDataToRealTimeDB(name,email);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthUserCollisionException)
                        {
                            btnRegister.setEnabled(true);
                            inputEmail.setError("Email Already Registered");
                            inputEmail.requestFocus();
                        }
                    }
                });
    }

    private void sendDataToRealTimeDB(final String name, final String email)
    {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String,Object> map = new HashMap<>();
                map.put(Constants.USER_NAME,name);
                map.put(Constants.USER_EMAIL,email);
                map.put(Constants.USER_BIO,"SocialCodia");
                map.put(Constants.USER_IMAGE,"");
                map.put(Constants.USER_ONLINE_STATUS,"online");
                map.put(Constants.USER_TYPING_STATUS,"noOne");
                map.put(Constants.LOGIN_STATE,0);
                map.put(Constants.USER_CONTACT,null);
                map.put(Constants.USER_ID,mAuth.getCurrentUser().getUid());
                mRef.child(mAuth.getCurrentUser().getUid()).updateChildren(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    btnRegister.setEnabled(true);
                                    sendEmailVerification();
                                }
                                else
                                {
                                    btnRegister.setEnabled(true);
                                    Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Oops! Something went wrong.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendEmailVerification ()
    {
        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(RegisterActivity.this, "Email verification link has been send to your email address", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    sendToLoginWithData();
                }
                else
                {
                    Toast.makeText(RegisterActivity.this, "Oops! Failed to send email verification link", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void isNetworkAvailable()
    {
        boolean isNetworkAvailable = Utils.isNetworkAvailable(getApplicationContext());
        if (isNetworkAvailable)
        {
            doRegister(name,email,password);
        }
        else
        {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendUserToLogin()
    {
        Intent sendUserToLoginIntent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(sendUserToLoginIntent);
    }

    private void sendToLoginWithData()
    {
        Intent sendToLoginWithDataIntent = new Intent(getApplicationContext(),LoginActivity.class);
        sendToLoginWithDataIntent.putExtra("email",email);
        sendToLoginWithDataIntent.putExtra("password",password);
        startActivity(sendToLoginWithDataIntent);
        finish();
    }

    private void sendUserToHome()
    {
        Intent sendUserToHomeIntent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(sendUserToHomeIntent);
        finish();
    }
}
