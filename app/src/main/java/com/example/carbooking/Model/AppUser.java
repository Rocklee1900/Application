package com.example.carbooking.Model;

public class AppUser {
    public String user_email;
    public String user_id;
    public String user_role;
    public String firstName;
    public String lastName;
    public String phoneNumber;

    // Required empty constructor for Firestore
    public AppUser() {}

    public AppUser(String email, String uid, String role, String firstName, String lastName, String phone) {
        this.user_email = email;
        this.user_id = uid;
        this.user_role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phone;
    }

    public AppUser(String email, String role, String name, String phoneNumber) {
    }
}

