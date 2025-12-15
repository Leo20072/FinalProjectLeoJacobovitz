package com.example.finalprojectleojacobovitz;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RateAndPostActivity extends AppCompatActivity {

    private TextView tvBookTitle, tvBookAuthor;
    private RatingBar ratingBar;
    private TextInputEditText etPostContent;
    private Button btnPublish, btnSkip;

    // משתנים לשמירת המידע שהגיע מהמסך הקודם
    private String bookId, bookName, authorName;
    private String bookImageBase64; // משתנה לשמירת התמונה

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_and_post);

        // 1. אתחול רכיבים
        tvBookTitle = findViewById(R.id.tv_book_title_header);
        tvBookAuthor = findViewById(R.id.tv_book_author_header);
        ratingBar = findViewById(R.id.ratingBar_input);
        etPostContent = findViewById(R.id.et_post_content);
        btnPublish = findViewById(R.id.btn_publish_post);
        btnSkip = findViewById(R.id.btn_skip);

        // 2. קבלת הנתונים מה-Intent (חובה לשלוח אותם כשקוראים ל-Activity הזה)
        if (getIntent() != null) {
            bookId = getIntent().getStringExtra("BOOK_ID");
            bookName = getIntent().getStringExtra("BOOK_NAME");
            authorName = getIntent().getStringExtra("BOOK_AUTHOR");
            bookImageBase64 = getIntent().getStringExtra("BOOK_IMAGE");

            tvBookTitle.setText(bookName);
            tvBookAuthor.setText(authorName);
        }

        // 3. כפתור פרסום
        btnPublish.setOnClickListener(v -> {
            submitPost();
        });

        // 4. כפתור דילוג
        btnSkip.setOnClickListener(v -> {
            finish(); // סגירת המסך
        });
    }

    private void submitPost() {
        // בדיקת תקינות בסיסית
        float ratingValue = ratingBar.getRating();
        String content = etPostContent.getText().toString().trim();

        if (ratingValue == 0) {
            Toast.makeText(this, "אנא דרג את הספר לפני הפרסום ⭐", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "אנא כתוב כמה מילים על הספר ✍️", Toast.LENGTH_SHORT).show();
            return;
        }

        // המרה ל-String כפי שביקשת (1 עד 5)
        String ratingString = String.valueOf((int) ratingValue);

        // קריאה לפונקציית השמירה לפיירבייס
        savePostToFirebase(bookId, bookName, authorName, content, ratingString);
    }

    // פונקציית השמירה (אותה אחת שבנינו בשלבים הקודמים)
    private void savePostToFirebase(String bId, String bName, String bAuthor, String content, String rating) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userName = user.getDisplayName();
        if (userName == null || userName.isEmpty()) userName = "קורא אנונימי";

        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        // יצירת האובייקט
        FeedPost post = new FeedPost(bName, bAuthor, content, userName, date, rating, bookImageBase64);

        DatabaseReference allPostsRef = FirebaseDatabase.getInstance().getReference("all_posts");

        // שמירה תחת ה-ID של הספר
        allPostsRef.child(bId).setValue(post)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RateAndPostActivity.this, "הפוסט פורסם בהצלחה!", Toast.LENGTH_SHORT).show();
                    DatabaseReference userBookRef = FirebaseDatabase.getInstance()
                            .getReference("books")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(bookId);
                    // עדכון השדה הספציפי ל-true
                    userBookRef.child("hasPost").setValue(true);
                    finish(); // סגירת המסך וחזרה לאפליקציה
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RateAndPostActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}