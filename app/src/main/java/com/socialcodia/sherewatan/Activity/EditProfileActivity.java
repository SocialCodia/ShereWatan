package com.socialcodia.sherewatan.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.storage.Constants;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private EditText inputName, inputEmail, inputBio, inputContact;
    private ImageView userProfileImage;
    private Button btnUpdateProfile;
    private Uri filePath;

    //Firebase

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    FirebaseStorage mStorage;
    StorageReference mStorageRef;
    String intentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Init
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputBio = findViewById(R.id.inputBio);
        inputContact =findViewById(R.id.inputContact);
        userProfileImage = findViewById(R.id.userProfileImage);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);

//        Firebase Init

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef =mDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        //getting data
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String bio = intent.getStringExtra("bio");
        String image = intent.getStringExtra("image");
        intentImage = intent.getStringExtra("image");
        String email = intent.getStringExtra("email");
        String contact = intent.getStringExtra("contact");
        inputName.setText(name);
        inputBio.setText(bio);
        inputEmail.setText(email);
        if (contact!=null)
        {
            inputContact.setText(String.valueOf(contact));
        }
        try {
            Picasso.get().load(image).into(userProfileImage);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Failed to load the image", Toast.LENGTH_SHORT).show();
        }

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateDataAndUpdate();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageToUpload();
            }
        });

    }

    private void chooseImageToUpload()
    {
        Intent chooseImageIntent = new Intent();
        chooseImageIntent.setAction(Intent.ACTION_PICK);
        chooseImageIntent.setType("image/*");
        startActivityForResult(chooseImageIntent,1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && resultCode==RESULT_OK)
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                userProfileImage.setImageBitmap(bitmap);
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Oops! Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Contact Number is : 7715848047

    private void ValidateDataAndUpdate()
    {
        String userName = inputName.getText().toString().trim();
        String userEmail = inputEmail.getText().toString().trim();
        String userBio = inputBio.getText().toString().trim();
        String userContact = inputContact.getText().toString().trim();
        if (userName.isEmpty())
        {
            inputName.setError("Enter Name");
            inputName.requestFocus();
        }
        else if (userName.length()<3)
        {
            inputName.setError("Name should be greater than 3 character");
            inputName.requestFocus();
        }
        else if (userEmail.isEmpty())
        {
            inputEmail.setError("Enter email address");
            inputEmail.requestFocus();
        }
        else if (userEmail.length()<10)
        {
            inputEmail.setError("Enter a valid email");
            inputEmail.requestFocus();
        }
        else if (userBio.isEmpty())
        {
            inputBio.setError("Enter Bio");
            inputBio.requestFocus();
        }
        else if (userBio.length()<10)
        {
            inputBio.setError("Bio should be greater than 10 character");
            inputBio.requestFocus();
        }
        else if (userContact.isEmpty())
        {
            inputContact.setError("Enter mobile number");
            inputContact.requestFocus();
        }
        else  if (userContact.length()<10)
        {
            inputContact.setError("Enter a valid mobile number");
            inputContact.requestFocus();
        }
        else  if (filePath==null)
        {
            updateProfile(userName,userEmail,userContact,userBio,intentImage);
        }
        else
        {
            uploadImageAndUpdateData(userName,userEmail,userContact,userBio,filePath);
        }

    }

    private void uploadImageAndUpdateData(final String userName, final String userEmail, final String userContact, final String userBio, Uri filePath)
    {
        mStorageRef.child("Users").child(mAuth.getCurrentUser().getUid()).putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String filePathDownloadUrl = uri.toString();
                            updateProfile(userName,userEmail,userContact,userBio,filePathDownloadUrl);
                        }
                    });
                }
            }
        });
    }

    private void updateProfile(String userName, String userEmail, String userContact, String userBio, String filePathDownloadUrl) {
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.USER_NAME,userName);
        map.put(Constants.USER_EMAIL,userEmail);
        map.put(Constants.USER_CONTACT,userContact);
        map.put(Constants.USER_BIO,userBio);
        map.put(Constants.USER_IMAGE,filePathDownloadUrl);
        mRef.child("Users").child(mAuth.getCurrentUser().getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(EditProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(EditProfileActivity.this, "Failed To Update the profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
