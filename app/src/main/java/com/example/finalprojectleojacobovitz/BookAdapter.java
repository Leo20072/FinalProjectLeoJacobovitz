package com.example.finalprojectleojacobovitz;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final Context context;
    private List<Book> booksList;
    private List<String> bookKeys; // המפתחות של Firebase לפעולות מחיקה/עדכון


    // 1. קונסטרוקטור
    public BookAdapter(Context context) {
        this.context = context;
        this.booksList = new ArrayList<>();
        this.bookKeys = new ArrayList<>();
    }

    // 2. מחלקה פנימית BookViewHolder
    public static class BookViewHolder extends RecyclerView.ViewHolder {
        // רכיבי ה-UI מתוך list_item_book.xml
        TextView title, author, pages, category, startDate;
        Button btnView, btnEdit, btnDelete;
        private ImageView imageViewResult;
        private ProgressBar progressBar;
        private TextView percentageText;

      ;

        public BookViewHolder(View itemView) {
            super(itemView);
            imageViewResult = itemView.findViewById(R.id.imageViewResult);
            title = itemView.findViewById(R.id.tv_book_title);
            author = itemView.findViewById(R.id.tv_book_author);
            pages = itemView.findViewById(R.id.tv_pages_count);
            category = itemView.findViewById(R.id.tv_category);
            startDate = itemView.findViewById(R.id.tv_start_date); // חדש
            progressBar = itemView.findViewById(R.id.horizontal_progress_bar);
            percentageText = itemView.findViewById(R.id.progress_percentage_text);

            btnView = itemView.findViewById(R.id.btn_view);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);

        }
    }

    // 3. יצירת ה-ViewHolder (מנפח את ה-XML)
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    // 4. קישור נתונים לרכיבי ה-UI
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book currentBook = booksList.get(position);
        final String currentKey = bookKeys.get(position); // המפתח הייחודי

        String base64 = currentBook.getUploadImageUrl();
        Bitmap decodedBitmap = decodeImage(base64);


        // הצגת פרטי הספר
        holder.title.setText(currentBook.getNameOfBook());
        holder.author.setText("מאת: " + currentBook.getAuthorsname());
        holder.pages.setText("עמודים: " + currentBook.getUploadPagesCount());
        holder.category.setText("קטגוריה: " + currentBook.getUploadCategory());
        holder.startDate.setText("התחלה: " + currentBook.getUploadStartDate());
        holder.imageViewResult.setImageBitmap(decodedBitmap);

        try {
            int totalPages = Integer.parseInt(currentBook.getUploadPagesCount());
            int pagesRead = Integer.parseInt(currentBook.getPagesread()); // נניח ש-getPagesread מחזיר את העמוד הנוכחי

            // א. הגדרת המקסימום וההתקדמות
            holder.progressBar.setMax(totalPages);
            // יש לוודא ש-pagesRead לא גדול מ-totalPages
            holder.progressBar.setProgress(Math.min(pagesRead, totalPages));

            // ב. חישוב האחוזים
            double percentage = 0.0;
            if (totalPages > 0) {
                percentage = ((double) pagesRead / totalPages) * 100;
            }

            String percentageDisplay = String.format(Locale.US, "%.0f%%", percentage);

            // ג. הצגת הטקסט
            if (pagesRead >= totalPages) {
                holder.percentageText.setText("הושלם! 100% 🎉");
                // יצירת Intent למעבר למסך הדירוג
                if (!currentBook.isHasPost()){
                Intent intent = new Intent(context, RateAndPostActivity.class);
                intent.putExtra("BOOK_ID", currentKey);       // ה-ID של הספר (ה-Key מפיירבייס)
                intent.putExtra("BOOK_NAME", currentBook.getNameOfBook());
                intent.putExtra("BOOK_AUTHOR", currentBook.getAuthorsname());
                intent.putExtra("BOOK_IMAGE", currentBook.getUploadImageUrl());
                context.startActivity(intent);
                }
            } else {
                holder.percentageText.setText(pagesRead + " מתוך " + totalPages + " (" + percentageDisplay + ")");
            }

        } catch (NumberFormatException e) {
            // טיפול בשגיאה אם הנתונים מ-Firebase אינם מספרים תקינים
            holder.percentageText.setText("שגיאת נתונים");
            holder.progressBar.setProgress(0);
            e.printStackTrace();
        }



        // טיפול בלחיצות על הכפתורים
        holder.btnDelete.setOnClickListener(v -> {
            // קריאה לפונקציית מחיקה שתטמיע בהמשך
            // handleBookDelete(currentKey);
            System.out.println("לחצת על מחק לספר ID: " + currentKey);
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference booksRootRef = FirebaseDatabase.getInstance().getReference("books");
            DatabaseReference userBooksRef = booksRootRef.child(userId);
            userBooksRef.child(currentKey).removeValue();




        });


        holder.btnEdit.setOnClickListener(v -> {
            // קריאה לפונקציית עדכון שתטמיע בהמשך
            // handleBookEdit(currentBook, currentKey);
            System.out.println("לחצת על עדכן לספר: " + currentBook.getNameOfBook());

            //((ListOfBooks)context).createDialogEdit();

            // הקריאה החוזרת ל-MainActivity
            ///onEditBook(currentKey, currentBook);
            ((ListOfBooks)context).onEditBook(currentKey, currentBook);



        });

        holder.btnView.setOnClickListener(v -> {
            // פעולת הצגת פרטים מלאים
            System.out.println("לחצת על צפה לספר: " + currentBook.getNameOfBook());
        });
    }




    // 5. קבלת מספר הפריטים ברשימה
    @Override
    public int getItemCount() {
        return booksList.size();
    }

    // 6. עדכון רשימת הספרים מה-Activity
    public void setBooks(List<Book> books, List<String> keys) {
        this.booksList = books;
        this.bookKeys = keys;
        notifyDataSetChanged(); // רענון ה-RecyclerView
    }

    // --- פונקציה להמרת Base64 String חזרה ל-Bitmap (כמו בקוד הקודם) ---
    public static Bitmap decodeImage(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}

