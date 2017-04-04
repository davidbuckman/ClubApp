package edu.upenn.cis350.clubapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
    private EditText userTitle;
    private Button btnInviteUser;
    private Button inviteUser;
    private Button leaveGroup;
    private Button deleteGroup;
    private CheckBox adminBox;
    private boolean adminAuth = false;

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

        // update admin status
        isAdmin(user, clubID);

        userEmail = (EditText) findViewById(R.id.user_email);
        userTitle = (EditText) findViewById(R.id.user_title);
        btnInviteUser = (Button) findViewById(R.id.invite_user_button);
        inviteUser = (Button) findViewById(R.id.inviteUser);
        leaveGroup = (Button) findViewById(R.id.leave_club_button);
        deleteGroup = (Button) findViewById(R.id.delete_club_button);
        adminBox = (CheckBox) findViewById(R.id.adminBox);

        userEmail.setVisibility(View.GONE);
        userTitle.setVisibility(View.GONE);
        inviteUser.setVisibility(View.GONE);
        adminBox.setVisibility(View.GONE);


        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        btnInviteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userEmail.setVisibility(View.VISIBLE);
                userTitle.setVisibility(View.VISIBLE);
                inviteUser.setVisibility(View.VISIBLE);
                adminBox.setVisibility(View.VISIBLE);
            }
        });

        inviteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = userEmail.getText().toString().trim();

                // check if admin status is checked.
                final boolean adminStatus = adminBox.isChecked();

                //get title
                //String title = userTitle.getText().toString().trim();
                //System.out.println("THE USER TITLE = " + title);


                if (!email.equals("")) {
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

                                        final String inviteeUid = dataSnapshot.getChildren().iterator().next().getKey();
                                        mDatabaseReference.child("clubs").child(clubID).child("members").child(user.getUid()).child("isAdmin")
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        boolean isAdmin = (boolean) dataSnapshot.getValue();
                                                        if (isAdmin) {
                                                            // Update user's invited to list
                                                            mDatabaseReference.child("users").child(inviteeUid).child("invitations").child(clubID).child("isAdmin").setValue(adminStatus);

                                                            String title = userTitle.getText().toString().trim();
                                                            System.out.println("USER TITLE = " + title);

                                                            if(title.isEmpty()){
                                                                System.out.println("using default");
                                                                mDatabaseReference.child("users").child(inviteeUid).child("invitations").child(clubID).child("title").setValue("General Member");
                                                            } else {
                                                                System.out.println("using: " + title);
                                                                mDatabaseReference.child("users").child(inviteeUid).child("invitations").child(clubID).child("title").setValue(title);
                                                            }
                                                            Toast.makeText(ClubSettingsActivity.this, "User has been invited!", Toast.LENGTH_LONG).show();
                                                            progressBar.setVisibility(View.GONE);
                                                        } else {
                                                            Toast.makeText(ClubSettingsActivity.this, "Only admins can invite users!", Toast.LENGTH_SHORT).show();
                                                            progressBar.setVisibility(View.GONE);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                } else if (email.equals("")) {
                    userEmail.setError("Enter an email");
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(ClubSettingsActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        leaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseReference.child("users").child(user.getUid()).child("clubs").child(clubID).removeValue();
                mDatabaseReference.child("clubs").child(clubID).child("members").child(user.getUid()).removeValue();
                Intent intent = new Intent(ClubSettingsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mDatabaseReference.child("clubs").child(clubID).child("members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        boolean isAdmin = dataSnapshot.child(user.getUid()).child("isAdmin").getValue(Boolean.class);
                        if (isAdmin) {
                            deleteGroup.setVisibility(View.VISIBLE);
                            deleteGroup.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    for (DataSnapshot member : dataSnapshot.getChildren()) {
                                        String memberID = member.getKey();
                                        mDatabaseReference.child("users").child(memberID).child("clubs").child(clubID).removeValue();
                                    }
                                    mDatabaseReference.child("clubs").child(clubID).removeValue();
                                    finish();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
  
  // I got rid of isAdmin due to asynchronousness making it too complicated and just built the
  // admin check into the function itself. Leaving your todo but currently this function is useless -dcb

    public boolean isAdmin(final FirebaseUser user, final String clubID) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference mDatabaseReference = database.getReference();
        //get data and display
        DatabaseReference ref = mDatabaseReference;

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            //mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("auth", "old adminAuth:" + adminAuth);
                //get all members for the club
                adminAuth = (boolean) dataSnapshot.child("clubs").child(clubID).child("members").child(user.getUid()).child("isAdmin").getValue();
                Log.d("auth", "new adminAuth:" + adminAuth);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // TODO
        return adminAuth;
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
