package com.example.finalprojectleojacobovitz;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editFullName, editEmail;
    private Button btnSave;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // אתחול Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        // קישור רכיבי ממשק המשתמש
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        btnSave = findViewById(R.id.btnSave);

        // טעינת נתונים קיימים כדי שהמשתמש יראה מה הוא עורך
        loadUserData();

        btnSave.setOnClickListener(v -> validateAndUpdate());

        Button btnResetPassword = findViewById(R.id.btnResetPassword);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת שדה טקסט להזנת הסיסמה בתוך הדיאלוג
                final EditText resetPassword = new EditText(v.getContext());

                // יצירת הדיאלוג
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("שינוי סיסמה");
                passwordResetDialog.setMessage("הזן סיסמה חדשה (לפחות 6 תווים):");
                passwordResetDialog.setView(resetPassword); // הוספת שדה הטקסט לדיאלוג

                passwordResetDialog.setPositiveButton("שמור", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // שליפת הסיסמה שהמשתמש הזין
                        String newPassword = resetPassword.getText().toString();

                        // עדכון הסיסמה ב-Firebase Authentication
                        mAuth.getCurrentUser().updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(EditProfileActivity.this, "הסיסמה שונתה בהצלחה", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditProfileActivity.this, "שגיאה בשינוי הסיסמה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

                passwordResetDialog.setNegativeButton("ביטול", null);
                passwordResetDialog.create().show();
            }
        });

    }

    private void loadUserData() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editFullName.setText(documentSnapshot.getString("fName"));
                        editEmail.setText(documentSnapshot.getString("email"));
                    }
                });
    }

    private void validateAndUpdate() {
        String newName = editFullName.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();

        // בדיקות תקינות קלט
        if (newName.isEmpty()) {
            editFullName.setError("נא להזין שם מלא");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            editEmail.setError("נא להזין אימייל תקין");
            return;
        }

        updateProfile(newName, newEmail);
    }

    private void updateProfile(String name, String email) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // שלב 1: עדכון האימייל ב-Firebase Authentication
            user.updateEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // שלב 2: אם העדכון ב-Auth הצליח, מעדכנים את Firestore
                            updateFirestore(name, email);
                        } else {
                            // טיפול בשגיאת "חיבור לא טרי" (Requires Re-authentication)
                            String error = task.getException() != null ? task.getException().getMessage() : "שגיאה לא ידועה";
                            Toast.makeText(this, "עדכון נכשל: " + error, Toast.LENGTH_LONG).show();

                            // הערה: כאן מומלץ להפנות את המשתמש למסך התחברות מחדש
                        }
                    });
        }
    }

    private void updateFirestore(String name, String email) {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("fName", name);
        userUpdates.put("email", email);

        db.collection("users").document(userId)
                .update(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "הפרופיל עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish(); // חזרה למסך הקודם
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "שגיאה בעדכון מסד הנתונים", Toast.LENGTH_SHORT).show();
                });
    }


}