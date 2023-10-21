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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.util.HashMap;
import java.util.Map;

public class secondStepUpdateProfileActivity extends AppCompatActivity {

    private EditText inputName, inputBio;
    private RadioGroup rbGender;
    private ImageView userProfileImage;
    private Button btnUpdateProfile;
    RadioButton radioButton;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    FirebaseStorage mStorage;
    StorageReference mStorageRef;

    Uri filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_step_update_profile);

        inputName = findViewById(R.id.inputName);
        inputBio = findViewById(R.id.inputBio);
        rbGender = findViewById(R.id.rbGender);
        userProfileImage = findViewById(R.id.userProfileImage);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);


        //Firebase Init
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("Users");

        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference("Users");

        //onClickListener at user profile image
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        Intent intent = getIntent();
        String intentGetName = intent.getStringExtra("name");
        inputName.setText(intentGetName);

        //onClickListener at update profile button
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateDataAndUpdateProfile();
            }
        });



    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setAction(intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                userProfileImage.setImageBitmap(bitmap);
            }
            catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), "Oops! Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ValidateDataAndUpdateProfile()
    {
        String name = inputName.getText().toString().trim();
        String bio = inputBio.getText().toString().trim();
        if (name.isEmpty())
        {
            inputName.setError("Enter Name");
            inputName.requestFocus();
        }
        else if (inputName.length()<3)
        {
            inputName.setError("Name Should be greater than 3 character");
            inputName.requestFocus();
        }
        else if (bio.isEmpty())
        {
            inputBio.setError("Enter Anything About You");
            inputBio.requestFocus();
        }
        else if (bio.length()<10)
        {
            inputBio.setError("Bio should be more than 10 character");
            inputBio.requestFocus();
            Toast.makeText(this, "Bio too short", Toast.LENGTH_SHORT).show();
        }
//        if (filePath==null)
//        {
//            Toast.makeText(this, "Please Select An Image", Toast.LENGTH_SHORT).show();
//        }
        else if (!radioButton.isChecked())
        {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if (filePath==null)
            {
                updateProfileDataWithoutImage(name,bio);
            }
            else
            {
                uploadImageAndUpdateProfileData(name,bio,filePath);
            }
        }

    }

    private void updateProfileDataWithoutImage(String name, String bio) {
        {

            int selectedGenderId = rbGender.getCheckedRadioButtonId();
            radioButton = findViewById(selectedGenderId);

            String gender = radioButton.getText().toString();

            Map<String,Object> map = new HashMap<>();
            map.put(Constants.USER_NAME,name);
            map.put(Constants.USER_BIO,bio);
            map.put("gender",gender);
            map.put(Constants.LOGIN_STATE,1);
            map.put(Constants.USER_IMAGE,"");
            mRef.child(mAuth.getCurrentUser().getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        btnUpdateProfile.setEnabled(true);
                        Toast.makeText(secondStepUpdateProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                    else
                    {
                        btnUpdateProfile.setEnabled(true);
                        Toast.makeText(getApplicationContext(),"Failed To Update Profile",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void updateProfileData(final String name, final String bio, String filePathDownloadUrl)
    {

        int selectedGenderId = rbGender.getCheckedRadioButtonId();
        radioButton = findViewById(selectedGenderId);

        String gender = radioButton.getText().toString();

        Map<String,Object> map = new HashMap<>();
        map.put(Constants.USER_NAME,name);
        map.put(Constants.USER_BIO,bio);
        map.put("gender",gender);
        map.put(Constants.LOGIN_STATE,1);
        map.put(Constants.USER_IMAGE,filePathDownloadUrl);
        mRef.child(mAuth.getCurrentUser().getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    btnUpdateProfile.setEnabled(true);
                    Toast.makeText(secondStepUpdateProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
                else
                {
                    btnUpdateProfile.setEnabled(true);
                    Toast.makeText(getApplicationContext(),"Failed To Update Profile",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadImageAndUpdateProfileData(final String name, final String bio, Uri filePath)
    {
        btnUpdateProfile.setEnabled(false);
        StorageReference reference = mStorageRef.child(mAuth.getCurrentUser().getUid());
        reference.putFile(filePath)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            task.getResult().getStorage().getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String filePathDownloadUrl = uri.toString();
                                            updateProfileData(name,bio,filePathDownloadUrl);
                                        }
                                    });
                        }
                        else
                        {
                            Toast.makeText(secondStepUpdateProfileActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
