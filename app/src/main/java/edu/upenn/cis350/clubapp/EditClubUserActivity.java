package edu.upenn.cis350.clubapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static edu.upenn.cis350.clubapp.InvitationsActivity.auth;
import static edu.upenn.cis350.clubapp.InvitationsActivity.mDatabaseReference;
import static edu.upenn.cis350.clubapp.R.id.adminBox;
import static edu.upenn.cis350.clubapp.R.id.club_description;
import static edu.upenn.cis350.clubapp.R.id.inviteUser;

public class EditClubUserActivity extends AppCompatActivity {

    String clubID;
    String userID;
    private EditText userTitle;
    private Button editUser;
    private CheckBox adminBox;
    private Button removeUser;
    private Button editUserButton;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        clubID = getIntent().getStringExtra("CLUB");
        userID = getIntent().getStringExtra("USER");

        auth = FirebaseAuth.getInstance();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference mDatabaseReference = database.getReference();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_club_user);

        clubID = getIntent().getStringExtra("CLUB");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        userTitle = (EditText) findViewById(R.id.user_title);
        editUser = (Button) findViewById(R.id.editUser);
        adminBox = (CheckBox) findViewById(R.id.adminBox);
        removeUser = (Button) findViewById(R.id.remove_user_button);

        editUserButton = (Button) findViewById(R.id.edit_user_button);

        userTitle.setVisibility(View.GONE);
        editUser.setVisibility(View.GONE);
        adminBox.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        editUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userTitle.setVisibility(View.VISIBLE);
                editUser.setVisibility(View.VISIBLE);
                adminBox.setVisibility(View.VISIBLE);
                mDatabaseReference.child("clubs").child(clubID).child("members").child(userID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                adminBox.setChecked(Boolean.parseBoolean(dataSnapshot.child("isAdmin").getValue().toString()));
                                userTitle.setText(dataSnapshot.child("title").getValue().toString());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }
        });

        removeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseReference.child("clubs").child(clubID).child("members").child(userID).removeValue();
                mDatabaseReference.child("users").child(userID).child("clubs").child(clubID).removeValue();
                Toast.makeText(EditClubUserActivity.this, "removed user", Toast.LENGTH_LONG).show();
            }
        });
        editUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                // check if admin status is checked.
                final boolean adminStatus = adminBox.isChecked();

                mDatabaseReference.child("clubs").child(clubID).child("members").child(userID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String uid = dataSnapshot.getChildren().iterator().next().getKey();

                                String title = userTitle.getText().toString().trim();
                                System.out.println("USER TITLE = " + title);

                                mDatabaseReference.child("clubs").child(clubID).child("members").child(userID).child("isAdmin").setValue(adminStatus);

                                if (title.isEmpty()) {
                                    System.out.println("using default");
                                    mDatabaseReference.child("clubs").child(clubID).child("members").child(userID).child("title").setValue("General Member");
                                } else {
                                    System.out.println("using: " + title);
                                    mDatabaseReference.child("clubs").child(clubID).child("members").child(userID).child("title").setValue(title);
                                }


                                Toast.makeText(EditClubUserActivity.this, "User has been edited!", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);

                            }

                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });


        }
    });

    }
}
