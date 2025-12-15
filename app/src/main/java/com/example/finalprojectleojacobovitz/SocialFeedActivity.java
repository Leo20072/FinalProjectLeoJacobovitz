package com.example.finalprojectleojacobovitz;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SocialFeedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FeedAdapter feedAdapter;
    private ImageView btnBack;
    private DatabaseReference allPostsRef;
    private ValueEventListener feedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_feed);

        // 1. אתחול רכיבים
        btnBack = findViewById(R.id.btn_back_feed);
        recyclerView = findViewById(R.id.recyclerView_feed);

        // הגדרת ה-RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // יצירת האדפטר וחיבורו
        feedAdapter = new FeedAdapter(this);
        recyclerView.setAdapter(feedAdapter);

        // כפתור חזרה
        btnBack.setOnClickListener(v -> finish());

        // 2. הפעלת הטעינה מפיירבייס
        loadPostsFromFirebase();
    }

    private void loadPostsFromFirebase() {
        // מצביע לענף all_posts
        allPostsRef = FirebaseDatabase.getInstance().getReference("all_posts");

        feedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FeedPost> postsList = new ArrayList<>();
                List<String> keysList = new ArrayList<>();

                // מעבר על כל הפוסטים שיש בשרת
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    FeedPost post = postSnapshot.getValue(FeedPost.class);
                    String key = postSnapshot.getKey(); // זה ה-BookID

                    if (post != null) {
                        postsList.add(post);
                        keysList.add(key);
                    }
                }

                // אופציונלי: להפוך את הרשימה כדי לראות את החדשים למעלה
                Collections.reverse(postsList);
                Collections.reverse(keysList);

                // עדכון האדפטר
                feedAdapter.setPosts(postsList, keysList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SocialFeedActivity.this, "שגיאה בטעינת פיד", Toast.LENGTH_SHORT).show();
            }
        };

        // הוספת המאזין
        allPostsRef.addValueEventListener(feedListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // הסרת המאזין ביציאה כדי לחסוך סוללה וזיכרון
        if (allPostsRef != null && feedListener != null) {
            allPostsRef.removeEventListener(feedListener);
        }
    }
}