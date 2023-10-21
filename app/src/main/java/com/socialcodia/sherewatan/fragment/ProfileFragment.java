package com.socialcodia.sherewatan.fragment;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.socialcodia.sherewatan.Activity.EditProfileActivity;
import com.socialcodia.sherewatan.Activity.LoginActivity;
import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.adapter.FeedAdapter;
import com.socialcodia.sherewatan.model.FeedModel;
import com.socialcodia.sherewatan.storage.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class ProfileFragment extends Fragment {

    private ImageView userProfileImage;
    private TextView tvUserName, tvUserProfession, tvUserBio,tvFollowing, tvFollowers;
    private Button btnEditProfile, btnEmail;
    private RecyclerView profileFeedRecyclerView;
    ActionBar actionBar;

    String hisUid;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    FirebaseStorage mStorage;
    StorageReference mStorageRef;
    FirebaseUser mUser;
    String user_name,user_bio,user_image,user_email;
    String user_contact;
    List<FeedModel> feedModelList;
    FeedAdapter feedAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_profile,container,false);
        //init
        userProfileImage = view.findViewById(R.id.userProfileImage);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserBio = view.findViewById(R.id.userBio);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnEmail = view.findViewById(R.id.btnEmail);
        tvFollowing = view.findViewById(R.id.tvFollowings);
        tvFollowers = view.findViewById(R.id.tvFollowers);

        //Recycler View Init
        profileFeedRecyclerView = view.findViewById(R.id.profileFeedRecyclerView);

        //set layout manager at profile feed recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        profileFeedRecyclerView.setLayoutManager(layoutManager);

        //Firebase Init
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("Users");
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        mUser = mAuth.getCurrentUser();


        btnEditProfile.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                editProfile();
            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(user_email);
            }
        });
        Intent intent = getActivity().getIntent();
        hisUid = intent.getStringExtra("hisUid");

        if (hisUid==null)
        {
            getUserData();
        }
        else
        {
            getUserDataById();
        }

        getUserData();
        getFeed();
        getFollowingCounts();
        getFollowDetails();
        setHasOptionsMenu(true);
        return view;

    }

    private void getUserDataById()
    {
        Query query = mRef.orderByChild("uid").equalTo(hisUid);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    //get data
                    user_name = ds.child(Constants.USER_NAME).getValue(String.class);
                    user_bio = ds.child(Constants.USER_BIO).getValue(String.class);
                    user_image = ds.child(Constants.USER_IMAGE).getValue(String.class);
                    user_email = ds.child(Constants.USER_EMAIL).getValue(String.class);
                    user_contact = ds.child(Constants.USER_CONTACT).getValue(String.class);

                    //set data

                    tvUserName.setText(user_name);
                    tvUserBio.setText(user_bio);
                    try {
                        Picasso.get().load(user_image).into(userProfileImage);
                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.person_female).into(userProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onCreateOptionsMenu(Menu menu , MenuInflater menuInflater)
    {
        menuInflater.inflate(R.menu.profile_menu,menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.miAboutUs:
                Toast.makeText(getContext(), "About Us", Toast.LENGTH_SHORT).show();
                break;
            case R.id.miContactUs:
                Toast.makeText(getContext(), "Contact Us", Toast.LENGTH_SHORT).show();
                break;
            case R.id.miLogout:
                doLogout();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void doLogout()
    {
        mAuth.signOut();
        sendUserToLogin();
    }

    public void sendUserToLogin()
    {
        if (mAuth.getCurrentUser()==null)
        {
            Intent sendUserToLoginIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(sendUserToLoginIntent);
            sendUserToLoginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
    }

    public void getUserData()
    {
        Query query = mRef.orderByChild("email").equalTo(mUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    //get data
                    user_name = ds.child(Constants.USER_NAME).getValue(String.class);
                    user_bio = ds.child(Constants.USER_BIO).getValue(String.class);
                    user_image = ds.child(Constants.USER_IMAGE).getValue(String.class);
                    user_email = ds.child(Constants.USER_EMAIL).getValue(String.class);
                    user_contact = ds.child(Constants.USER_CONTACT).getValue(String.class);

                    //set data

                    tvUserName.setText(user_name);
                    tvUserBio.setText(user_bio);
                    try {
                        Picasso.get().load(user_image).into(userProfileImage);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(getContext(), "No profile image", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Oops! Somethings went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void editProfile()
    {
        Intent editProfileIntent = new Intent(getContext(), EditProfileActivity.class);
        editProfileIntent.putExtra("name",user_name);
        editProfileIntent.putExtra("bio",user_bio);
        editProfileIntent.putExtra("image",user_image);
        editProfileIntent.putExtra("email",user_email);
        editProfileIntent.putExtra("contact",user_contact);
        startActivity(editProfileIntent);
    }

    public void sendEmail(String toEmail)
    {
        Intent sendEmailIntent = new Intent(Intent.ACTION_SEND);
        sendEmailIntent.setData(Uri.parse("mailto"));
        sendEmailIntent.putExtra(Intent.EXTRA_EMAIL,toEmail);
        sendEmailIntent.putExtra(Intent.EXTRA_SUBJECT,"Comming From Android Application Shere Watab");
        sendEmailIntent.putExtra(Intent.EXTRA_TEXT,"Extra Data Extra Data Extra Data Extra Data Extra Data Extra Data Extra Data Extra Data Extra Data Extra Data Extra Data Extra Data Extra Data Extra Data Extra Data Extra Data ");
        startActivity(sendEmailIntent);
    }

    private void getFeed()
    {
        feedModelList = new ArrayList<>();
        DatabaseReference postRef =  mDatabase.getReference("Feeds");
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    FeedModel feedModel = ds.getValue(FeedModel.class);
                    if (feedModel.getUid().equals(mAuth.getCurrentUser().getUid()))
                    {
                        feedModelList.add(feedModel);
                        feedAdapter = new FeedAdapter(feedModelList,getContext());
                        feedAdapter.notifyDataSetChanged();
                        profileFeedRecyclerView.setAdapter(feedAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Oops! Failed to load the post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFollowingCounts()
    {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Follows")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followings");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        int followingsCount = (int) dataSnapshot.getChildrenCount();
                        tvFollowing.setText(followingsCount + "\nFollowing");
                    }
                    else
                    {
                        tvFollowing.setText("0 \n Following");
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowDetails()
    {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Follows").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followers");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    int followCount = (int) dataSnapshot.getChildrenCount();
                    tvFollowers.setText(followCount + "\n Followers");
                }
                else
                {
                    tvFollowers.setText(0+  "\n Followers");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
