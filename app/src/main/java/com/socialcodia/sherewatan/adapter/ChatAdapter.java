package com.socialcodia.sherewatan.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.model.ChatModel;
import com.socialcodia.sherewatan.storage.Constants;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    List<ChatModel> modelClassList;
    Context context;
    private static  final int MSG_TYPE_RIGHT = 0;
    private static final int MSG_TYPE_LEFT = 1;

    FirebaseUser firebaseUser;
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    public ChatAdapter(List<ChatModel> modelClassList, Context context) {
        this.modelClassList = modelClassList;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==MSG_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_right,parent,false);
            ChatViewHolder viewHolder = new ChatViewHolder(view);
            return viewHolder;
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_left,parent,false);
            ChatViewHolder viewHolder = new ChatViewHolder(view);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, final int position) {
//        firebaseUser = FirebaseAuth.get
        final String msg = modelClassList.get(position).getMsg();
        String timestamp = modelClassList.get(position).getTimestamp();
        String chatImage = modelClassList.get(position).getImage();
        Integer chat_status = modelClassList.get(position).getChat_status();
        if (chat_status==1)
        {
            if (!modelClassList.get(position).getType().equals("image"))
            {
                holder.tvChatMessage.setText(msg);
                holder.ivChatImage.setVisibility(View.GONE);
                holder.tvChatMessage.setVisibility(View.VISIBLE);
            }
            else
            {
                try {
                    Picasso.get().load(chatImage).into(holder.ivChatImage);
                }
                catch (Exception e)
                {
                    Toast.makeText(context, "Oops! Failed to load the image from the database", Toast.LENGTH_SHORT).show();
                    Picasso.get().load(R.drawable.person_male).into(holder.ivChatImage);
                }
                holder.ivChatImage.setVisibility(View.VISIBLE);
                holder.tvChatMessage.setVisibility(View.GONE);
            }

            holder.tvChatTime.setText(getTime(timestamp));
        }
        else
        {
            if (modelClassList.get(position).getFromUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            {

                    String deletedString = " You deleted this message";
                    SpannableString spanString = new SpannableString(deletedString);
                    spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
                    holder.tvChatMessage.setText(spanString);
                    holder.ivChatImage.setVisibility(View.GONE);
                    holder.tvChatMessage.setVisibility(View.VISIBLE);


            }
            else
            {
                final String deletedString = " This message was deleted";
                SpannableString spanString = new SpannableString(deletedString);
                spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
                holder.tvChatMessage.setText(spanString);
                holder.ivChatImage.setVisibility(View.GONE);
                holder.tvChatMessage.setVisibility(View.VISIBLE);
            }

            holder.tvChatTime.setText(getTime(timestamp));
        }

        holder.chat_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message");
                //delete button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });
                //cancel button
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    private void deleteMessage(int position)
    {
        DatabaseReference deleteRef = FirebaseDatabase.getInstance().getReference("Chats");
        String msgId = modelClassList.get(position).getMid();
        Query delQuery = deleteRef.orderByChild(Constants.MESSAGE_ID).equalTo(msgId);
        delQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser mUser = mAuth.getCurrentUser();
                    String uid = mUser.getUid();
                    String fromUid = ds.child("fromUid").getValue(String.class);
                    if (fromUid.equals(uid))
                    {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put(Constants.CHAT_STATUS,0);
                        ds.getRef().updateChildren(map);
                    }
                    else
                    {
                        Toast.makeText(context, "you can delete only your own messages", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getTime(String timestamp)
    {
        Long ts = Long.valueOf(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:a");
        String time = sdf.format(new Date(ts));
        return time;
    }


    @Override
    public int getItemCount() {
        return modelClassList.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (modelClassList.get(position).getFromUid().equals(firebaseUser.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return MSG_TYPE_LEFT;
        }
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder
    {

        private TextView tvChatMessage, tvChatTime;
        private LinearLayout chat_right;
        private ImageView ivChatImage;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatMessage = itemView.findViewById(R.id.tvChatMessage);
            tvChatTime = itemView.findViewById(R.id.tvChatTime);
            chat_right = itemView.findViewById(R.id.chat_layout);
            ivChatImage = itemView.findViewById(R.id.ivChatImage);
        }
    }
}