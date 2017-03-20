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
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Iterator;

public class DirectoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

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
//        setSupportActionBar(toolbar);

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
//        navigationHeaderEmail.setText(auth.getCurrentUser().getEmail());

        // Initialize data structures


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



//        //get data and display
//        DatabaseReference ref = mDatabaseReference;
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                //get all channels for the club
//                for (DataSnapshot snapshot : dataSnapshot.child("clubs").child(clubID).child("channels").getChildren()){
//                    System.out.println("\n channel: " + snapshot.getKey());
//                    clubChannels.add(snapshot.getKey());
//                }
//                System.out.println("size of channel array= " + clubChannels.size());
//
//
//                //look at club's channels to see if user is subscribed
//                Iterator<String> iter = clubChannels.iterator();
//                while (iter.hasNext()){
//                    String channelID = iter.next();
//                    for(DataSnapshot snapshot : dataSnapshot.child("channels").child(channelID).getChildren()){
//                        System.out.println("\n curr channel: " + snapshot.getKey());
//                        if(snapshot.getKey().toString().contentEquals("subscribers")){
//                            for(DataSnapshot subshot : snapshot.getChildren()){
//                                System.out.println("Sub to " + channelID + " = " + subshot.getKey());
//                                if(subshot.getKey().equals(auth.getCurrentUser().getUid())){
//                                    System.out.println("BINGO!");
//                                    myChannels.add(channelID);
//                                }
//                            }
//                        }
//                    }
//                }
//
//                System.out.println("size of my chan array= " + myChannels.size());
//
//
//                final Iterator<String> myIter = myChannels.iterator();
//                System.out.println("we have iter? " + myIter.hasNext());
//                if(!myIter.hasNext()){
//                    Toast.makeText(AnnouncementsActivity.this,
//                            "You have no new messages!",
//                            Toast.LENGTH_LONG).show();
//                }
//                while (myIter.hasNext()){
//                    String channel = myIter.next();
//                    for(DataSnapshot snapshot : dataSnapshot.child("messages").child(channel).getChildren()){
//                        ClubNotification newNotif =
//                                new ClubNotification(
//                                        snapshot.child("author").getValue(String.class),
//                                        channel,
//                                        snapshot.child("content").getValue(String.class),
//                                        snapshot.child("timeStamp").getValue(Long.class));
//                        messages.add(newNotif);
//                    }
//
//                }
//
//
//                AnnouncementsActivity.RVAdapter adapter = new AnnouncementsActivity.RVAdapter(messages);
//                mRecyclerView.setAdapter(adapter);
//
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

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
}
