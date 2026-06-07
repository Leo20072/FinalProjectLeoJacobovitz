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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final Context context;
    private List<Book> booksList;
    private List<String> bookKeys; // המפתחות של Firebase לפעולות מחיקה/עדכון

    public BookAdapter(Context context) {
        this.context = context;
        this.booksList = new ArrayList<>();
        this.bookKeys = new ArrayList<>();
    }

    //  מחלקה פנימית BookViewHolder
    public static class BookViewHolder extends RecyclerView.ViewHolder {
        // רכיבי ה-UI מתוך list_item_book.xml
        TextView title, author, pages, category, startDate;
        Button btnEdit, btnDelete;
        private ImageView imageViewResult;
        private ProgressBar progressBar;
        private TextView percentageText;

        public BookViewHolder(View itemView) {
            super(itemView);
            imageViewResult = itemView.findViewById(R.id.imageViewResult);
            title = itemView.findViewById(R.id.tv_book_title);
            author = itemView.findViewById(R.id.tv_book_author);
            pages = itemView.findViewById(R.id.tv_pages_count);
            category = itemView.findViewById(R.id.tv_category);
            startDate = itemView.findViewById(R.id.tv_start_date);
            progressBar = itemView.findViewById(R.id.horizontal_progress_bar);
            percentageText = itemView.findViewById(R.id.progress_percentage_text);

            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    //  יצירת ה-ViewHolder
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    //  קישור נתונים לרכיבי ה-UI
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

        // הצגת התמונה רק אם היא קיימת כדי למנוע קריסה
        if (decodedBitmap != null) {
            holder.imageViewResult.setImageBitmap(decodedBitmap);
        } else {
            holder.imageViewResult.setImageBitmap(null);
        }

        try {
            int totalPages = Integer.parseInt(currentBook.getUploadPagesCount());
            int pagesRead = Integer.parseInt(currentBook.getPagesread());

            holder.progressBar.setMax(totalPages);
            holder.progressBar.setProgress(Math.min(pagesRead, totalPages));

            double percentage = 0.0;
            if (totalPages > 0) {
                percentage = ((double) pagesRead / totalPages) * 100;
            }

            String percentageDisplay = String.format(Locale.US, "%.0f%%", percentage);

            if (pagesRead >= totalPages) {
                holder.percentageText.setText("הושלם! 100% 🎉 (לחץ כאן לדירוג)");


                holder.percentageText.setOnClickListener(v -> {
                    if (!currentBook.isHasPost()) {
                        Intent intent = new Intent(context, RateAndPostActivity.class);
                        intent.putExtra("BOOK_ID", currentKey);
                        intent.putExtra("BOOK_NAME", currentBook.getNameOfBook());
                        intent.putExtra("BOOK_AUTHOR", currentBook.getAuthorsname());
                        intent.putExtra("BOOK_IMAGE", currentBook.getUploadImageUrl());
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "כבר שיתפת פוסט על ספר זה!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                holder.percentageText.setText(pagesRead + " מתוך " + totalPages + " (" + percentageDisplay + ")");
                holder.percentageText.setOnClickListener(null);
            }

        } catch (NumberFormatException e) {
            holder.percentageText.setText("שגיאת נתונים");
            holder.progressBar.setProgress(0);
            e.printStackTrace();
        }

        holder.btnDelete.setOnClickListener(v -> {
            System.out.println("לחצת על מחק לספר ID: " + currentKey);
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference booksRootRef = FirebaseDatabase.getInstance().getReference("books");
            DatabaseReference userBooksRef = booksRootRef.child(userId);
            userBooksRef.child(currentKey).removeValue();

            DatabaseReference postRef = FirebaseDatabase.getInstance()
                    .getReference("all_posts")
                    .child(currentKey);
            postRef.removeValue();
        });

        holder.btnEdit.setOnClickListener(v -> {
            System.out.println("לחצת על עדכן לספר: " + currentBook.getNameOfBook());
            ((ListOfBooks)context).onEditBook(currentKey, currentBook);
        });

    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }

    public void setBooks(List<Book> books, List<String> keys) {
        this.booksList = books;
        this.bookKeys = keys;
        notifyDataSetChanged();
    }

    public static Bitmap decodeImage(String base64String) {
        if (base64String == null || base64String.trim().isEmpty()) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}