package com.socialcodia.sherewatan.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.socialcodia.sherewatan.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        Handler handler  = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                selectLoginType();
            }
        },2500);

    }



    public void sendToLogin()
    {
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void selectLoginType()
    {
        Intent intent = new Intent(getApplicationContext(),SelectLoginTypeActivity.class);
        startActivity(intent);
        finish();
    }
}
