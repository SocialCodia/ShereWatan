package com.socialcodia.sherewatan.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.adapter.FeedAdapter;
import com.socialcodia.sherewatan.model.FeedModel;
import com.socialcodia.sherewatan.storage.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageView userProfileImage;
    private TextView tvUserName, tvUserProfession, tvUserBio, tvFollowing,tvFollowers;
    private Button btnFollow, btnFollowing, btnMessage;
    Intent intent;
    String hisUid;
    List<FeedModel> feedModelList;

    RecyclerView recyclerView;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    ActionBar actionBar;

    boolean mProcessFollow = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userProfileImage = findViewById(R.id.userProfileImage);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserBio = findViewById(R.id.userBio);
        btnFollow = findViewById(R.id.btnFollow);
        btnMessage = findViewById(R.id.btnMessage);
        recyclerView = findViewById(R.id.profileFeedRecyclerView);
        btnFollowing = findViewById(R.id.btnFollowing);
        tvFollowing = findViewById(R.id.tvFollowings);
        tvFollowers = findViewById(R.id.tvFollowers);

        //Disable button following
        btnFollowing.setVisibility(View.GONE);

        //init

        intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        //Firebase Init

        mAuth  = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);



        if (hisUid!=null)
        {
            getUserDataById(hisUid);
        }
        else
        {
            Toast.makeText(getApplicationContext(),"His Uid is null",Toast.LENGTH_SHORT).show();
        }

        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToChat(hisUid);
            }
        });

        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProcessFollow = true;
                doFollow();
            }
        });

        btnFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doUnFollow();
            }
        });

        getFeed();

        checkFollows();

        getFollowingCounts();

        getFollowersCount();

    }

    private void doFollow()
    {
        if (mProcessFollow)
        {
            mRef = FirebaseDatabase.getInstance().getReference("Follows").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followings");
            DatabaseReference mFollowingRef = FirebaseDatabase.getInstance().getReference("Follows")
                    .child(hisUid)
                    .child("followers");
            HashMap<String,Object> map = new HashMap<>();
            map.put(hisUid,hisUid);
            mRef.updateChildren(map);
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(FirebaseAuth.getInstance().getCurrentUser().getUid(),FirebaseAuth.getInstance().getCurrentUser().getUid());
            mFollowingRef.updateChildren(hashMap);
            btnFollow.setVisibility(View.GONE);
            btnFollowing.setVisibility(View.VISIBLE);
        }
    }

    private void doUnFollow()
    {
        mRef = FirebaseDatabase.getInstance().getReference("Follows").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followings");
        DatabaseReference mFollowRef = FirebaseDatabase.getInstance().getReference("Follows").child(hisUid).child("followers");
        mRef.child(hisUid).removeValue();
        mFollowRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
        btnFollow.setVisibility(View.VISIBLE);
        btnFollowing.setVisibility(View.GONE);
    }

    private void checkFollows()
    {
        mRef = FirebaseDatabase.getInstance().getReference("Follows").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followings");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    String UidExistOrNot = ds.getValue(String.class);
                    if (UidExistOrNot.equals(hisUid))
                    {
                        btnFollow.setVisibility(View.GONE);
                        btnFollowing.setVisibility(View.VISIBLE);
                    }
//                    else
//                    {
//                        btnFollow.setVisibility(View.VISIBLE);
//                        btnFollowing.setVisibility(View.GONE);
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendToChat(String hisUid)
    {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra("uid",hisUid);
        startActivity(intent);
    }

    private void getUserDataById(String hisUid)
    {
        Query query = mRef.child("Users").orderByChild(Constants.USER_ID).equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String name = ds.child(Constants.USER_NAME).getValue(String.class);
                    String bio = ds.child(Constants.USER_BIO).getValue(String.class);
                    String image = ds.child(Constants.USER_IMAGE).getValue(String.class);

                    tvUserName.setText(name);
                    tvUserBio.setText(bio);
                    try {
                        Picasso.get().load(image).into(userProfileImage);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"Oops! Failed to load the profile image",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFeed()
    {
        feedModelList = new ArrayList<>();
        DatabaseReference feedRef = mDatabase.getReference("Feeds");
        feedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                feedModelList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    FeedModel feedModel = ds.getValue(FeedModel.class);
                    if (feedModel.getUid().equals(hisUid))
                    {
                        feedModelList.add(feedModel);
                        FeedAdapter feedAdapter = new FeedAdapter(feedModelList,getApplicationContext());
                        recyclerView.setAdapter(feedAdapter);
                        feedAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCounts()
    {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Follows").
        child(hisUid).child("followings");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                    if (dataSnapshot.exists())
                    {
                        int followingsCount = (int) dataSnapshot.getChildrenCount();
                        tvFollowing.setText(followingsCount + "\nFollowing");
                    }
                    else
                    {
                        tvFollowing.setText("0 \nFollowing");
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getFollowersCount()
    {
        DatabaseReference mFollowersRef = FirebaseDatabase.getInstance().getReference("Follows")
                .child(hisUid).child("followers");
        mFollowersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    int followersCount = (int) dataSnapshot.getChildrenCount();
                    tvFollowers.setText(followersCount + " \n Followers");
                }
                else
                {
                    tvFollowers.setText(0 + " \n Followers");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
