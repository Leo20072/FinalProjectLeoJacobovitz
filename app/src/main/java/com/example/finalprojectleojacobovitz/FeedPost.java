package com.example.finalprojectleojacobovitz;

public class FeedPost {
    public String bookName;     // שם הספר
    public String authorName;   // שם הסופר
    public String postContent;  // תוכן הפוסט
    public String userName;     // שם המשתמש המפרסם
    public String postDate;
    public String rating;// תאריך הפרסום
    public String imageBase64;

    public FeedPost() {
    }

    // יוצר פוסט חדש
    public FeedPost(String bookName, String authorName, String postContent, String userName, String postDate, String rating, String imageBase64) {
        this.bookName = bookName;
        this.authorName = authorName;
        this.postContent = postContent;
        this.userName = userName;
        this.postDate = postDate;
        this.rating = rating;
        this.imageBase64 = imageBase64;
    }
}
