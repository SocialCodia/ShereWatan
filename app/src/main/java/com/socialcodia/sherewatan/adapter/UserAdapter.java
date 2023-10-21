package com.socialcodia.sherewatan.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.socialcodia.sherewatan.Activity.ChatActivity;
import com.socialcodia.sherewatan.Activity.ProfileActivity;
import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.model.UserModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    List<UserModel> modelClassList;
    Context context;

    public UserAdapter(List<UserModel> modelClassList, Context context) {
        this.modelClassList = modelClassList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_user,parent,false);
        UserViewHolder userViewHolder = new UserViewHolder(view);
        return userViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.tvUserName.setText(modelClassList.get(position).getName());
        holder.tvUserEmail.setText(modelClassList.get(position).getEmail());
        final String uid = modelClassList.get(position).getUid();
        String image = modelClassList.get(position).getImage();
        try {
            Picasso.get().load(image).into(holder.userProfileImage);
        }
        catch (Exception e)
        {
            Picasso.get().load(R.drawable.person_female).into(holder.userProfileImage);
            Toast.makeText(context, "Failed To Display The Image", Toast.LENGTH_SHORT).show();
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToProfileActivity(uid);
            }
        });
    }

    private void sendToProfileActivity(String uid) {
        Intent sendToChatIntent = new Intent(context, ProfileActivity.class);
        sendToChatIntent.putExtra("hisUid",uid);
        context.startActivity(sendToChatIntent);
    }

    private void sendToChatActivity(String uid) {
        Intent sendToChatIntent = new Intent(context, ChatActivity.class);
        sendToChatIntent.putExtra("uid",uid);
        context.startActivity(sendToChatIntent);
    }

    @Override
    public int getItemCount() {
        return modelClassList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder
    {
        private TextView tvUserName, tvUserEmail;
        private ImageView userProfileImage;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            userProfileImage  = itemView.findViewById(R.id.userProfileImage);
        }
    }
}
