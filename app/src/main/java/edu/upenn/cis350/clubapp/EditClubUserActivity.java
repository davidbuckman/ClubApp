package edu.upenn.cis350.clubapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EditClubUserActivity extends AppCompatActivity {

    String clubID;
    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        clubID = getIntent().getStringExtra("CLUB");
        userID = getIntent().getStringExtra("USER");

        System.out.println("USER ID IS " + userID);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_club_user);
    }
}
