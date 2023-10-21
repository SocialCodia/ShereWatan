package com.socialcodia.sherewatan.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.Utils.Utils;

public class PhoneLoginActivity extends AppCompatActivity {
    private EditText inputContact;
    private Button btnPhoneLogin;
    String contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        //Init
        inputContact = findViewById(R.id.inputContact);
        btnPhoneLogin = findViewById(R.id.btnPhoneLogin);

        //On click listener at login button

        btnPhoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateDataAndSendOtp();
            }
        });
    }

    private void validateDataAndSendOtp()
    {
        contact = inputContact.getText().toString().trim();
        if (contact.isEmpty())
        {
            inputContact.setError("Enter Email Address");
            inputContact.requestFocus();
        }
        else if (inputContact.length()<10)
        {
            inputContact.setError("Enter a valid mobile number");
            inputContact.requestFocus();
        }
        else
        {
            isNetworkAvailable();
        }
    }

    private void isNetworkAvailable()
    {
        boolean isNetworkAvailable = Utils.isNetworkAvailable(getApplicationContext());
        if (isNetworkAvailable)
        {
            sendOtp(contact);
        }
        else
        {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendOtp(String contact)
    {

    }
}
