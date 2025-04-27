package com.worksy.data.model;

public class User {
    String email;
    String password;
    String role;

    int UserID;

    public User(int UserID, String email, String role, String password) {
        this.UserID = UserID;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public int getUserID() {
        return UserID;
    }

    public String getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }
}
