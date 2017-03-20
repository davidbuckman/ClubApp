package edu.upenn.cis350.clubapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class AnnouncementsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private HashSet<String> clubChannels;
    private HashSet<String> myChannels;
    private HashSet<ClubNotification> messages;

    //Getting reference to Firebase Database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    //firebase auth for user id
    FirebaseAuth auth = FirebaseAuth.getInstance();

    String clubID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);
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
//        navigationHeaderEmail.setText(auth.getCurrentUser().getEmail());

        // Initialize data structures
        clubChannels = new HashSet<String>();
        myChannels = new HashSet<String>();
        messages = new HashSet<ClubNotification>();

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
                //get all channels for the club
                for (DataSnapshot snapshot : dataSnapshot.child("clubs").child(clubID).child("channels").getChildren()){
                    System.out.println("\n channel: " + snapshot.getKey());
                    clubChannels.add(snapshot.getKey());
                }
                System.out.println("size of channel array= " + clubChannels.size());


                //look at club's channels to see if user is subscribed
                Iterator<String> iter = clubChannels.iterator();
                while (iter.hasNext()){
                    String channelID = iter.next();
                    for(DataSnapshot snapshot : dataSnapshot.child("channels").child(channelID).getChildren()){
                        System.out.println("\n curr channel: " + snapshot.getKey());
                        if(snapshot.getKey().toString().contentEquals("subscribers")){
                            for(DataSnapshot subshot : snapshot.getChildren()){
                                System.out.println("Sub to " + channelID + " = " + subshot.getKey());
                                if(subshot.getKey().equals(auth.getCurrentUser().getUid())){
                                    System.out.println("BINGO!");
                                    myChannels.add(channelID);
                                }
                            }
                        }
                    }
                }

                System.out.println("size of my chan array= " + myChannels.size());


                final Iterator<String> myIter = myChannels.iterator();
                System.out.println("we have iter? " + myIter.hasNext());
                if(!myIter.hasNext()){
                    Toast.makeText(AnnouncementsActivity.this,
                            "You have no new messages!",
                            Toast.LENGTH_LONG).show();
                }
                while (myIter.hasNext()){
                    String channel = myIter.next();
                    for(DataSnapshot snapshot : dataSnapshot.child("messages").child(channel).getChildren()){
                        ClubNotification newNotif =
                                new ClubNotification(
                                        snapshot.child("author").getValue(String.class),
                                        channel,
                                        snapshot.child("content").getValue(String.class),
                                        snapshot.child("timeStamp").getValue(Long.class));
                        messages.add(newNotif);
                    }

                }


                RVAdapter adapter = new RVAdapter(messages);
                mRecyclerView.setAdapter(adapter);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

        HashSet<ClubNotification> messageSet;
        ArrayList<ClubNotification> messages;

        RVAdapter(HashSet<ClubNotification> msg){
            System.out.println("ahhh");
            this.messageSet = msg;
            messages = new ArrayList(messageSet);
            for(ClubNotification notif : messages){
                System.out.println(notif.getAuthor());
            }
        }

        @Override
        public RVAdapter.NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false);
            RVAdapter.NotificationViewHolder nvh = new RVAdapter.NotificationViewHolder(v);
            return nvh;
        }

        @Override
        public void onBindViewHolder(RVAdapter.NotificationViewHolder holder, int position) {
            holder.author.setText(messages.get(position).getAuthor());
            holder.content.setText(messages.get(position).getContent());
            holder.channel.setText(messages.get(position).getChannel());
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        //ViewHolder for our Firebase UI
        public class NotificationViewHolder extends RecyclerView.ViewHolder{

            TextView author;
            TextView content;
            TextView channel;

            public NotificationViewHolder(View v) {
                super(v);
                author = (TextView) v.findViewById(R.id.message_author);
                content = (TextView) v.findViewById(R.id.message_content);
                channel = (TextView) v.findViewById(R.id.message_channel);
            }
        }
    }
}
