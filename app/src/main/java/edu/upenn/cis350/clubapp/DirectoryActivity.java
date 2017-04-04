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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeMap;

public class DirectoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    //global list
    HashMap<String, ClubMember> membersMap;


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

         //maintains the user lists for the club
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
        //mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("2-- Num snapshot members = " + dataSnapshot.getChildrenCount());

                //membersList = new ArrayList<ClubMember>((int)dataSnapshot.getChildrenCount());

                //get all members for the club
                for (DataSnapshot snapshot : dataSnapshot.child("clubs").child(clubID).child("members").getChildren()){
                    System.out.println("\n id: " + snapshot.getKey());
                    System.out.println("isAdmin = " + snapshot.child("isAdmin").getValue().toString());
                    System.out.println("title = " + snapshot.child("title").getValue().toString());
                    //TODO(jenna) figure out if this is filled in by default
                    String title = "";
                    if (snapshot.child("title").getValue() != null) {
                        title = snapshot.child("title").getValue().toString();
                    }

                    //defualt admin permission to false, chnge if its true
                    boolean isAdmin = false;
                    if(snapshot.child("isAdmin").getValue().toString().toLowerCase().equals("true")){
                        isAdmin = true;
                    }
                    membersMap.put(snapshot.getKey(), new ClubMember(isAdmin, title));
                    System.out.println("added person with title " + title);
                    //membersList.add(new ClubMember(snapshot.getKey(), isAdmin , title));

                }



                //maps from user to their title in the club
                HashMap<User, String> admins = new HashMap<>();
                HashMap<User, String> genUsers = new HashMap<>();


                if(membersMap.isEmpty()){
                    // TODO: is this toast ever displayed? lol no
                    Toast.makeText(DirectoryActivity.this,
                            "This club has no members!",
                            Toast.LENGTH_LONG).show();
                } else{
                    System.out.println("members map");
                    for(String key : membersMap.keySet()){
                        System.out.println(key + ":: "+ membersMap.get(key).getTitle());
                        System.out.println(key + "::: "+ membersMap.get(key));
                    }



                    //get user object for each member and map to id
                    System.out.println("3-- entered snapshot listener to get users from ids");
                    System.out.println("num users = " + dataSnapshot.child("users").getChildrenCount());

                    for(DataSnapshot userSnap : dataSnapshot.child("users").getChildren()){
                        String currID = userSnap.getKey().toString();
                        if(membersMap.keySet().contains(userSnap.getKey().toString())){
                            System.out.println(userSnap.child("firstName").getValue());
                            if(membersMap.get(currID).getIsAdmin()){
                                //add to admin
                                admins.put(new User(
                                                userSnap.child("firstName").getValue().toString(),
                                                userSnap.child("lastName").getValue().toString(),
                                                userSnap.child("email").getValue().toString()),
                                        membersMap.get(currID).getTitle());

                            } else {
                                //add to genUsers
                                genUsers.put(new User(
                                                userSnap.child("firstName").getValue().toString(),
                                                userSnap.child("lastName").getValue().toString(),
                                                userSnap.child("email").getValue().toString()),
                                        membersMap.get(currID).getTitle());
                            }
                        }
                    }


                    System.out.println("The admins: " + admins.size());
                    for(User temp : admins.keySet()){
                        System.out.println(temp.getFirstName() + temp.getLastName() + admins.get(temp));
                    }
                    System.out.println("The genusers:" + genUsers.size());
                    for(User temp : genUsers.keySet()){
                        System.out.println(temp.getFirstName() + temp.getLastName() + genUsers.get(temp));
                    }

                    System.out.println("admin list size = " + admins.size());
                    System.out.println("gen list size = " + genUsers.size());

                }


                RVAdapter adapter = new RVAdapter(membersMap, admins, genUsers);
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

        HashMap<String,ClubMember> memberList;

        //maps from use to their title in the club
        HashMap<User, String> adminMap;
        ArrayList<User> adminList;
        HashMap<User, String> genUserMap;
        ArrayList<User> genUserList;

        RVAdapter(HashMap<String, ClubMember> mem, HashMap<User, String> adMap, HashMap<User, String> genMap){
            System.out.println("4-- In RVAdapter");

            this.memberList = mem;
            this.genUserMap = genMap;
            this.adminMap = adMap;
            adminList = new ArrayList<>();
            genUserList = new ArrayList<>();

            //fill arraylist of admins
            for(User temp : adminMap.keySet()){
                adminList.add(temp);
            }
            //fill arraylist of general users
            for(User temp : genUserMap.keySet()){
                genUserList.add(temp);
            }
            //sort
            Collections.sort(adminList);
            Collections.sort(genUserList);


            System.out.println("rv admin list size = " + adminList.size());
            System.out.println("rv gen list size = " + genUserList.size());

        }

        @Override
        public RVAdapter.DirectoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout_directory, parent, false);
            RVAdapter.DirectoryViewHolder dvh = new RVAdapter.DirectoryViewHolder(v);
            return dvh;
        }

        @Override
        public void onBindViewHolder(RVAdapter.DirectoryViewHolder holder, int position) {
            System.out.println("binding holder: " + position);
            System.out.println("adminlist size = " + adminList.size());
            if(position >= adminList.size()){
                //need to look at gen users
                position = position - adminList.size();
                System.out.println("posting gen user for index " + position);
                holder.user.setText(genUserList.get(position).getFirstName() + " " + genUserList.get(position).getLastName() );
                holder.position.setText(genUserMap.get(genUserList.get(position)));
                holder.email = genUserList.get(position).getEmail();

            } else{
                //need to look at admins
                System.out.println("posting admin for index " + position);
                holder.user.setText(adminList.get(position).getFirstName() + " " + adminList.get(position).getLastName() );
                holder.position.setText(adminMap.get(adminList.get(position)));
                holder.email = adminList.get(position).getEmail();

            }

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

            //String userId;
            String email;
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

                        if(email.isEmpty()){
                            Toast.makeText(DirectoryActivity.this,
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
                            startActivity(Intent.createChooser(emailIntent, "Sending mail to: " + email));
                        }

                    }
                });



            }
        }
    }
}
