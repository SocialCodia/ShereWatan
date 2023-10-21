package com.socialcodia.sherewatan.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.storage.Constants;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFeedFragment extends Fragment {

    private EditText inputFeedContent;
    private ImageView feedImage;
    private Button btnPostFeed;
    private Uri filePath;

    String currentUserName;
    String currentUserImage;
    String feedImageName;
    //Firebase

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    FirebaseStorage mStorage;
    StorageReference mStorageRef;

    String postImageDownloadUrl;
    public AddFeedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_feed,container,false);


        //Firebase Init

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();


        //Init

        inputFeedContent = view.findViewById(R.id.inputFeedContent);
        feedImage = view.findViewById(R.id.feedImage);
        btnPostFeed = view.findViewById(R.id.btnPostFeed);

        //getting user details

        getUserDetails();

        // on click listener at btn post feed

        btnPostFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateDataAndPostFeed();
            }
        });

        feedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageToUpload();
            }
        });

        return view;
    }

    private void chooseImageToUpload()
    {
        Intent chooseImageIntent = new Intent();
        chooseImageIntent.setAction(Intent.ACTION_PICK);
        chooseImageIntent.setType("image/*");
        startActivityForResult(chooseImageIntent,1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && resultCode==RESULT_OK)
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),filePath);
                feedImage.setImageBitmap(bitmap);
            }
            catch (Exception e)
            {
                Toast.makeText(getContext(), "Oops! Failed to load the image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ValidateDataAndPostFeed()
    {
        String postContent = inputFeedContent.getText().toString().trim();

        if (postContent.isEmpty())
        {
            inputFeedContent.setError("Please Enter Some Content");
            inputFeedContent.requestFocus();
        }
        else if (postContent.length()<2)
        {
            inputFeedContent.setError("Content should be greater than 2 character");
            inputFeedContent.requestFocus();
        }
        else if (filePath==null)
        {
            Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
        }
        else
        {
            uploadImageAndPostFeed(postContent);
        }
    }

    private void uploadImageAndPostFeed(final String postContent)
    {
        String imageName = "sherewatan_"+System.currentTimeMillis()/1000;
        btnPostFeed.setEnabled(false);
        mStorageRef.child("posts").child(imageName).putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            postImageDownloadUrl = uri.toString();
                            postFeed(postContent,postImageDownloadUrl);
                        }
                    });
                }
            }
        });
    }


    //get user details
    private void getUserDetails()
    {
        mRef.child("Users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUserName = dataSnapshot.child(Constants.USER_NAME).getValue(String.class);
                currentUserImage = dataSnapshot.child(Constants.USER_IMAGE).getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //post feed to database
    private void postFeed(String postContent, String postImageDownloadUrl)
    {
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.FEED_CONTENT,postContent);
        map.put(Constants.FEED_IMAGE,postImageDownloadUrl);
        map.put(Constants.FEED_TIMESTAMP,String.valueOf(System.currentTimeMillis()/1000));
        map.put(Constants.USER_ID,mAuth.getCurrentUser().getUid());
        map.put(Constants.Feed_ID,mRef.child("Feeds").push().getKey());
        map.put(Constants.USER_NAME,currentUserName);
        map.put(Constants.USER_IMAGE,currentUserImage);
        mRef.child("Feeds").push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    btnPostFeed.setEnabled(true);
                    inputFeedContent.setText("");
                    filePath = null;
                    Toast.makeText(getContext(), "Feed has been published", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    btnPostFeed.setEnabled(true);
                    Toast.makeText(getContext(), "Oops! Failed to publish the post", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
