package edu.upenn.cis350.clubapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import static java.security.AccessController.getContext;

public class CalendarActivity extends AppCompatActivity {

    private static Context mContext;

    private static final String TAG = "CalendarActivity";
    private Calendar currentCalender = Calendar.getInstance(Locale.getDefault());
    private SimpleDateFormat dateFormatForDisplaying = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.getDefault());
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());
    private boolean shouldShow = false;
    private CompactCalendarView compactCalendarView;
    private Toolbar toolbar;
    private HashSet<String> eventList = new HashSet<String>();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    FirebaseAuth auth = FirebaseAuth.getInstance();
    String clubID;


    protected void onCreate(Bundle savedInstanceState) {

        mContext = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        clubID = getIntent().getStringExtra("CLUB");
        final ArrayList<ClubEvent> mutableBookings = new ArrayList<>();

        final ListView bookingsListView = (ListView) findViewById(R.id.bookings_listview);

        class EventArrayAdapter extends ArrayAdapter<ClubEvent> {
            public EventArrayAdapter(Context context, ArrayList<ClubEvent> events) {
                super(context, 0, events);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the data item for this position
                ClubEvent event = getItem(position);
                // Check if an existing view is being reused, otherwise inflate the view
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                }
                // Lookup view for data population
                TextView eventName = (TextView) convertView.findViewById(android.R.id.text1);
                eventName.setText(new Date(event.getDate()) + ": " + event.getName());
                eventName.setTag(position);
                eventName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = (Integer) view.getTag();
                        // Access the row position here to get the correct data item
                        ClubEvent eventPassToIntent = getItem(position);
                        // Do what you want here...
                        Intent i = new Intent();
                        i.putExtra("EVENT_ID", eventPassToIntent.getEventId());
                        i.setClass(mContext, CalendarEventActivity.class);
                        startActivity(i);
                    }
                });
                // Return the completed view to render on screen
                return convertView;
            }
        }
        final ArrayAdapter adapter = new EventArrayAdapter(this, mutableBookings);
        bookingsListView.setAdapter(adapter);

        compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);

        compactCalendarView.setUseThreeLetterAbbreviation(false);
        compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(Html.fromHtml("<font color='#ffffff'>"
                + dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()) +
                "</font>"));
        setSupportActionBar(toolbar);

        loadEvents();

        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                toolbar.setTitle(Html.fromHtml("<font color='#ffffff'>" + dateFormatForMonth.format(dateClicked) + "</font>"));
                List<Event> bookingsFromMap = compactCalendarView.getEvents(dateClicked);
                Log.d(TAG, "inside onclick " + dateFormatForDisplaying.format(dateClicked));
                if (bookingsFromMap != null) {
                    Log.d(TAG, bookingsFromMap.toString());
                    mutableBookings.clear();
                    for (Event booking : bookingsFromMap) {
                        ClubEvent data = (ClubEvent) booking.getData();
                        mutableBookings.add(data);
                    }
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                toolbar.setTitle(Html.fromHtml("<font color='#ffffff'>" + dateFormatForMonth.format(firstDayOfNewMonth)+ "</font>"));
            }
        });

    }

    private void loadEvents() {
        DatabaseReference ref = mDatabaseReference;
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.child("clubs").child(clubID).child("events").getChildren()){
                    System.out.println("\n events: " + snapshot.getKey());
                    eventList.add(snapshot.getKey());
                }

                for(String event : eventList) {
                    DataSnapshot ds = dataSnapshot.child("events").child(event);
                        //String author, String description, String name, Long date
                        ClubEvent clubEvent = new ClubEvent(ds.child("author").getValue(String.class),
                                ds.child("description").getValue(String.class),
                                ds.child("name").getValue(String.class),
                                ds.child("date").getValue(Long.class),
                                event);

                        System.out.println("LONG: " + clubEvent.getDescription());
                        Event e = new Event(Color.WHITE, clubEvent.getDate(), clubEvent);

                        compactCalendarView.addEvent(e);
                }
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
