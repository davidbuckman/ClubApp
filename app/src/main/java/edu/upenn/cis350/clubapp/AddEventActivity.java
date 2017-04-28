package edu.upenn.cis350.clubapp;

import android.annotation.SuppressLint;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

import static edu.upenn.cis350.clubapp.R.id.body;

public class AddEventActivity extends AppCompatActivity {

    private EditText inputTitle, inputBody;
    private Button btnMakeAnnouncement;
    private ProgressBar progressBar;
    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year, month, day, hour, minute;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();


    String clubID;
    String eventID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        clubID = getIntent().getStringExtra("CLUB");
        eventID = getIntent().getStringExtra("EVENT");
        System.out.println("CLUB ID " + clubID);
        System.out.println("EVENT ID " + eventID);

        final Calendar c= Calendar.getInstance();
        int year = c.get(c.YEAR);
        int month = c.get(c.MONTH);
        int dayOfMonth = c.get(c.DAY_OF_MONTH);
        hour = c.get(c.HOUR_OF_DAY);
        minute = c.get(c.MINUTE);

        //Get the widgets reference from XML layout
        final DatePicker dp = (DatePicker) findViewById(R.id.dp);
        final TimePicker tp = (TimePicker) findViewById(R.id.tp);


        btnMakeAnnouncement = (Button) findViewById(R.id.make_event);
        inputTitle = (EditText) findViewById(R.id.event_title);
        inputBody = (EditText) findViewById(R.id.event_description);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (eventID != null && eventID.length() > 0) {
            DatabaseReference eref = db.getReference().child("events").child(eventID);
            eref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    inputTitle.setText(dataSnapshot.child("name").getValue(String.class));
                    inputBody.setText(dataSnapshot.child("description").getValue(String.class));
                    Calendar c = Calendar.getInstance();
                    c.setTime(new Date(dataSnapshot.child("date").getValue(Long.class)));
                    System.out.println("YEAR" + c.get(Calendar.YEAR));
                    tp.setCurrentMinute(c.get(Calendar.MINUTE));
                    tp.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
                    dp.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        btnMakeAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = inputTitle.getText().toString().trim();
                String description = inputBody.getText().toString().trim();
                int month = dp.getMonth();
                int day = dp.getDayOfMonth();
                int year = dp.getYear();

                int hour = tp.getCurrentHour();
                int minute = tp.getCurrentMinute();

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(0);
                cal.set(year, month, day, hour, minute);
                Date date = cal.getTime();
                Long time = date.getTime();


                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(getApplicationContext(), "Enter a title!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(getApplicationContext(), "Enter an announcement!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                if (eventID != null && eventID.length() > 0) {
                    System.out.println("EVENT ID INSIDE " + eventID);
                    System.out.println("CLUB ID INSIDE " + clubID);
                    DatabaseReference eevent = db.getReference().child("events").child(eventID);
                    if (eevent != null) {
                        eevent.removeValue();
                    }
                    DatabaseReference cevent = db.getReference().child("clubs").child(clubID).child("events").child(eventID);
                    if (cevent != null) {
                        cevent.removeValue();
                    }
                }

                DatabaseReference ref = db.getReference().child("events");


                final DatabaseReference evt = ref.push();
                db.getReference().child("users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        evt.child("author").setValue(dataSnapshot.child("firstName").getValue(String.class) + " "
                                                    + dataSnapshot.child("lastName").getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                evt.child("date").setValue(time);
                evt.child("description").setValue(description);
                evt.child("name").setValue(title);

                db.getReference().child("clubs").child(clubID).child("events").child(evt.getKey()).setValue(true);

                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

}
