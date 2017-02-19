package edu.upenn.cis350.clubapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("DONE DONE DONE DDONE");
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}
