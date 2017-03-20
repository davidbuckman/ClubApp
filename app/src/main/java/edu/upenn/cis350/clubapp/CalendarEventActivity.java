package edu.upenn.cis350.clubapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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


    private SimpleDateFormat dateFormatForDisplaying = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.getDefault());


    private String EVENT_ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_event);
        EVENT_ID = getIntent().getStringExtra("EVENT_ID");

        final TextView title = (TextView) findViewById(R.id.title);
        final TextView date = (TextView) findViewById(R.id.date);
        final TextView author = (TextView) findViewById(R.id.author);
        final TextView description = (TextView) findViewById(R.id.description);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(Html.fromHtml("<font color='#ffffff'>Event</font>"));
        DatabaseReference ref = mDatabaseReference;
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot ds = dataSnapshot.child("events").child(EVENT_ID);
                ClubEvent clubEvent = new ClubEvent(ds.child("author").getValue(String.class),
                        ds.child("description").getValue(String.class),
                        ds.child("name").getValue(String.class),
                        ds.child("date").getValue(Long.class),
                        EVENT_ID);
                title.setText(clubEvent.getName());
                date.setText("WHEN: " + dateFormatForDisplaying.format(new Date(clubEvent.getDate())));
                author.setText("CREATED BY: " + clubEvent.getAuthor());
                description.setText("DESCRIPTION: \n" + clubEvent.getDescription());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
}
