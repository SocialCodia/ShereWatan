package com.socialcodia.sherewatan.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.socialcodia.sherewatan.Activity.ProfileActivity;
import com.socialcodia.sherewatan.R;
import com.socialcodia.sherewatan.model.FeedModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    List<FeedModel> feedModelList;
    Context context;
    private boolean mProcessLike = false;

    public FeedAdapter(List<FeedModel> feedModelList, Context context) {
        this.feedModelList = feedModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_view,parent,false);
        FeedViewHolder feedViewHolder = new FeedViewHolder(view);
        return feedViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final FeedViewHolder holder, int position) {

        final String feedUserId = feedModelList.get(position).getUid();
        String feedUserName = feedModelList.get(position).getName();
        String feedUserImage = feedModelList.get(position).getImage();
        String feedTimestamp = feedModelList.get(position).getFeed_timestamp();
        final String feedUid = feedModelList.get(position).getFeed_id();
        final String feedContent = feedModelList.get(position).getFeed_content();
        final String feedImage = feedModelList.get(position).getFeed_image();
        final String feedId = feedModelList.get(position).getFeed_id();

        holder.feedUserName.setText(feedUserName);
        try {
            Picasso.get().load(feedUserImage).into(holder.feedUserImage);
        }
        catch (Exception e)
        {
            Toast.makeText(context, "Failed to load user profile image", Toast.LENGTH_SHORT).show();
        }
        holder.feedTimestamp.setText(getTime(Long.valueOf(feedTimestamp)));
        holder.feedContent.setText(feedContent);
        try {
            Picasso.get().load(feedImage).into(holder.feedImage);
        }
        catch (Exception e)
        {
            Toast.makeText(context, "Failed to load feed image", Toast.LENGTH_SHORT).show();
        }

        //On click listener at like button
        holder.tvLike.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                mProcessLike = true;
                final DatabaseReference mLikeRef = FirebaseDatabase.getInstance().getReference("Likes");
                mLikeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (mProcessLike)
                        {
                            if (dataSnapshot.child(feedId).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                            {
                                //Delete
                                mLikeRef.child(feedId).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .removeValue();
                                mProcessLike = false;
                            }
                            else
                            {
                                //Do Like
                                mLikeRef.child(feedId).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid(),FirebaseAuth.getInstance().getCurrentUser().getUid());
                                mProcessLike = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        // On click listener at feed more button
        holder.ivFeedMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptionForFeed(holder.ivFeedMore,feedImage,feedUid,feedUserId);
            }
        });

        holder.feedUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToProfile(feedUserId);
            }
        });

        holder.feedUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToProfile(feedUserId);
            }
        });

        //Share intent

        holder.tvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareIntent(feedContent);
            }
        });

        checkLike(holder,feedUid);

        countLike(holder,feedId);
    }

    private void sendToProfile(String feedUserId)
    {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("hisUid",feedUserId);
        context.startActivity(intent);
    }

    private void shareIntent(String feedContent) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT,"Shere Watan");
        sendIntent.putExtra(Intent.EXTRA_TEXT,feedContent);
        context.startActivity(sendIntent);
    }

    private void countLike(final FeedViewHolder holder, String feedId)
    {
        DatabaseReference mLikeCountRef = FirebaseDatabase.getInstance().getReference("Likes").child(feedId);
        mLikeCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    int counts = (int) dataSnapshot.getChildrenCount();
                    holder.tvLikesCount.setText(counts + " Likes");
                }
                else
                {
                    holder.tvLikesCount.setText(0 + " Likes");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // checking like that whatever the current login user is like any post or not

    private void checkLike(final FeedViewHolder holder,String feedId)
    {
        DatabaseReference mCheckLikeRef = FirebaseDatabase.getInstance().getReference("Likes");
        mCheckLikeRef.child(feedId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                {
                    holder.tvLike.setText(" Liked");
                    holder.tvLike.setTextColor(Color.parseColor("#B4290E"));
                }
                else
                {
                    holder.tvLike.setText(" Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Popup menu for post more option
    private void showMoreOptionForFeed(ImageView feedBtnMore, final String feedImage, final String feedUid, final String feedUserId)
    {
        PopupMenu popupMenu = new PopupMenu(context,feedBtnMore);
        popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
        popupMenu.getMenu().add(Menu.NONE,1,1,"Edit Post");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                //Delete Button Clicked
                if (id==0)
                {
                    DeleteFeedWithImage(feedImage,feedUid,feedUserId);
                }

                //Edit Button Clicked

                else if (id==1)
                {
                    Toast.makeText(context, "Edit Button Clicked", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void DeleteFeedWithImage(String feedImage, final String feedUid, final String feedUserId)
    {
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(feedUserId))
        {
            StorageReference mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(feedImage);
            mStorageRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            DeleteFeedData(feedUid,feedUserId);
                            DeleteLikesData(feedUid);
                            Toast.makeText(context, "Feed has been deleted", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else
        {
            Toast.makeText(context, "You can delete only your own post", Toast.LENGTH_SHORT).show();
        }
    }

    private void DeleteLikesData(String feedUid)
    {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Likes").child(feedUid);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DeleteFeedData(String feedUid, String feedUserId)
    {
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(feedUserId))
        {
            final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Feeds");
            Query feedDeleteQuery = mRef.orderByChild("feed_id").equalTo(feedUid);
            feedDeleteQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        ds.getRef().removeValue(); // post also has been delete
                        Toast.makeText(context, "Post has been delete.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to delete the post data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return feedModelList.size();
    }

    public class FeedViewHolder extends RecyclerView.ViewHolder
    {
        private TextView feedUserName,feedContent, feedTimestamp,tvLike, tvComment, tvShare, tvLikesCount, tvCommentsCount;
        private ImageView feedUserImage, feedImage, ivFeedMore;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);

            feedUserName = itemView.findViewById(R.id.feedUserName);
            feedUserImage = itemView.findViewById(R.id.feedUserImage);
            feedImage = itemView.findViewById(R.id.feedImage);
            feedTimestamp = itemView.findViewById(R.id.feedTimestamp);
            feedContent = itemView.findViewById(R.id.feedContent);
            tvLike = itemView.findViewById(R.id.tvLike);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvShare = itemView.findViewById(R.id.tvShare);
            ivFeedMore = itemView.findViewById(R.id.ivFeedMore);
            tvLikesCount = itemView.findViewById(R.id.tvLikesCount);
            tvCommentsCount = itemView.findViewById(R.id.tvCommentsCount);
            tvShare = itemView.findViewById(R.id.tvShare);
            tvComment = itemView.findViewById(R.id.tvComment);
        }
    }

    private String getTime(Long timestamp)
    {
        Long ts = timestamp*1000;
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:aa");
        String time = sdf.format(new Date(ts));
        return time;
    }
}
