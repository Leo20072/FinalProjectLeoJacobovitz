package com.example.finalprojectleojacobovitz;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentsActivity extends AppCompatActivity {

    private String bookId; // ה-ID של הספר שעליו אנחנו מגיבים
    private RecyclerView recyclerView;
    private CommentsAdapter adapter;
    private EditText etInput;
    private ImageButton btnSend;
    private DatabaseReference commentsRef;
    private ValueEventListener commentsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        // קבלת ה-ID מה-Intent
        if (getIntent() != null) {
            bookId = getIntent().getStringExtra("BOOK_ID_KEY");
        }

        if (bookId == null) {
            Toast.makeText(this, "שגיאה בטעינת תגובות", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // אתחול
        recyclerView = findViewById(R.id.recycler_view_comments);
        etInput = findViewById(R.id.et_comment_input);
        btnSend = findViewById(R.id.btn_send_comment);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentsAdapter(this);
        recyclerView.setAdapter(adapter);

        // הפניה לענף התגובות הספציפי של הספר הזה
        commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(bookId);

        // טעינת תגובות
        loadComments();

        // שליחת תגובה
        btnSend.setOnClickListener(v -> postComment());
    }

    private void postComment() {
        String text = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        // גישה ל-Firestore לשליפת השם
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        DocumentReference docRef = fstore.collection("users").document(userId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {

            String userName = "משתמש"; // ברירת מחדל

            // בדיקה שקיים ויש בו שם
            if (documentSnapshot.exists()) {
                String fetchedName = documentSnapshot.getString("fName");
                if (fetchedName != null && !fetchedName.isEmpty()) {
                    userName = fetchedName;
                }
            }

            // תאריך
            String time = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date());

            //  יצירת אובייקט התגובה עם השם
            Comment newComment = new Comment(text, userId, userName, time);

            //  שמירה ב-Realtime Database
            commentsRef.push().setValue(newComment)
                    .addOnSuccessListener(aVoid -> {
                        etInput.setText("");

                        // גלילה למטה לתגובה החדשה
                        if (adapter.getItemCount() > 0) {
                            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CommentsActivity.this, "שגיאה בשליחה", Toast.LENGTH_SHORT).show();
                    });

        }).addOnFailureListener(e -> {
            // טיפול במקרה שהייתה בעיה לשלוף את השם מ-Firestore
            Toast.makeText(CommentsActivity.this, "שגיאה בטעינת פרטי משתמש", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadComments() {
        commentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> list = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Comment c = snap.getValue(Comment.class);
                    if (c != null) list.add(c);
                }
                adapter.setComments(list);
                // גלילה למטה כשפותחים את המסך
                if (list.size() > 0) {
                    recyclerView.scrollToPosition(list.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        commentsRef.addValueEventListener(commentsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentsRef != null && commentsListener != null) {
            commentsRef.removeEventListener(commentsListener);
        }
    }
}