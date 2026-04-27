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
    String base64String;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_books);

        btnback = findViewById(R.id.btnback);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ListOfBooks.this, MainActivity.class));
            }
        });

        // אתחול ה-RecyclerView
        recyclerView = findViewById(R.id.booksrecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // אתחול וחיבור ה-Adapter
        bookAdapter = new BookAdapter(this);
        recyclerView.setAdapter(bookAdapter);

        // לשלוף נתונים
        retrieveUserBooks();

    }

    // "שליפה"
    public void retrieveUserBooks() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // המשתמש לא מחובר
            return;
        }
        String userId = user.getUid();

        userBooksRef = FirebaseDatabase.getInstance().getReference("books").child(userId);


        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Book> booksList = new ArrayList<>();
                List<String> bookKeys = new ArrayList<>();


                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    String bookKey = bookSnapshot.getKey();
                    Book book = bookSnapshot.getValue(Book.class);

                    if (book != null && bookKey != null) {
                        booksList.add(book);
                        bookKeys.add(bookKey);
                    }
                }

                // עדכון ה-Adapter
                bookAdapter.setBooks(booksList, bookKeys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // שגיאה
                Toast.makeText(ListOfBooks.this, "שגיאה בקריאת נתונים: ", Toast.LENGTH_SHORT).show();

            }
        };
        // הוספת המאזין
        userBooksRef.addValueEventListener(valueEventListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // הסרת המאזין
        if (userBooksRef != null && valueEventListener != null) {
            userBooksRef.removeEventListener(valueEventListener);
        }
    }


    public void onEditBook(String bookKey, Book bookToEdit) {
        showEditBookDialog(bookKey, bookToEdit);
    }

    private void showEditBookDialog(String bookKey, Book bookToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_edit, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.editBookName);
        EditText etAuthor = dialogView.findViewById(R.id.editAuthorName);

        // זה השדה שמציג ונותן לערוך את סך כל העמודים
        EditText etTotalPages = dialogView.findViewById(R.id.editTotalPages);

        // זה השדה של העמוד הנוכחי
        EditText etCurrentPage = dialogView.findViewById(R.id.editCurrentPage);

        Button btnAdd1 = dialogView.findViewById(R.id.btnAdd1Page);
        Button btnAdd10 = dialogView.findViewById(R.id.btnAdd10Pages);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);

        base64String = bookToEdit.getUploadImageUrl();

        // פתיחת הגלריה לבחירת תמונה
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        //  מילוי הנתונים מfirebase אל תוך המסך
        etTitle.setText(bookToEdit.getNameOfBook());
        etAuthor.setText(bookToEdit.getAuthorsname());

        // מציג את סך הכל העמודים בספר (המשתמש יכול ללחוץ ולשנות את זה)
        etTotalPages.setText(bookToEdit.getUploadPagesCount());

        // מציג את העמוד הנוכחי
        if (bookToEdit.getPagesread() != null && !bookToEdit.getPagesread().isEmpty()) {
            etCurrentPage.setText(bookToEdit.getPagesread());
        } else {
            etCurrentPage.setText("0");
        }

        // פעולת +1 עם חסימה מלעבור את סך העמודים
        btnAdd1.setOnClickListener(view -> {
            int current = 0;
            int total = 0;
            try { current = Integer.parseInt(etCurrentPage.getText().toString().trim()); } catch (Exception ignored) {}
            try { total = Integer.parseInt(etTotalPages.getText().toString().trim()); } catch (Exception ignored) {}

            if (current + 1 <= total) {
                etCurrentPage.setText(String.valueOf(current + 1));
            } else {
                Toast.makeText(this, "הגעת לסוף הספר!", Toast.LENGTH_SHORT).show();
                etCurrentPage.setText(String.valueOf(total));
            }
        });

        // פעולת +10 עם חסימה מלעבור את סך העמודים
        btnAdd10.setOnClickListener(view -> {
            int current = 0;
            int total = 0;
            try { current = Integer.parseInt(etCurrentPage.getText().toString().trim()); } catch (Exception ignored) {}
            try { total = Integer.parseInt(etTotalPages.getText().toString().trim()); } catch (Exception ignored) {}

            if (current + 10 <= total) {
                etCurrentPage.setText(String.valueOf(current + 10));
            } else {
                Toast.makeText(this, "אי אפשר לעבור את סך העמודים!", Toast.LENGTH_SHORT).show();
                etCurrentPage.setText(String.valueOf(total));
            }
        });

        builder.setPositiveButton("שמור שינויים", null);
        builder.setNegativeButton("ביטול", (dialog, id) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // פעולת השמירה, מתבצעת רק לאחר בדיקת תקינות
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newTitle = etTitle.getText().toString().trim();
            String newAuthor = etAuthor.getText().toString().trim();

            // לוקחים את הנתון מהשדה (גם אם המשתמש שינה אותו עכשיו)
            String newTotalPages = etTotalPages.getText().toString().trim();
            String newCurrentPage = etCurrentPage.getText().toString().trim();

            if (newTitle.isEmpty() || newAuthor.isEmpty()) {
                Toast.makeText(this, "שם הספר ושם המחבר אינם יכולים להיות ריקים.", Toast.LENGTH_SHORT).show();
                return;
            }

            int current = 0;
            int total = 0;
            try { current = Integer.parseInt(newCurrentPage); } catch (Exception ignored) {}
            try { total = Integer.parseInt(newTotalPages); } catch (Exception ignored) {}

            if (current > total) {
                Toast.makeText(this, "שגיאה: מספר העמודים שקראת (" + current + ") לא יכול להיות גדול מסך העמודים בספר (" + total + ")!", Toast.LENGTH_LONG).show();
                etCurrentPage.setError("מספר גדול מדי");
                return;
            }

            // שומרים את האובייקט המעודכן עם סך העמודים החדש (או הישן)
            Book updatedBook = new Book();
            updatedBook.setNameOfBook(newTitle);
            updatedBook.setAuthorsname(newAuthor);
            updatedBook.setUploadImageUrl(base64String);
            updatedBook.setUploadPagesCount(newTotalPages); // שומר את סך העמודים
            updatedBook.setPagesread(newCurrentPage);       // שומר את העמוד הנוכחי

            // שדות שאינם לעריכה
            updatedBook.setUploadCategory(bookToEdit.getUploadCategory());
            updatedBook.setUploadStartDate(bookToEdit.getUploadStartDate());

            updateBook(bookKey, updatedBook);
            Toast.makeText(this, "הספר עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    public void updateBook(String bookKey, Book updatedBook) {
        // קבלת המשתמש המחובר ואימות
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || bookKey == null) {
            System.err.println("שגיאה: משתמש לא מאומת או מפתח ספר חסר לעדכון.");
            return;
        }
        String userId = user.getUid();

        // יצירת הפניה לנתיב הספר הספציפי
        DatabaseReference bookToUpdateRef = FirebaseDatabase.getInstance()
                .getReference("books")
                .child(userId)
                .child(bookKey);

        // יצירת מפה (Map) המכילה את כל השדות לעדכון
        // להמיר את כל האובייקט Book המעודכן למפה
        Map<String, Object> bookValues = new HashMap<>();

        // הוספת כל ששת השדות מהאובייקט "המעודכן"
        bookValues.put("authorsname", updatedBook.getAuthorsname());
        bookValues.put("nameOfBook", updatedBook.getNameOfBook());
        bookValues.put("uploadCategory", updatedBook.getUploadCategory());
        bookValues.put("uploadImageUrl", updatedBook.getUploadImageUrl());
        bookValues.put("uploadPagesCount", updatedBook.getUploadPagesCount());
        bookValues.put("uploadStartDate", updatedBook.getUploadStartDate());
        bookValues.put("pagesread", updatedBook.getPagesread());

        // ביצוע פעולת העדכון
        bookToUpdateRef.updateChildren(bookValues)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("✅ הספר עודכן בהצלחה. Key: " + bookKey);
                        // להוסיף Toast
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.err.println("❌ כישלון בעדכון הספר: " + e.getMessage());
                        // להוסיף Toast שגיאה
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // שהתוצאה היא מתאימה
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // המרת ה-URI ל-Bitmap
                Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                //  המרה ל-Base64
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

}