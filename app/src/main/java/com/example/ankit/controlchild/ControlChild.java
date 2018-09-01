package com.example.ankit.controlchild;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class ControlChild extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
