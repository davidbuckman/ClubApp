package edu.upenn.cis350.clubapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CalendarEventActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();
    private Toolbar toolbar;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    String clubID;
    boolean isAdmin = false;

    Context mContext;
    private SimpleDateFormat dateFormatForDisplaying = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.getDefault());


    private String EVENT_ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_event);
        EVENT_ID = getIntent().getStringExtra("EVENT_ID");
        clubID = getIntent().getStringExtra("CLUB_ID");

        mContext = this;

        final TextView title = (TextView) findViewById(R.id.title);
        final TextView date = (TextView) findViewById(R.id.date);
        final TextView author = (TextView) findViewById(R.id.author);
        final TextView description = (TextView) findViewById(R.id.description);
        final Button editEvent = (Button) findViewById(R.id.edit_event);
        final Button deleteEvent = (Button) findViewById(R.id.delete_event);

        editEvent.setVisibility(View.GONE);
        deleteEvent.setVisibility(View.GONE);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(Html.fromHtml("<font color='#ffffff'>Event</font>"));
        DatabaseReference ref = mDatabaseReference;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    DataSnapshot ds = dataSnapshot.child("events").child(EVENT_ID);
                    ClubEvent clubEvent = new ClubEvent(ds.child("author").getValue(String.class),
                            ds.child("description").getValue(String.class),
                            ds.child("name").getValue(String.class),
                            ds.child("date").getValue(Long.class),
                            EVENT_ID);
                    title.setText(clubEvent.getName());
                    if (clubEvent.getDate() == null) {
                        finish();
                    }
                    date.setText("WHEN: " + dateFormatForDisplaying.format(new Date(clubEvent.getDate())));
                    author.setText("CREATED BY: " + clubEvent.getAuthor());
                    description.setText("DESCRIPTION: \n" + clubEvent.getDescription());
                } catch(Exception e) {
                    finish();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ref.child("clubs").child(clubID).child("members").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("HIT THE ADMIN CODE");
                isAdmin = dataSnapshot.child("isAdmin").getValue(Boolean.class);
                if (isAdmin) {
                    editEvent.setVisibility(View.VISIBLE);
                    deleteEvent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        System.out.println(isAdmin);

            editEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent();
                    i.putExtra("EVENT", EVENT_ID);
                    i.putExtra("CLUB", clubID);
                    i.setClass(mContext, AddEventActivity.class);
                    startActivity(i);
                    finish();
                }
            });

            deleteEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference ref = mDatabaseReference;
                    ref.child("clubs").child(clubID).child("events").child(EVENT_ID).removeValue();
                    ref.child("events").child(EVENT_ID).removeValue();
                    finish();
                }
            });


    }
}
