package edu.upenn.cis350.clubapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {


    private static Context mContext;

    private FloatingActionButton fab;

    ScaleAnimation shrinkAnim;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;
    private TextView clubView;


    //Getting reference to Firebase Database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    FirebaseAuth auth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mContext = this;

        //Initializing our Recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        clubView = (TextView) findViewById(R.id.club_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(Html.fromHtml("<font color='#ffffff'>Club List </font>"));
        setSupportActionBar(toolbar);
        //scale animation to shrink floating actionbar
        shrinkAnim = new ScaleAnimation(1.15f, 0f, 1.15f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        if (mRecyclerView != null) {
            //to enable optimization of recyclerview
            mRecyclerView.setHasFixedSize(true);
        }
        //using staggered grid pattern in recyclerview
        mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        DatabaseReference ref = mDatabaseReference.child("users/" + auth.getCurrentUser().getUid() + "/clubs");
        //Say Hello to our new FirebaseUI android Element, i.e., FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<Boolean,ClubViewHolder> adapter = new FirebaseRecyclerAdapter<Boolean, ClubViewHolder>(
                Boolean.class,
                R.layout.card_layout,
                ClubViewHolder.class,
                //referencing the node where we want the database to store the data from our Object
                ref
        ) {
            @Override
            protected void populateViewHolder(final ClubViewHolder viewHolder, final Boolean model, int position) {
                final String key = this.getRef(position).getKey();
                mDatabaseReference.child("clubs").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(DataSnapshot dataSnapshot) {
                         String name = dataSnapshot.child("name").getValue(String.class);
                         String about = dataSnapshot.child("about").getValue(String.class);
                         viewHolder.clubName.setText(name);
                         viewHolder.clubAbout.setText(about);
                         viewHolder.clubLink.setTag(R.string.club_id, key);

                     }

                     @Override
                     public void onCancelled(DatabaseError databaseError) {

                     }
                 });

            }
        };

        mRecyclerView.setAdapter(adapter);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                shrinkAnim.setDuration(400);
                fab.setAnimation(shrinkAnim);
                shrinkAnim.start();
                shrinkAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //changing floating actionbar visibility to gone on animation end
                        fab.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {


                    }
                });

                startActivity(new Intent(MainActivity.this, CreateClubActivity.class));

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (fab.getVisibility() == View.GONE)
            fab.setVisibility(View.VISIBLE);
    }

    //ViewHolder for our Firebase UI

    public static class ClubViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView clubName;
        TextView clubAbout;
        Button clubLink;

        public ClubViewHolder(View v) {
            super(v);
            clubName = (TextView) v.findViewById(R.id.club_name);
            clubAbout = (TextView) v.findViewById(R.id.club_description);
            clubLink = (Button) v.findViewById(R.id.club_link);
            clubLink.setOnClickListener(this);

        }

        @Override
        public void onClick(View v){
            System.out.println("Click" + clubName.getText().toString());
            Intent i = new Intent(mContext, AnnouncementsActivity.class);
            i.putExtra("CLUB", clubLink.getTag(R.string.club_id).toString());
            mContext.startActivity(i);

        }

    }

}
