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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RateAndPostActivity extends AppCompatActivity {

    private TextView tvBookTitle, tvBookAuthor;
    private RatingBar ratingBar;
    private TextInputEditText etPostContent;
    private Button btnPublish, btnSkip;

    private String bookId, bookName, authorName;
    private String bookImageBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_and_post);

        tvBookTitle = findViewById(R.id.tv_book_title_header);
        tvBookAuthor = findViewById(R.id.tv_book_author_header);
        ratingBar = findViewById(R.id.ratingBar_input);
        etPostContent = findViewById(R.id.et_post_content);
        btnPublish = findViewById(R.id.btn_publish_post);
        btnSkip = findViewById(R.id.btn_skip);

        if (getIntent() != null) {
            bookId = getIntent().getStringExtra("BOOK_ID");
            bookName = getIntent().getStringExtra("BOOK_NAME");
            authorName = getIntent().getStringExtra("BOOK_AUTHOR");
            bookImageBase64 = getIntent().getStringExtra("BOOK_IMAGE");

            tvBookTitle.setText(bookName);
            tvBookAuthor.setText(authorName);
        }

        btnPublish.setOnClickListener(v -> submitPost());
        btnSkip.setOnClickListener(v -> finish());
    }

    private void submitPost() {
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

        String ratingString = String.valueOf((int) ratingValue);
        savePostToFirebase(bookId, bookName, authorName, content, ratingString);
    }

    private void savePostToFirebase(String bId, String bName, String bAuthor, String content, String rating) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        //  גישה ל-Firestore כדי לשלוף את השם
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        DocumentReference documentReference = fstore.collection("users").document(userId);

        documentReference.get().addOnSuccessListener(documentSnapshot -> {

            String userName = "קורא אנונימי"; // ברירת מחדל

            if (documentSnapshot.exists()) {
                // שליפת השם
                String fetchedName = documentSnapshot.getString("fName");
                if (fetchedName != null && !fetchedName.isEmpty()) {
                    userName = fetchedName;
                }
            }

            // יצירת התאריך
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            // יצירת האובייקט עם השם ששלפנו מ-Firestore
            FeedPost post = new FeedPost(bName, bAuthor, content, userName, date, rating, bookImageBase64);

            // שמירה ב-Realtime Database
            DatabaseReference allPostsRef = FirebaseDatabase.getInstance().getReference("all_posts");

            allPostsRef.child(bId).setValue(post)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(RateAndPostActivity.this, "הפוסט פורסם בהצלחה!", Toast.LENGTH_SHORT).show();

                        // עדכון שהספר פורסם
                        DatabaseReference userBookRef = FirebaseDatabase.getInstance()
                                .getReference("books")
                                .child(userId)
                                .child(bookId);
                        userBookRef.child("hasPost").setValue(true);

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(RateAndPostActivity.this, "שגיאה בפרסום: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        }).addOnFailureListener(e -> {
            // טיפול במקרה שלא הצליח להביא את השם מ-Firestore
            Toast.makeText(RateAndPostActivity.this, "שגיאה בשליפת פרטי משתמש", Toast.LENGTH_SHORT).show();
        });
    }
}