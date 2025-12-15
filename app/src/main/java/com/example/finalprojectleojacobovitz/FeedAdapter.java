package com.example.finalprojectleojacobovitz;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private Context context;
    private List<FeedPost> feedList;
    private List<String> postKeys; // נשמור פה את ה-ID של הספר (בשביל התגובות)

    // בנאי (Constructor)
    public FeedAdapter(Context context) {
        this.context = context;
        this.feedList = new ArrayList<>();
        this.postKeys = new ArrayList<>();
    }

    // פונקציה לעדכון הנתונים מבחוץ (מה-Activity)
    public void setPosts(List<FeedPost> posts, List<String> keys) {
        this.feedList = posts;
        this.postKeys = keys;
        notifyDataSetChanged(); // מרענן את המסך
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // כאן אנחנו מחברים את קובץ ה-XML שיצרנו (item_feed)
        View view = LayoutInflater.from(context).inflate(R.layout.item_feedpost , parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        // שליפת הנתונים של הפוסט הנוכחי
        FeedPost currentPost = feedList.get(position);
        String currentBookId = postKeys.get(position);

        // הצגת הנתונים על המסך
        holder.tvUserName.setText(currentPost.userName);
        holder.tvDate.setText(currentPost.postDate);
        holder.tvBookName.setText(currentPost.bookName);
        holder.tvAuthorName.setText(currentPost.authorName);
        holder.tvPostContent.setText(currentPost.postContent);

        // --- הוספת הטיפול בדירוג ---
        if (currentPost.rating != null && !currentPost.rating.isEmpty()) {
            try {
                // המרת ה-String למספר כדי שהכוכבים יופיעו
                float ratingValue = Float.parseFloat(currentPost.rating);
                holder.ratingBar.setRating(ratingValue);
            } catch (NumberFormatException e) {
                holder.ratingBar.setRating(0); // אם יש שגיאה בנתונים
            }
        } else {
            holder.ratingBar.setRating(0);
        }

        // --- הוספת הטיפול בתמונה ---
        if (currentPost.imageBase64 != null && !currentPost.imageBase64.isEmpty()) {
            // המרה מ-Base64 ל-Bitmap
            Bitmap bookImage = decodeBase64(currentPost.imageBase64);
            holder.ivBookImage.setImageBitmap(bookImage);
            holder.ivBookImage.setVisibility(View.VISIBLE);
        } else {
            // אם אין תמונה, נסתיר את ה-ImageView
            holder.ivBookImage.setVisibility(View.GONE); // או שנשים תמונת ברירת מחדל
        }

        // טיפול בלחיצה על כפתור תגובות
        holder.btnComments.setOnClickListener(v -> {
            // כרגע רק נדפיס הודעה, בהמשך נעשה שזה יפתח מסך תגובות
            Toast.makeText(context, "פתור תגובות לספר: " + currentBookId, Toast.LENGTH_SHORT).show();
        });
    }

    // פונקציית עזר להמרה (אותה פונקציה שיש לך כנראה כבר באדפטר של הספרים)
    private Bitmap decodeBase64(String input) {
        try {
            byte[] decodedByte = Base64.decode(input, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    // המחלקה הפנימית שמחזיקה את הרכיבים מה-XML
    public static class FeedViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvDate, tvBookName, tvAuthorName, tvPostContent;
        Button btnComments;
        android.widget.RatingBar ratingBar; // <--- החדש
        android.widget.ImageView ivBookImage; // <--- חדש

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tv_post_user_name);
            tvDate = itemView.findViewById(R.id.tv_post_date);
            tvBookName = itemView.findViewById(R.id.tv_post_book_name);
            tvAuthorName = itemView.findViewById(R.id.tv_post_author);
            tvPostContent = itemView.findViewById(R.id.tv_post_content);
            btnComments = itemView.findViewById(R.id.btn_comments);
            ratingBar = itemView.findViewById(R.id.ratingBar_post); // <--- החדש
            ivBookImage = itemView.findViewById(R.id.iv_post_image); // <--- חדש
        }
    }
}