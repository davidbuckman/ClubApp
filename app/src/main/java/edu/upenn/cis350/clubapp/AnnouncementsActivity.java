package edu.upenn.cis350.clubapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class AnnouncementsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //global data structures
    private HashSet<String> usersChannels;
    private TreeSet<ClubNotification> messages; //leverage implicit ordering using custom comparator

    //Getting reference to Firebase Database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    //firebase auth for user id
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

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
            i.setClass(this, CalendarActivity.class);

        } else if (id == R.id.nav_directory) {
            i.setClass(this, DirectoryActivity.class);

        } else if (id == R.id.nav_club_settings) {
            i.setClass(this, ClubSettingsActivity.class);
        }

        startActivity(i);
        // Calling finish after startActivity causes the announcement activity to break intent stack
        // by finishing when its child intent finishes.
     //   finish();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Determine club name (club ID) via intent extras
        clubID = getIntent().getStringExtra("CLUB");
        System.out.println("\nclub name in act =  " + clubID);

        // If club name is null, send back to main page
        if (clubID == null) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
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

        //set up refresh button
        final FloatingActionButton refresh = (FloatingActionButton) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                startActivity(getIntent().putExtra("CLUB", clubID));
            }
        });


        //button for admins to add announcements
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        mDatabaseReference.child("clubs").child(clubID).child("members").child(user.getUid()).child("isAdmin")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean isAdmin = (boolean) dataSnapshot.getValue();
                        if (isAdmin) {
                            fab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(AnnouncementsActivity.this, AddAnnouncementActivity.class);
                                    i.putExtra("CLUB", clubID);
                                    startActivity(i);
                                }
                            });
                        } else {
                            fab.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
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


        /****************
         * Get all of the messages that the user needs to see and pass to viewer
         ***************/

        // Initialize data structures
        usersChannels = new HashSet<String>();
        messages = new TreeSet<ClubNotification>();

        //get data and display
        final DatabaseReference ref = mDatabaseReference.child("clubs").child(clubID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //populate userChannels with list of channels this user is subscribed to
                for (DataSnapshot snapshot : dataSnapshot.child("members").child(user.getUid()).child("channels").getChildren()) {
                    System.out.println("\n   channel: " + snapshot.getKey());
                    usersChannels.add(snapshot.getKey().toString());
                }
                System.out.println("size of channel array= " + usersChannels.size());

                //check admin for delete announcement button
                final boolean isAdmin = dataSnapshot.child("members").child(user.getUid()).child("isAdmin").getValue(boolean.class);

                //get messages for user's channels
                for (DataSnapshot snapshot : dataSnapshot.child("channels").getChildren()) {
                    //determine if current channel is in the user's list
                    System.out.println("current channel is: " + snapshot.getKey().toString());
                    if (usersChannels.contains(snapshot.getKey().toString())) {
                        for (DataSnapshot subShot : snapshot.getChildren()) {
                            //add this channel's messages to the set to display
                            System.out.println("adding messages from " + snapshot.getKey().toString());
                            ClubNotification newNotif =
                                    new ClubNotification(
                                            subShot.child("title").getValue(String.class),
                                            snapshot.getKey().toString(), //channel
                                            subShot.child("body").getValue(String.class),
                                            Long.parseLong(subShot.getKey()), //timestamp
                                            ref.child("channels").child(snapshot.getKey().toString()).child(subShot.getKey()),
                                            isAdmin);
                            messages.add(newNotif);
                            System.out.println("in method number of notifications is " + messages.size());


                        }

                    }
                }

                System.out.println("number of notifications is " + messages.size());

                //pass to adapter
                RVAdapter adapter = new RVAdapter(messages);
                mRecyclerView.setAdapter(adapter);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }


    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.NotificationViewHolder> {
        ArrayList<ClubNotification> messages;

        RVAdapter(TreeSet<ClubNotification> msg) {
            messages = new ArrayList(msg);


        }

        @Override
        public RVAdapter.NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false);
            RVAdapter.NotificationViewHolder nvh = new RVAdapter.NotificationViewHolder(v);
            return nvh;
        }

        @Override
        public void onBindViewHolder(RVAdapter.NotificationViewHolder holder, final int position) {
            holder.title.setText(messages.get(position).getTitle());
            holder.body.setText(messages.get(position).getBody());
            holder.channel.setText(messages.get(position).getChannel());
            if (messages.get(position).isAdmin()) {
                holder.deleteAnnouncement.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        messages.get(position).getRef().removeValue();
                    }
                });
            } else {
                holder.deleteAnnouncement.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public class NotificationViewHolder extends RecyclerView.ViewHolder {

            TextView title;
            TextView body;
            TextView channel;
            Button deleteAnnouncement;

            public NotificationViewHolder(View v) {
                super(v);
                title = (TextView) v.findViewById(R.id.message_title);
                body = (TextView) v.findViewById(R.id.message_body);
                channel = (TextView) v.findViewById(R.id.message_channel);
                deleteAnnouncement = (Button) v.findViewById(R.id.delete_announcement_button);
            }
        }
    }
}



