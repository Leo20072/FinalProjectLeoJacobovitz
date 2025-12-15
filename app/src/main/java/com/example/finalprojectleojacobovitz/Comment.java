package com.example.finalprojectleojacobovitz;

public class Comment {
    public String content;      // תוכן התגובה
    public String userId;       // מזהה המשתמש (כדי שנוכל לזהות מי כתב)
    public String userName;     // שם המשתמש לתצוגה
    public String timestamp;    // מתי נכתבה

    public Comment() {
    }

    public Comment(String content, String userId, String userName, String timestamp) {
        this.content = content;
        this.userId = userId;
        this.userName = userName;
        this.timestamp = timestamp;
    }
}
