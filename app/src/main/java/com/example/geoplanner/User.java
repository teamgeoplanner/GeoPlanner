package com.example.geoplanner;

public class User {

    public String name, email;

    public User(){

    }

    //Constructor called
    //Set values for adding in database
    public User(String name, String email){
        this.name = name;
        this.email = email;
    }
}
