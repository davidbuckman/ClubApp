package edu.upenn.cis350.clubapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;

/**
 * Created by david on 3/7/2017.
 */

public class InvitationsActivity extends AppCompatActivity {

    private static Context mContext;

    private TextView mNoInvitations;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;
    static boolean adminStatus = false;
    static String title = "";


    //Getting reference to Firebase Database
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static DatabaseReference mDatabaseReference = database.getReference();

    static FirebaseAuth auth = FirebaseAuth.getInstance();
    private static String userID = auth.getCurrentUser().getUid();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        mContext = this;

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

        mNoInvitations = (TextView) findViewById(R.id.no_invitations);
        mRecyclerView = (RecyclerView) findViewById(R.id.invitations_recycler_view);
        if (mRecyclerView != null) {
            //to enable optimization of RecyclerView
            mRecyclerView.setHasFixedSize(true);
        }
        //using staggered grid pattern in recyclerview
        mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Say Hello to our new FirebaseUI android Element, i.e., FirebaseRecyclerAdapter
        mRecyclerView.setAdapter(new FirebaseRecyclerAdapter<ClubInvitation, InvitationViewHolder>(
                ClubInvitation.class,
                R.layout.card_layout_invitations,
                InvitationViewHolder.class,
                mDatabaseReference.child("users").child(userID).child("invitations")
                ) {
                    @Override
                    protected void populateViewHolder(final InvitationViewHolder viewHolder, final ClubInvitation model, int position) {
                        final String key = this.getRef(position).getKey();
                        mDatabaseReference.child("clubs").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //TODO: invitation doesn't contain name - removing "null pointer" causes error
                                System.out.println("WILL THROW ERROR?");
                                Log.d("InvitationsActivity", "null pointer:" + dataSnapshot.child("name").getValue(String.class));
                                String name = dataSnapshot.child("name").getValue(String.class);
                                String about = dataSnapshot.child("about").getValue(String.class);
                                viewHolder.clubName.setText(name);
                                viewHolder.clubAbout.setText(about);
                                viewHolder.clubID = key;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                });

    }

    protected static void acceptInvitation(String clubID) {
        DatabaseReference user  = mDatabaseReference.child("users").child(userID);
        // get boolean for admin status
        user.child("invitations").child(clubID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adminStatus = (boolean) dataSnapshot.child("isAdmin").getValue();
                title = dataSnapshot.child("title").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //TODO: overrode to set value to true
        user.child("clubs").child(clubID).setValue(true);
        user.child("invitations").child(clubID).removeValue();
        System.out.println("accepted invite to " + clubID);


        DatabaseReference userInClub = mDatabaseReference.child("clubs").child(clubID).child("members").child(userID);
        userInClub.child("isAdmin").setValue(adminStatus);
        userInClub.child("title").setValue(title);

        Log.d("InvitationsActivity", "Accepting invitation to " + clubID);
    }

    protected static void declineInvitation(String clubID) {
        mDatabaseReference.child("users").child(userID).child("invitations").child(clubID).removeValue();
        Log.d("InvitationsActivity", "Declining invitation to " + clubID);
    }

    //ViewHolder for our Firebase UI

    public static class InvitationViewHolder extends RecyclerView.ViewHolder {
        String clubID;
        TextView clubName;
        TextView clubAbout;
        Button btnAcceptInv;
        Button btnDeclineInv;

        public InvitationViewHolder(View v) {
            super(v);
            clubName = (TextView) v.findViewById(R.id.club_name);
            clubAbout = (TextView) v.findViewById(R.id.club_description);
            btnAcceptInv = (Button) v.findViewById(R.id.accept_inv);
            btnDeclineInv = (Button) v.findViewById(R.id.decline_inv);
            btnAcceptInv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acceptInvitation(clubID);
                }
            });
            btnDeclineInv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    declineInvitation(clubID);
                }
            });
        }

    }

}
