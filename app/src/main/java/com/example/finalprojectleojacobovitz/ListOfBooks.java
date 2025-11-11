package com.example.finalprojectleojacobovitz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListOfBooks extends AppCompatActivity {


    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private DatabaseReference userBooksRef;
    private ValueEventListener valueEventListener;
    Button btnback;
    private static final String TAG = "Base64Converter";
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageViewResult;
    String base64String;
    //int numberpages = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_books); // ודא שזה מכיל את ה-RecyclerView

        btnback = findViewById(R.id.btnback);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ListOfBooks.this, MainActivity.class));
            }
        });

        // 1. אתחול ה-RecyclerView
        recyclerView = findViewById(R.id.booksrecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // 2. אתחול וחיבור ה-Adapter
        bookAdapter = new BookAdapter(this);
        recyclerView.setAdapter(bookAdapter);

        // 3. התחל לשלוף נתונים
        retrieveUserBooks();

    }

    // שיטת Retrieve (שליפה)
    public void retrieveUserBooks() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // המשתמש לא מחובר
            return;
        }
        String userId = user.getUid();

        userBooksRef = FirebaseDatabase.getInstance().getReference("books").child(userId);

        Query searchQuery;


        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Book> booksList = new ArrayList<>();
                List<String> bookKeys = new ArrayList<>();


                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    String bookKey = bookSnapshot.getKey();
                    // שימוש במחלקת Book המתוקנת
                    Book book = bookSnapshot.getValue(Book.class);

                    if (book != null && bookKey != null) {
                        booksList.add(book);
                        bookKeys.add(bookKey);
                    }
                }

                // עדכון ה-Adapter עם רשימת הספרים והמפתחות
                bookAdapter.setBooks(booksList, bookKeys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // טיפול בשגיאה
                Toast.makeText(ListOfBooks.this, "שגיאה בקריאת נתונים: ", Toast.LENGTH_SHORT).show();

            }
        };
        // הוספת המאזין (ירוץ פעם אחת וכן בכל שינוי עתידי)
        userBooksRef.addValueEventListener(valueEventListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // הסרת המאזין כדי למנוע דליפת זיכרון
        if (userBooksRef != null && valueEventListener != null) {
            userBooksRef.removeEventListener(valueEventListener);
        }
    }


    public void onEditBook(String bookKey, Book bookToEdit) {
        showEditBookDialog(bookKey, bookToEdit);
    }

    private void showEditBookDialog(String bookKey, Book bookToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("עריכת פרטי ספר");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_edit, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.changeNameBook);
        EditText etAuthor = dialogView.findViewById(R.id.changeAuthorsName);
        EditText etPages = dialogView.findViewById(R.id.currentPagesCount);
        EditText etImageUrl = dialogView.findViewById(R.id.changeImage);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        base64String = bookToEdit.getUploadImageUrl();
        Button btnAdd1CurrentPagesCount = dialogView.findViewById(R.id.btnAdd1CurrentPagesCount);

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // קריאה לפונקציה לפתיחת הגלריה
                openGallery();
            }

            private void openGallery() {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });


        // 2. מילוי השדות בנתונים הנוכחיים של הספר
        etTitle.setText(bookToEdit.getNameOfBook());
        etAuthor.setText(bookToEdit.getAuthorsname());
        etPages.setText(bookToEdit.getUploadPagesCount()); // מילוי מספר העמודים
        etImageUrl.setText(bookToEdit.getUploadImageUrl()); // מילוי קישור התמונה
        //   String strnaumpages = bookToEdit.getPagesread();
        final int[] pagesRead = {Integer.parseInt(bookToEdit.getPagesread())};
        //numberpages = Integer.parseInt(strnaumpages);
        btnAdd1CurrentPagesCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pagesRead[0] = pagesRead[0] + 1;
            }
        });


        // 3. כפתור 'שמור' (עדכון הנתונים ב-Firebase)
        builder.setPositiveButton("שמור שינויים", (dialog, id) -> {

            // א. קבלת הנתונים החדשים
            String newTitle = etTitle.getText().toString().trim();
            String newAuthor = etAuthor.getText().toString().trim();
            String newPagesCount = etPages.getText().toString().trim();
            String newImageUrl = base64String;
            String newstrnaumpages = Integer.toString(pagesRead[0]);

            if (newTitle.isEmpty() || newAuthor.isEmpty()) {
                Toast.makeText(this, "שם הספר ושם המחבר אינם יכולים להיות ריקים.", Toast.LENGTH_LONG).show();
                return;
            }

            // ב. יצירת אובייקט Book מעודכן
            Book updatedBook = new Book();

            // --- 1. שדות מעודכנים (מהדיאלוג) ---
            updatedBook.setNameOfBook(newTitle);
            updatedBook.setAuthorsname(newAuthor);
            updatedBook.setUploadPagesCount(newPagesCount);
            updatedBook.setUploadImageUrl(newImageUrl);
            updatedBook.setPagesread(newstrnaumpages);

            // --- 2. שדות שאינם ניתנים לעריכה (העתקה מהאובייקט המקורי) ---
            // נתונים אלה חיוניים כדי לא לאבד אותם ב-Firebase!
            updatedBook.setUploadCategory(bookToEdit.getUploadCategory());
            updatedBook.setUploadStartDate(bookToEdit.getUploadStartDate());

            // ג. קריאה לשיטת העדכון ב-Firebase
            updateBook(bookKey, updatedBook);

            Toast.makeText(this, "הספר עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
        });

        // 4. כפתור 'ביטול'
        builder.setNegativeButton("ביטול", (dialog, id) -> dialog.cancel());

        // 5. הצגת הדיאלוג
        builder.create().show();
    }

    public void updateBook(String bookKey, Book updatedBook) {
        // 1. קבלת המשתמש המחובר ואימות
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || bookKey == null) {
            System.err.println("שגיאה: משתמש לא מאומת או מפתח ספר חסר לעדכון.");
            return;
        }
        String userId = user.getUid();

        // 2. יצירת הפניה לנתיב הספר הספציפי: books/$UID/$bookKey
        DatabaseReference bookToUpdateRef = FirebaseDatabase.getInstance()
                .getReference("books")
                .child(userId)
                .child(bookKey);

        // 3. יצירת מפה (Map) המכילה את כל השדות לעדכון
        // אנו ממירים את כל האובייקט Book המעודכן למפה.
        Map<String, Object> bookValues = new HashMap<>();

        // הוספת כל ששת השדות מהאובייקט המעודכן
        bookValues.put("authorsname", updatedBook.getAuthorsname());
        bookValues.put("nameOfBook", updatedBook.getNameOfBook());
        bookValues.put("uploadCategory", updatedBook.getUploadCategory());
        bookValues.put("uploadImageUrl", updatedBook.getUploadImageUrl());
        bookValues.put("uploadPagesCount", updatedBook.getUploadPagesCount());
        bookValues.put("uploadStartDate", updatedBook.getUploadStartDate());
        bookValues.put("pagesread", updatedBook.getPagesread());

        // 4. ביצוע פעולת העדכון (updateChildren)
        bookToUpdateRef.updateChildren(bookValues)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("✅ הספר עודכן בהצלחה. Key: " + bookKey);
                        // ניתן להוסיף Toast או הודעה למשתמש
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.err.println("❌ כישלון בעדכון הספר: " + e.getMessage());
                        // ניתן להוסיף Toast שגיאה
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ודא שהתוצאה היא מתאימה (בחירת תמונה הצליחה)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // המרת ה-URI ל-Bitmap
                Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                // --- המרה ל-Base64 ---
                base64String = encodeImage(selectedBitmap);
                Log.d(TAG, "Encoded Base64 String: " + base64String.substring(0, Math.min(base64String.length(), 50)) + "...");
                Toast.makeText(this, "התמונה קודדה ל-Base64", Toast.LENGTH_SHORT).show();


            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "שגיאה בטעינת התמונה: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // דחיסה עם איכות, ניתן לשנות את האיכות כאן אם התמונה גדולה מדי
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

}