package edu.upenn.cis350.clubapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.RelativeLayout;
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

import static edu.upenn.cis350.clubapp.R.id.date;
import static java.security.AccessController.getContext;
import android.view.MenuItem;

public class CalendarActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static Context mContext;

    private static final String TAG = "CalendarActivity";
    private Calendar currentCalender = Calendar.getInstance(Locale.getDefault());
    private SimpleDateFormat dateFormatForDisplaying = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.getDefault());
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());
    private boolean shouldShow = false;
    private CompactCalendarView compactCalendarView;
    Toolbar toolbar;
    private HashSet<String> eventList = new HashSet<String>();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    FirebaseAuth auth = FirebaseAuth.getInstance();
    String clubID;


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        System.out.println("SELECTED SOMETHING");
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    protected void onCreate(Bundle savedInstanceState) {

        mContext = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        clubID = getIntent().getStringExtra("CLUB");
        final ArrayList<ClubEvent> mutableBookings = new ArrayList<>();


        mDatabaseReference.child("clubs").child(clubID).child("members").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Button b = (Button) findViewById(R.id.cal_add_button);

                if(dataSnapshot.child("isAdmin").getValue(Boolean.class) == false) {
                    b.setVisibility(View.GONE);
                } else {
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent();
                            i.putExtra("CLUB", clubID);
                            i.setClass(mContext, AddEventActivity.class);
                            startActivity(i);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //set up refresh button
        final FloatingActionButton refresh = (FloatingActionButton) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                startActivity(getIntent().putExtra("CLUB", clubID));
                overridePendingTransition(0, 0);


            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

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
                        i.putExtra("CLUB_ID", clubID);
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
        toolbar.setTitle(Html.fromHtml("<font color='#ffffff'>"
                + dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()) +
                "</font>"));

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



        loadEvents();





    }

    private void loadEvents() {
        DatabaseReference ref = mDatabaseReference;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                compactCalendarView.removeAllEvents();
                for (DataSnapshot snapshot : dataSnapshot.child("clubs").child(clubID).child("events").getChildren()){
                    System.out.println("\n events: " + snapshot.getKey());
                    eventList.add(snapshot.getKey());
                }

                for(String event : eventList) {
                    DataSnapshot ds = dataSnapshot.child("events").child(event);
                        //String author, String description, String name, Long date
                    System.out.println("EVENT IS" + event);
                        ClubEvent clubEvent = new ClubEvent(ds.child("author").getValue(String.class),
                                ds.child("description").getValue(String.class),
                                ds.child("name").getValue(String.class),
                                ds.child("date").getValue(Long.class),
                                event);

                        System.out.println("THE EVENT DATE IS" + clubEvent.getDate());
                        System.out.println("LONG: " + clubEvent.getDescription());
                        System.out.println("THE EVENT 2 DATE IS" + clubEvent.getDate());

                    if (clubEvent.getDate() != null) {

                        Event e = new Event(Color.BLACK, clubEvent.getDate(), clubEvent);
                        compactCalendarView.addEvent(e);
                    }
                }
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
