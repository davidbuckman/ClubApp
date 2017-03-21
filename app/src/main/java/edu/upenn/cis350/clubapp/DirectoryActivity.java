package edu.upenn.cis350.clubapp;

import android.content.ActivityNotFoundException;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeMap;

public class DirectoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    //global list
    ArrayList<ClubMember> membersList;


    //set up for recycler view
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Getting reference to Firebase Database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    //firebase auth for user id
    FirebaseAuth auth = FirebaseAuth.getInstance();

    String clubID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        mDatabaseReference.child("users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                navigationHeaderName.setText(dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue());
                navigationHeaderEmail.setText(dataSnapshot.child("email").getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // Initialize data structures
        membersList = new ArrayList<>(0);

        // Determine club name (club ID) via intent extras
        clubID = getIntent().getStringExtra("CLUB");
        System.out.println("\nclub name in act =  " + clubID);

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

        // maintains the user lists for the club
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("Num snapshot members = " + dataSnapshot.getChildrenCount());

                membersList = new ArrayList<ClubMember>((int)dataSnapshot.getChildrenCount());
                //get all channels for the club
                for (DataSnapshot snapshot : dataSnapshot.child("clubs").child(clubID).child("members").getChildren()){
                    System.out.println("\n members: " + snapshot.getKey());
                    System.out.println("isAdmin = " + snapshot.child("isAdmin").getValue().toString());
                    System.out.println("title = " + snapshot.child("title").getValue());
                    String title = "";
                    if (snapshot.child("title").getValue() != null) {
                        title = snapshot.child("title").getValue().toString();
                    }

                    String userId = "";
                    if (snapshot.child("userId").getValue() != null) {
                        userId = snapshot.child("userId").getValue().toString();
                    }

                    boolean isAdmin = true;
                    if(snapshot.child("isAdmin").getValue().toString().equals("false")){
                        isAdmin = false;
                    }
                    membersList.add(new ClubMember(snapshot.getKey(), userId, isAdmin , title));

                }

                if(membersList.isEmpty()){
                    Toast.makeText(DirectoryActivity.this,
                            "This club has no members!",
                            Toast.LENGTH_LONG).show();
                }

                System.out.println("memList:");
                RVAdapter adapter = new RVAdapter(membersList);
                mRecyclerView.setAdapter(adapter);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Intent i = new Intent();
        i.putExtra("CLUB", clubID);

        int id = item.getItemId();
        if (id == R.id.nav_information) {

        } else if (id == R.id.nav_announcements) {
            i.setClass(this, AnnouncementsActivity.class);

        } else if (id == R.id.nav_calendar) {

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


    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.DirectoryViewHolder>{

        ArrayList<ClubMember> memberList;

        RVAdapter(ArrayList<ClubMember> mem){
            System.out.println("In RVAdapter");
            this.memberList = mem;
            for(ClubMember cMember : memberList){
                System.out.println(cMember.getName());
            }
            Collections.sort(memberList);
        }

        @Override
        public RVAdapter.DirectoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout_directory, parent, false);
            RVAdapter.DirectoryViewHolder dvh = new RVAdapter.DirectoryViewHolder(v);
            return dvh;
        }

        @Override
        public void onBindViewHolder(RVAdapter.DirectoryViewHolder holder, int position) {
            holder.user.setText(memberList.get(position).getName());
            holder.position.setText(memberList.get(position).getTitle());
            holder.userId = memberList.get(position).getUserId();
        }

        @Override
        public int getItemCount() {
            return memberList.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        //ViewHolder for our Firebase UI
        public class DirectoryViewHolder extends RecyclerView.ViewHolder{

            String userId;
            TextView user;
            TextView position;
            Button emailLink;

            public DirectoryViewHolder(View v) {
                super(v);
                user = (TextView) v.findViewById(R.id.user_name);
                position = (TextView) v.findViewById(R.id.user_position);
                emailLink = (Button) v.findViewById(R.id.write_email);
                emailLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("MainActivity", "Click to email " + user.getText());
                        DatabaseReference mDB = database.getReference().child("users");
                        mDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //System.out.println("Look for: " + userId);

                                String emailTo = "";
                                String targetEmail = "";


                                //find member with coordinating ID
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    //System.out.println("Found: " + snapshot.getKey().toString());

                                    if(snapshot.getKey().toString().equals(userId)){

                                        //email this child
                                        System.out.println("email: " + snapshot.child("email").getValue());
                                        targetEmail = snapshot.child("email").getValue().toString();
                                        emailTo = "mailto:" + targetEmail;
                                        //break; //TODO: this is bad style
                                    }
                                }
                                System.out.println("emailing....");

                                if(targetEmail.isEmpty()){
                                    Toast.makeText(DirectoryActivity.this,
                                            "No email available! Sorry!",
                                            Toast.LENGTH_LONG).show();
                                } else{
//                                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
//                                    emailIntent.setData(Uri.parse(emailTo));
//                                    try{
//                                        startActivity(emailIntent);
//                                    } catch (ActivityNotFoundException e){
//                                        //TODO: Handle cases where no email app is available
//                                        Toast.makeText(DirectoryActivity.this,
//                                                "You have no email app available. " +
//                                                        "\nThe requested email is: " + targetEmail,
//                                                Toast.LENGTH_LONG).show();
//
//                                    }

                                    /* Create the Intent */
                                    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                                    /* Fill it with Data */
                                    emailIntent.setType("plain/text");
                                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{targetEmail});
                                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
                                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");

                                    /* Send it off to the Activity-Chooser */
                                    startActivity(Intent.createChooser(emailIntent, "Sending mail to: " + targetEmail));
                                }



                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                });



            }
        }
    }
}
