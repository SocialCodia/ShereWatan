package com.socialcodia.sherewatan.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.Utils.Utils;

public class PhoneOtpVerificationActivity extends AppCompatActivity {

    private EditText inputOtp;
    private Button btnVerifyOtp;

    String otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_otp_verification);


        //Init
        inputOtp = findViewById(R.id.inputOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        btnVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateDataAndVerifyOtp();
            }
        });

    }

    private void ValidateDataAndVerifyOtp()
    {
        otp = inputOtp.getText().toString().trim();
        if (otp.isEmpty())
        {
            inputOtp.setError("Enter Otp");
            inputOtp.requestFocus();
        }
        else if (otp.length()<5)
        {
            inputOtp.setError("Enter a valid otp");
            inputOtp.requestFocus();
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
            verifyOtp(otp);
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Please check your internet connection",Toast.LENGTH_SHORT).show();
        }
    }

    private void verifyOtp(String otp)
    {
    }
}
