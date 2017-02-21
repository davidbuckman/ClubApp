package edu.upenn.cis350.clubapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


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


        //Getting reference to Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference mDatabaseReference = database.getReference();


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


        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = clubName.getText().toString().trim();
                String about = aboutClub.getText().toString().trim();
                String chanString = allChannels.getText().toString().trim();
                HashMap<String, Boolean> channels = new HashMap<String, Boolean>();




                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getApplicationContext(), "Enter club name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(about)) {
                    Toast.makeText(getApplicationContext(), "Enter club description!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(chanString)) {
                    //add general
                    channels.put("general", true);
                }else{
                    //parse string
                    String[] indivChannels = chanString.split(",");
                    for(String chan : indivChannels){
                        channels.put(chan.trim(), true);
                    }
                    channels.put("general", true);

                }

                //create
                Toast.makeText(getApplicationContext(), "Your club was made!", Toast.LENGTH_SHORT).show();

                Club newClub = new Club(name, about, auth.getCurrentUser().getUid());
                newClub.setChannels(channels);

                //add to database
                mDatabaseReference.child("clubs").child(name).setValue(newClub);

                //update user profile
                mDatabaseReference.child("users").child(auth.getCurrentUser().getUid()).child("clubs").child(name).setValue(true);

                //go back to main
                startActivity(new Intent(CreateClubActivity.this, MainActivity.class));


            }
        });


    }
}
