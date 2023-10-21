package com.socialcodia.sherewatan.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.fragment.AddFeedFragment;
import com.socialcodia.sherewatan.fragment.HomeFragment;
import com.socialcodia.sherewatan.fragment.NotificationFragment;
import com.socialcodia.sherewatan.fragment.ProfileFragment;
import com.socialcodia.sherewatan.fragment.SearchFragment;
import com.socialcodia.sherewatan.storage.Constants;
import com.socialcodia.sherewatan.Utils.Utils;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    FirebaseUser mUser ;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        //Firebase Init
                mAuth = FirebaseAuth.getInstance();
                mDatabase = FirebaseDatabase.getInstance();
                mRef = mDatabase.getReference("Users");
                mUser = mAuth.getCurrentUser();
        if (mUser!=null)
        {
            String userId = mUser.getUid();
        }
        else
        {
            sendToLogin();
        }

        //Default fragment
        DefaultHomeFragment();
        //Create login state
//        createLoginState();
        //check login state
//        checkLoginState();

        //Check Network Connection
        checkNetwork();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                switch (menuItem.getItemId())
                {
                    case R.id.miHome:
                        actionBar = getSupportActionBar();
                        actionBar.setTitle("Home");
                        fragment = new HomeFragment();
                        break;
                    case R.id.miProfile:
                        actionBar = getSupportActionBar();
                        actionBar.setTitle("Profile");
                        Toast.makeText(MainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                        fragment = new ProfileFragment();
                        break;
                    case R.id.miSearch:
                        actionBar = getSupportActionBar();
                        actionBar.setTitle("Search User");
                        fragment = new SearchFragment();
                        break;
                    case R.id.miAdd:
                        actionBar = getSupportActionBar();
                        actionBar.setTitle("Add Post");
                        fragment = new AddFeedFragment();
                        break;
                    case R.id.miNotification:
                        actionBar = getSupportActionBar();
                        actionBar.setTitle("Notification");
                        fragment = new NotificationFragment();
                        break;
                    }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment).commit();
                return true;
            }
        });
    }
    public void DefaultHomeFragment()
    {
        actionBar = getSupportActionBar();
        actionBar.setTitle("Home");
        Fragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.fragment_container,fragment1);
        ft1.commit();
    }

//    //Creating Login State With Bio,
//    private void createLoginState()
//    {
//        mRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String bio = dataSnapshot.child(Constants.USER_BIO).getValue(String.class);
//                if (bio.equals("SocialCodia"))
//                {
//                    mRef.child(mAuth.getCurrentUser().getUid()).child(Constants.LOGIN_STATE).setValue(0);
//                }
//                else
//                {
//                    mRef.child(mAuth.getCurrentUser().getUid()).child(Constants.LOGIN_STATE).setValue(1);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    //Checking Login State

    private void sendToLogin()
    {
        Intent sendToLoginIntent = new Intent(getApplicationContext(),LoginActivity.class);
        sendToLoginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendToLoginIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        String online = "online";
        setOnlineStatus(online);
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        String offline = String.valueOf(System.currentTimeMillis()/1000);
//        setOnlineStatus(offline);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String offline = String.valueOf(System.currentTimeMillis()/1000);
        setOnlineStatus(offline);
    }

    private void setOnlineStatus(String onlineStatus)
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.USER_ONLINE_STATUS,onlineStatus);
        mRef.child(mAuth.getCurrentUser().getUid()).updateChildren(map);
    }

    private void checkNetwork()
    {
        if (Utils.isNetworkAvailable(getApplicationContext()))
        {

        }
        else
        {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }

    }


}
