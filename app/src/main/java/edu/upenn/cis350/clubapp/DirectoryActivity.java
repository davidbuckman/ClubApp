package edu.upenn.cis350.clubapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;

import static android.R.attr.data;
import static android.R.attr.value;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class DirectoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    //global list
    HashMap<String, ClubMember> membersMap;
    HashMap<String, User> usersMap;

    private static Context mContext;

    //set up for recycler view
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;


    //Getting reference to Firebase Database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    //firebase auth for user id
    FirebaseAuth auth = FirebaseAuth.getInstance();
    // get if current user is an administrator
    boolean currentUserIsAdmin = false;



    String clubID;

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
// Handle navigation view item clicks here.
        Intent i = new Intent();
        i.putExtra("CLUB", clubID);

        int id = item.getItemId();
        if (id == R.id.nav_information) {
            i.setClass(this, InformationActivity.class);

        } else if (id == R.id.nav_announcements) {
            i.setClass(this, AnnouncementsActivity.class);

        } else if (id == R.id.nav_calendar) {
            i.setClass(this, InformationActivity.class);

        } else if (id == R.id.nav_directory) {
            i.setClass(this, DirectoryActivity.class);

        } else if (id == R.id.nav_club_settings) {
            i.setClass(this, ClubSettingsActivity.class);
        }

        startActivity(i);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mContext = this;


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set navigation header information to current user
        final TextView navigationHeaderName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
        final TextView navigationHeaderEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);
        mDatabaseReference.child("users").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("1-- enter header change");
                navigationHeaderName.setText(dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue());
                navigationHeaderEmail.setText(dataSnapshot.child("email").getValue(String.class));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // Initialize data structures
        membersMap = new HashMap<>(0);

        // Determine club name (club ID) via intent extras
        clubID = getIntent().getStringExtra("CLUB");
        System.out.println("\nclub name in act =  " + clubID);
        Log.d("test location", "getting club id from intent");

        // If club name is null, send back to main page
        if (clubID == null){
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }

        //set up for display
        mRecyclerView = (RecyclerView) findViewById(R.id.notification_recycler_view);

        if (mRecyclerView != null) {
            //to enable optimization of recyclerview
            mRecyclerView.setHasFixedSize(true);
        }

        //using staggered grid pattern in recyclerview
        mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //get data and display
        DatabaseReference ref = mDatabaseReference;


        mDatabaseReference.child("clubs").child(clubID).child("members").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserIsAdmin = dataSnapshot.child("isAdmin").getValue(Boolean.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        final FirebaseRecyclerAdapter<ClubMember, ClubMemberViewHolder> adapter = new FirebaseRecyclerAdapter<ClubMember, ClubMemberViewHolder>(
                ClubMember.class,
                R.layout.card_layout_directory,
                ClubMemberViewHolder.class,
                ref.child("clubs").child(clubID).child("members").orderByChild("isAdmin").getRef()
        ) {
            @Override
            protected void populateViewHolder(final ClubMemberViewHolder viewHolder, final ClubMember model, int position) {
                // key is UID
                final String key = this.getRef(position).getKey();
                final String title = model.getTitle();

                System.out.println("CURRENT USER IS " + currentUserIsAdmin + " admin");

                mDatabaseReference.child("clubs").child(clubID).child("members").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (!dataSnapshot.child("isAdmin").getValue(Boolean.class)) {
                                viewHolder.editUser.setVisibility(View.GONE);
                            } else {
                                viewHolder.editUser.setVisibility(View.VISIBLE);
                            }
                        }
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mDatabaseReference.child("users").child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String fname = dataSnapshot.child("firstName").getValue(String.class);
                        String lname = dataSnapshot.child("lastName").getValue(String.class);
                        String name = fname + " " + lname;
                        System.out.println("NAME IS: " + name);

                        String email = dataSnapshot.child("email").getValue(String.class);



                        viewHolder.clubID = clubID;

                        viewHolder.user.setText(name);
                        viewHolder.email = email;
                        viewHolder.position.setText(title);
                        viewHolder.uid = key;

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mRecyclerView.setAdapter(adapter);

    }
    public static class ClubMemberViewHolder extends RecyclerView.ViewHolder {
        String email;
        String uid;
        TextView user;
        TextView position;
        Button editUser;
        Button emailLink;
        String clubID;

        public ClubMemberViewHolder(View v) {
            super(v);
            emailLink = (Button) v.findViewById(R.id.write_email);
            editUser = (Button) v.findViewById(R.id.edit_user);
            user = (TextView) v.findViewById(R.id.user_name);
            position = (TextView) v.findViewById(R.id.user_position);


            editUser.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    final Intent intent = new Intent(mContext, EditClubUserActivity.class);
                    intent.putExtra("CLUB", clubID);
                    intent.putExtra("USER", uid);
                    mContext.startActivity(intent);
                }
            });


            emailLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("MainActivity", "Click to email " + user.getText());

                    if(email.isEmpty()){
                        Toast.makeText(mContext,
                                "No email available! Sorry!",
                                Toast.LENGTH_LONG).show();
                    } else{
                        String emailTo = "mailto:" + email;
                        System.out.println("sending email to emailTo");

                /* Create the Intent */
                        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                /* Fill it with Data */
                        emailIntent.setType("plain/text");
                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
                        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");

                /* Send it off to the Activity-Chooser */
                        mContext.startActivity(Intent.createChooser(emailIntent, "Sending mail to: " + email));
                    }

                }
            });
        }
    }
}
