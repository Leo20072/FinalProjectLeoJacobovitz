package com.example.finalprojectleojacobovitz;

import static android.opengl.ETC1.encodeImage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Base64;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Addbook extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner spinner;
    private String[] arrCategories = {
            "לא ידוע", "הרפתקה","אמנות", "אוטוביוגרפיה", "ביוגרפיה", "עסקים", "ילדים", "קלאסי", "עכשווי", "בישול", "פשע", "דרמה", "כלכלה", "חינוך", "פנטזיה", "מדע בדיוני", "בריאות", "היסטורי", "היסטוריה", "אימה", "הומור", "מוזיקה", "מסתורין", "ספרי עיון", "פילוסופיה", "פסיכולוגיה", "דת", "רומן", "מדע", "עזרה עצמית", "ספורט", "טכנולוגיה", "מותחן", "טיולים", "נוער"
    };

    private String choosecategory;
    EditText uploadImageUrl;
    Button saveButton;
    EditText nameofbook, authorsname, uploadPagesCount, uploadStartDate;
    String pagesread = String.valueOf(0);
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    // המרה תמונה לטקסט
    private Button btnSelectImage;
    private static final String TAG = "Base64Converter";
    private static final int PICK_IMAGE_REQUEST = 1;
    String base64String = "";
    private ImageView imageViewResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addbook);
        btnSelectImage = findViewById(R.id.btnSelectImage);

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // קריאה לפונקציה לפתיחת הגלריה
                openGallery();
            }
        });

        spinner = findViewById(R.id.uploadCategory);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter aa =
                new ArrayAdapter(this, android.R.layout.simple_spinner_item, arrCategories);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aa);

        nameofbook = findViewById(R.id.nameofbook);
        authorsname = findViewById(R.id.authorsname);
        uploadPagesCount = findViewById(R.id.uploadPagesCount);
        uploadStartDate = findViewById(R.id.uploadStartDate);
        saveButton = findViewById(R.id.saveButton);
        imageViewResult = findViewById(R.id.imageViewResult);


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String namebook = nameofbook.getText().toString();
                //String image = uploadImageUrl.getText().toString();
                String author = authorsname.getText().toString();
                String pagesCount = uploadPagesCount.getText().toString();
                String startDate = uploadStartDate.getText().toString();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(Addbook.this, "שגיאה: המשתמש לא אומת.", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference booksRootRef = FirebaseDatabase.getInstance().getReference("books");
                DatabaseReference userBooksRef = booksRootRef.child(userId);

                String newBookId = userBooksRef.push().getKey();

                if (newBookId != null) {
                    Book newbook = new Book(namebook, author, pagesCount, base64String, choosecategory,startDate,pagesread);


                    // שמירת הנתונים בנתיב החדש באמצעות setValue
                    if (!namebook.equals("") && !author.equals("") && !pagesCount.equals("") && !startDate.equals(""))
                    {
                        userBooksRef.child(newBookId).setValue(newbook)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(Addbook.this, "Book saved successfully for user: " + userId, Toast.LENGTH_SHORT).show();
                                    // עדכון ממשק המשתמש
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Addbook.this, "Failed to save book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    // הצגת הודעת שגיאה למשתמש
                                });
                    }
                    else {
                        Toast.makeText(Addbook.this, "שמירת הספר נכשלה", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });

    }

    // פונקציה לפתיחת הגלריה
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // קבלת התוצאה לאחר בחירת התמונה מהגלריה
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ודא שהתוצאה היא מתאימה
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // המרת ה-URI ל-Bitmap
                Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                imageViewResult.setImageBitmap(selectedBitmap);

                // המרה ל-Base64
                base64String =encodeImage(selectedBitmap);
                Log.d(TAG, "Encoded Base64 String: " + base64String.substring(0, Math.min(base64String.length(), 50)) + "...");
                Toast.makeText(this, "התמונה קודדה ל-Base64", Toast.LENGTH_SHORT).show();


            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "שגיאה בטעינת התמונה: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        choosecategory = arrCategories[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    //  פונקציה להמרת Bitmap ל-Base64
    public static String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

//    // --- פונקציה להמרת Base64 String חזרה ל-Bitmap (כמו בקוד הקודם) ---
//    public static Bitmap decodeImage(String base64String) {
//        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
//        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
//    }
}