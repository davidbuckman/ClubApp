package edu.upenn.cis350.clubapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;

public class CreateClubActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    //buttons
    Button btnCancel, btnCreate;
    EditText clubName, aboutClub, allChannels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_club);


        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        clubName = (EditText) findViewById(R.id.new_club_name);
        aboutClub = (EditText) findViewById(R.id.about_club);
        allChannels = (EditText) findViewById(R.id.club_channels);
        btnCancel = (Button) findViewById(R.id.cancel_create_club_button);
        btnCreate = (Button) findViewById(R.id.create_club_button);


        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CreateClubActivity.this, MainActivity.class));
            }
        });


    }
}
