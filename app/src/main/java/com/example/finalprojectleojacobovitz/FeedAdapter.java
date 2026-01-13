package com.example.finalprojectleojacobovitz;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private Context context;
    private List<FeedPost> feedList;
    private List<String> postKeys;

    public FeedAdapter(Context context) {
        this.context = context;
        this.feedList = new ArrayList<>();
        this.postKeys = new ArrayList<>();
    }

    public void setPosts(List<FeedPost> posts, List<String> keys) {
        this.feedList = posts;
        this.postKeys = keys;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feedpost, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedPost currentPost = feedList.get(position);
        String currentBookId = postKeys.get(position);

        // הצגת הנתונים
        holder.tvUserName.setText(currentPost.userName);
        holder.tvDate.setText(currentPost.postDate);
        holder.tvBookName.setText(currentPost.bookName);
        holder.tvAuthorName.setText(currentPost.authorName);
        holder.tvPostContent.setText(currentPost.postContent);

        // הצגת הדירוג
        if (currentPost.rating != null && !currentPost.rating.isEmpty()) {
            try {
                holder.ratingBar.setRating(Float.parseFloat(currentPost.rating));
            } catch (NumberFormatException e) {
                holder.ratingBar.setRating(0);
            }
        }

        // הצגת תמונה
        if (currentPost.imageBase64 != null && !currentPost.imageBase64.isEmpty()) {
            Bitmap bookImage = decodeBase64(currentPost.imageBase64);
            if (bookImage != null) {
                holder.ivBookImage.setImageBitmap(bookImage);
                holder.ivBookImage.setVisibility(View.VISIBLE);
            } else {
                holder.ivBookImage.setVisibility(View.GONE);
            }
        } else {
            holder.ivBookImage.setVisibility(View.GONE);
        }


        DatabaseReference commentsRef = FirebaseDatabase.getInstance()
                .getReference("comments")
                .child(currentBookId);

        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount(); // ספירת התגובות

                if (count > 0) {
                    holder.btnComments.setText("תגובות (" + count + ")");
                } else {
                    holder.btnComments.setText("תגובות");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.btnComments.setText("תגובות");
            }
        });


        holder.btnComments.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentsActivity.class);
            intent.putExtra("BOOK_ID_KEY", currentBookId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    // פונקציית המרת תמונה
    private Bitmap decodeBase64(String input) {
        try {
            byte[] decodedByte = Base64.decode(input, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ה-ViewHolder
    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvDate, tvBookName, tvAuthorName, tvPostContent;
        RatingBar ratingBar;
        Button btnComments;
        ImageView ivBookImage;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_post_user_name);
            tvDate = itemView.findViewById(R.id.tv_post_date);
            tvBookName = itemView.findViewById(R.id.tv_post_book_name);
            tvAuthorName = itemView.findViewById(R.id.tv_post_author);
            tvPostContent = itemView.findViewById(R.id.tv_post_content);
            ratingBar = itemView.findViewById(R.id.ratingBar_post);
            btnComments = itemView.findViewById(R.id.btn_comments);
            ivBookImage = itemView.findViewById(R.id.iv_post_image);
        }
    }
}