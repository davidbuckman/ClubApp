package edu.upenn.cis350.clubapp;

import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
//        setSupportActionBar(toolbar); //TODO: figure out why this sits below

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
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("Num snapshot members = " + dataSnapshot.getChildrenCount());

                membersList = new ArrayList<ClubMember>((int)dataSnapshot.getChildrenCount());
                //get all channels for the club
                for (DataSnapshot snapshot : dataSnapshot.child("clubs").child(clubID).child("members").getChildren()){
                    System.out.println("\n members: " + snapshot.getKey());
                    System.out.println("isAdmin = " + snapshot.child("isAdmin").getValue());
                    System.out.println("title = " + snapshot.child("title").getValue());
                    boolean isAdmin = true;
                    if(snapshot.child("isAdmin").getValue().toString().equals("false")){
                        isAdmin = false;
                    }
                    membersList.add(new ClubMember(snapshot.getKey(), isAdmin , snapshot.child("title").getValue().toString()));

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


    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.NotificationViewHolder>{

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
        public RVAdapter.NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout_directory, parent, false);
            RVAdapter.NotificationViewHolder nvh = new RVAdapter.NotificationViewHolder(v);
            return nvh;
        }

        @Override
        public void onBindViewHolder(RVAdapter.NotificationViewHolder holder, int position) {
            holder.user.setText(memberList.get(position).getName());
            holder.position.setText(memberList.get(position).getTitle());
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
        public class NotificationViewHolder extends RecyclerView.ViewHolder{

            TextView user;
            TextView position;

            public NotificationViewHolder(View v) {
                super(v);
                user = (TextView) v.findViewById(R.id.user_name);
                position = (TextView) v.findViewById(R.id.user_position);
            }
        }
    }
}
