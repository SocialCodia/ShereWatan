package com.socialcodia.sherewatan.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.socialcodia.sherewatan.R;

public class SelectLoginTypeActivity extends AppCompatActivity {

    private CardView cvLoginEmail, cvLoginPhone;

    FirebaseAuth mAuth;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_login_type);

        //Firebase Init

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if (mUser!=null)
        {
            sendToHome();
        }

        //Init
        cvLoginEmail = findViewById(R.id.cvEmailLogin);
        cvLoginPhone = findViewById(R.id.cvLoginPhone);

        cvLoginPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToPhoneLogin();
            }
        });

        cvLoginEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToEmailLogin();
            }
        });
    }

    private void sendToEmailLogin()
    {
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendToPhoneLogin()
    {
        Intent intent = new Intent(getApplicationContext(),PhoneLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendToHome()
    {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }
}
