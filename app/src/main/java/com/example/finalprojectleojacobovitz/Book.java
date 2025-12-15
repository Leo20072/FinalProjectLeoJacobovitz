package com.example.finalprojectleojacobovitz;

public class Book {
    private String nameOfBook;
    private String authorsname;
    private String uploadPagesCount;
    private String uploadImageUrl;
    private String uploadCategory;
    private String uploadStartDate;
    private String pagesread;
    public boolean hasPost; // <--- המשתנה החדש

    public String getPagesread() {
        return pagesread;
    }

    public void setPagesread(String pagesread) {
        this.pagesread = pagesread;
    }

    public String getNameOfBook() {
        return nameOfBook;
    }

    public String getAuthorsname() {
        return authorsname;
    }

    public String getUploadPagesCount() {
        return uploadPagesCount;
    }

    public String getUploadCategory() {
        return uploadCategory;
    }

    public String getUploadStartDate() {
        return uploadStartDate;
    }

    public String getUploadImageUrl() {
        return uploadImageUrl;
    }

    public void setNameOfBook(String nameOfBook) {
        this.nameOfBook = nameOfBook;
    }

    public void setAuthorsname(String authorsname) {
        this.authorsname = authorsname;
    }

    public void setUploadPagesCount(String uploadPagesCount) {
        this.uploadPagesCount = uploadPagesCount;
    }

    public void setUploadImageUrl(String uploadImageUrl) {
        this.uploadImageUrl = uploadImageUrl;
    }

    public void setUploadCategory(String uploadCategory) {
        this.uploadCategory = uploadCategory;
    }

    public void setUploadStartDate(String uploadStartDate) {
        this.uploadStartDate = uploadStartDate;
    }

    public boolean isHasPost() {
        return hasPost;
    }

    public void setHasPost(boolean hasPost) {
        this.hasPost = hasPost;
    }

    public Book(){
    }
    public Book(String nameOfBook, String authorsname, String uploadPagesCount, String uploadImageUrl, String uploadCategory, String uploadStartDate, String pagesread) {
        this.nameOfBook = nameOfBook;
        this.authorsname = authorsname;
        this.uploadPagesCount = uploadPagesCount;
        this.uploadImageUrl = uploadImageUrl;
        this.uploadCategory = uploadCategory;
        this.uploadStartDate = uploadStartDate;
        this.pagesread = pagesread;
        this.hasPost = false;
    }

}