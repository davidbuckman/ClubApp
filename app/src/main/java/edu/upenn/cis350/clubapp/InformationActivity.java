package edu.upenn.cis350.clubapp;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class InformationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    //Getting reference to Firebase Database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    //firebase auth for user id
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    //current club name
    String clubID;

    //string for about
    String clubInfo;

    //set up for recycler view
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Intent i = new Intent();
        i.putExtra("CLUB", clubID);

        int id = item.getItemId();
        if (id == R.id.nav_information){
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
        finish();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        // Determine club name (club ID) via intent extras
        clubID = getIntent().getStringExtra("CLUB");
        System.out.println("\nclub name in info act =  " + clubID);

        // If club name is null, send back to main page
        if (clubID == null){
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }


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
                System.out.println("setting personal info");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        //set up for display
        mRecyclerView = (RecyclerView) findViewById(R.id.notification_recycler_view);

        if (mRecyclerView != null) {
            //to enable optimization of recyclerview
            mRecyclerView.setHasFixedSize(true);
        }

        //using staggered grid pattern in recyclerview
        mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //get reference
        mDatabaseReference.child("clubs").child(clubID).child("about").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                clubInfo = dataSnapshot.getValue().toString();
                System.out.println(clubInfo);

                InfoAdapter adapter = new InfoAdapter(clubInfo);
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


    //recycler
    public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.InformationViewHolder>{

        String aboutClub;

        InfoAdapter(String a){
            this.aboutClub = a;
        }

        @Override
        public InformationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout_information, parent, false);
            InfoAdapter.InformationViewHolder ivh = new InfoAdapter.InformationViewHolder(v);
            return ivh;
        }

        @Override
        public void onBindViewHolder(InformationViewHolder holder, int position) {
            holder.about.setText(aboutClub);
        }

        @Override
        public int getItemCount() {
            return 1;
        }


        public class InformationViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView about;

            public InformationViewHolder(View v){
                super(v);
                //name = (TextView) v.findViewById(R.id.club_name);
                about = (TextView) v.findViewById(R.id.club_information);

            }
        }
    }


}
