package com.example.ankit.controlchild.Model;

public class User {

    public String email;
    public String password;
    public String name;
    public String phone;
    public String uniqueID;

    public User() {
    }

    public User(String email, String password, String name, String phone, String uniqueID) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.uniqueID = uniqueID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }
}
