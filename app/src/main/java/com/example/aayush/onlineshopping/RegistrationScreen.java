package com.example.aayush.onlineshopping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import dbs.DAOs;
import dbs.Databases;
import dbs.Entities;
import misc.PasswordOps;

import static xdroid.toaster.Toaster.toast;

public class RegistrationScreen extends AppCompatActivity {
    private Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_screen);
        setTitle("Register");

        extras = getIntent().getExtras();
    }

    @Override
    public void onBackPressed() {
        Intent goBack = new Intent(this, WelcomeScreen.class);
        startActivity(goBack);
    }

    public void registerClick(View view){
        assert extras != null;
        int source = extras.getInt("source");
        Context current = getApplicationContext();

        String name, pw1, pw2, email, address;
        name = ((EditText) findViewById(R.id.name)).getText().toString();
        pw1 = ((EditText) findViewById(R.id.vendPW1)).getText().toString();
        pw2 = ((EditText) findViewById(R.id.vendPW2)).getText().toString();
        email = ((EditText) findViewById(R.id.email)).getText().toString();
        address = ((EditText) findViewById(R.id.address)).getText().toString();

        if (name == null || pw1 == null || pw2 == null || email == null || address == null) {
            Toast.makeText(current, "Fields cannot be blank!", Toast.LENGTH_SHORT).show();
        }

        if (source == 1) {
            VThread vThread = new VThread(name, address, email, pw1, pw2, this);
            vThread.start();
        } else {
            UThread uThread = new UThread(name, address, email, pw1, pw2, this);
            uThread.start();
        }
    }
}

class VThread extends Thread {
    private String email;
    private String pw1;
    private String pw2;
    private String name;
    private String address;
    private Activity activity;

    VThread(String name, String address, String emailId, String pw1, String pw2, Activity activity){
        this.name = name;
        this.address = address;
        this.email = emailId;
        this.pw1 = pw1;
        this.pw2 = pw2;
        this.activity = activity;
    }

    @Override
    public void run(){
        DAOs.VendorDAO vendorAcc;
        Databases.VendorDatabase db = Databases.VendorDatabase.getVendorDatabase(activity);
        vendorAcc = db.vendorDAO();
        Entities.VendorEntity vendor = new Entities.VendorEntity();

        if (vendorAcc.isUnique(email) == null) {
            toast("Email must be unique");
        } else if (!pw1.equals(pw2)) {
            toast("Passwords do not match");
        } else {
            byte[] salt = new byte[0];
            try {
                salt = PasswordOps.getSalt();
            } catch (NoSuchAlgorithmException e) {
                Log.e("exception", "NoSuchAlgorithm");
            } catch (NoSuchProviderException e) {
                Log.e("exception", "NoSuchProvider");
            }
            String hashedPW = PasswordOps.getSecurePassword(pw1, salt);
            vendor.setName(name);
            vendor.setAddress(address);
            vendor.setEmailId(email);
            vendor.setPassword(hashedPW);
            vendor.setSalt(new String(salt));
            vendorAcc.insertVendor(vendor);

            Cursor vend = vendorAcc.getIdByNameAndEmail(name, email);
            int id = 0;
            if(vend.moveToFirst()){
                id = vend.getInt(vend.getColumnIndex("id"));
                vend.close();
            }

            Intent moveOn = new Intent(activity, CardDetails.class);
            moveOn.putExtra("id", id);
            activity.startActivity(moveOn);
        }
    }
}

class UThread extends Thread {
    private Activity current;
    private String email;
    private String pw1;
    private String pw2;
    private String name;
    private String address;

    UThread(String name, String address, String emailId, String pw1, String pw2, Activity current){
        this.name = name;
        this.address = address;
        this.email = emailId;
        this.pw1 = pw1;
        this.pw2 = pw2;
        this.current = current;
    }

    @Override
    public void run(){
        DAOs.UserDAO userAcc;
        Databases.UserDatabase db = Databases.UserDatabase.getUserDatabase(current);
        userAcc = db.userDAO();
        Entities.UserEntity user = new Entities.UserEntity();

        if (userAcc.isUnique(email) == null) {
            toast("Email must be unique");
        } else if (!pw1.equals(pw2)) {
            toast("Passwords must match");
        } else {
            byte[] salt = new byte[0];
            try {
                salt = PasswordOps.getSalt();
            } catch (NoSuchAlgorithmException e) {
                Log.e("exception", "NoSuchAlgorithm");
            } catch (NoSuchProviderException e) {
                Log.e("exception", "NoSuchProvider");
            }
            String hashedPW = PasswordOps.getSecurePassword(pw1, salt);
            user.setName(name);
            user.setAddress(address);
            user.setEmailId(email);
            user.setPassword(hashedPW);
            user.setSalt(new String(salt));
            userAcc.insertUser(user);

            Cursor cursor = userAcc.getIdByNameAndEmail(name, email);
            int id = 0;
            if(cursor.moveToFirst()){
                id = cursor.getInt(cursor.getColumnIndex("id"));
                cursor.close();
            }
            Intent moveOn = new Intent(current, CardDetails.class);
            moveOn.putExtra("id", id);
            current.startActivity(moveOn);
        }
    }
}
