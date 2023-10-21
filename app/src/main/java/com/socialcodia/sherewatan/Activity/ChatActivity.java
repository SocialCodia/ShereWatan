package com.socialcodia.sherewatan.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.adapter.ChatAdapter;
import com.socialcodia.sherewatan.model.ChatModel;
import com.socialcodia.sherewatan.storage.Constants;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;

    private EditText inputMessage;
    private ImageView btnSendMessage,btnAttachImage;
    private TextView toolbarUserName, toolbarUserStatus;
    private ImageView toolbarUserImage,ivChatImage;
    private Uri filePath;

    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference  databaseReference;
    DatabaseReference chatDatabaseReference;
    FirebaseUser firebaseUser;

    RecyclerView chatRecyclerView;
    Intent intent;
    String toUid;
    String myUid;

    List<ChatModel> chatList;
    ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init Chat Recycler View
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        //set layout manager at chatRecyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        chatRecyclerView.setLayoutManager(layoutManager);

        //Firebase Init
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        chatDatabaseReference = firebaseDatabase.getReference("Chats");
        firebaseUser =firebaseAuth.getCurrentUser();

        //Init
        inputMessage = findViewById(R.id.inputMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        toolbarUserName = findViewById(R.id.toolbarUserName);
        toolbarUserStatus = findViewById(R.id.toolbarUserStatus);
        toolbarUserImage = findViewById(R.id.toolbarUserImage);
        btnAttachImage = findViewById(R.id.btnAttachImage);
        ivChatImage = findViewById(R.id.ivChatImage);

        toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);

        intent = getIntent();

        //Data From intent

        toUid = intent.getStringExtra("uid");

        // Get user Details


        if (firebaseUser!=null)
        {
            myUid = firebaseUser.getUid();
        }
        else
        {
            sendToLogin();
        }

        //set online
        userOnline();

        getUserDetails();

        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length()==0)
                {
                    checkTypingStatus("noOne");
                }
                else
                {
                    checkTypingStatus(toUid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnAttachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //Click listener on btn send message
        btnSendMessage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                ValidateAndSendMessage();
            }
        });

        getMessage();
        
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode==RESULT_OK)
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
//                ivChatImage.setImageBitmap(bitmap);
                uploadChatImage(filePath);
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Kuch Galat Ho Gya"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void uploadChatImage(Uri filePath) {
        String ChatsImage = "sherewatan_"+System.currentTimeMillis();
        StorageReference mRef = FirebaseStorage.getInstance().getReference("ChatsImage").child(ChatsImage);
        mRef.putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String DownloadUrl = uri.toString();
                            sendImageMessage(DownloadUrl);
                        }
                    });
                }
            }
        });

    }

    private void sendImageMessage(String downloadUrl)
    {
        DatabaseReference chatRef = firebaseDatabase.getReference("Chats");
        HashMap<String, Object> map = new HashMap<>();
        map.put("image",downloadUrl);
        map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
        map.put("fromUid",myUid);
        map.put("type","image");
        map.put(Constants.CHAT_STATUS,1);
        map.put("toUid",toUid);
        map.put("mid",chatRef.push().getKey());
        chatRef.push().setValue(map);
        inputMessage.setText("");
    }

    private void sendToLogin()
    {
        Intent sendToLoginIntent = new Intent(getApplicationContext(),LoginActivity.class);
        sendToLoginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendToLoginIntent);
    }

    private void ValidateAndSendMessage()
    {
        String message = inputMessage.getText().toString().trim();
        if (message.isEmpty())
        {
            Toast.makeText(this, "Can't send empty message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            sendMessage(message);
        }
    }

    //Send message
    private void sendMessage(String message)
    {

        DatabaseReference chatRef = firebaseDatabase.getReference("Chats");
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg",message);
        map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
        map.put("fromUid",myUid);
        map.put(Constants.CHAT_STATUS,1);
        map.put("toUid",toUid);
        map.put("type","text");
        map.put("mid",chatRef.push().getKey());
        chatRef.push().setValue(map);
        inputMessage.setText("");
    }

    //Get user details
    public void getUserDetails()
    {

        Query query = databaseReference.orderByChild("uid").equalTo(toUid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String name = ds.child(Constants.USER_NAME).getValue(String.class);
                    String status = ds.child(Constants.USER_ONLINE_STATUS).getValue(String.class);
                    String typingStatus = ds.child(Constants.USER_TYPING_STATUS).getValue(String.class);
                    String image = ds.child(Constants.USER_IMAGE).getValue(String.class);

                    toolbarUserName.setText(name);

                    if (typingStatus.equals(myUid))
                    {
                        toolbarUserStatus.setText("Typing...");
                    }
                    else
                    {
                        if (status.equals("online"))
                        {
                            toolbarUserStatus.setText("Online");
                        }
                        else
                        {
                            toolbarUserStatus.setText("Last seen " +getTime(status));
                        }
                    }


                    try {
                        Picasso.get().load(image).into(toolbarUserImage);
                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.person_male).into(toolbarUserImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //User online
    private void userOnline()
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.USER_ONLINE_STATUS,"online");
        databaseReference.child(firebaseAuth.getCurrentUser().getUid()).updateChildren(map);
    }

    //Get message
    private void getMessage()
    {
        chatList = new ArrayList<>();
        final DatabaseReference chatRef = firebaseDatabase.getReference("Chats");
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ChatModel chatModel = ds.getValue(ChatModel.class);
                    if (chatModel.getFromUid().equals(myUid) && chatModel.getToUid().equals(toUid) ||
                    chatModel.getFromUid().equals(toUid) && chatModel.getToUid().equals(myUid))
                    {
                        chatList.add(chatModel);
                    }
                    chatAdapter = new ChatAdapter(chatList,getApplicationContext());
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public  void checkTypingStatus(String status)
    {
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.USER_TYPING_STATUS,status);
        databaseReference.child(myUid).updateChildren(map);
    }

    private String getTime(String timestamp) {
        Long ts = Long.valueOf(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:a");
        String time = sdf.format(new Date(ts));
        return time;
    }


}
