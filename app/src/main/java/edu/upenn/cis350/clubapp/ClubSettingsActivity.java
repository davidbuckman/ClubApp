package edu.upenn.cis350.clubapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by david on 3/6/2017.
 */

public class ClubSettingsActivity extends AppCompatActivity {

    private EditText userEmail;
    private Button btnInviteUser;
    private Button inviteUser;

    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    String clubID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_settings);

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

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //Getting reference to Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference mDatabaseReference = database.getReference();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(ClubSettingsActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        userEmail = (EditText) findViewById(R.id.user_email);
        btnInviteUser = (Button) findViewById(R.id.invite_user_button);
        inviteUser = (Button) findViewById(R.id.inviteUser);

        userEmail.setVisibility(View.GONE);
        inviteUser.setVisibility(View.GONE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        btnInviteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userEmail.setVisibility(View.VISIBLE);
                inviteUser.setVisibility(View.VISIBLE);
            }
        });

        inviteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = userEmail.getText().toString().trim();
                if (isAdmin(user, clubID) && !email.equals("")) {
                    mDatabaseReference.child("users").orderByChild("email").equalTo(email)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getChildrenCount() > 1) {
                                        Log.e("ClubSettingsActivity", "Multiple users with same email");
                                        Toast.makeText(ClubSettingsActivity.this, "Whoops! There are multiple registered users with this email address. Please let our team know!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    } else if (dataSnapshot.getChildrenCount() == 0) {
                                        Toast.makeText(ClubSettingsActivity.this, "There is no user with that email!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        String uid = dataSnapshot.getChildren().iterator().next().getKey();
                                        // Update user's invited to list
                                        mDatabaseReference.child("users").child(uid).child("invitations").child(clubID).setValue(true);

                                        Toast.makeText(ClubSettingsActivity.this, "User has been invited!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                } else if (!isAdmin(user, clubID)) {
                    Toast.makeText(ClubSettingsActivity.this, "Only admins can invite users!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else if (email.equals("")) {
                    userEmail.setError("Enter an email");
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(ClubSettingsActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public boolean isAdmin(FirebaseUser user, String clubID) {
        // TODO
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

}