package edu.upenn.cis350.clubapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AnnouncementsActivity extends AppCompatActivity {


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

    String clubName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);


         //initialize data structures
        clubChannels = new HashSet<String>();
        myChannels = new HashSet<String>();
        messages = new HashSet<ClubNotification>();

        //determine club name (club ID) via intent extras
        clubName = getIntent().getStringExtra("CLUB");
        System.out.println("\nclub name in act =  " + clubName);

        //if club name is null, send back to main page
        if(clubName == null){
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
                for(DataSnapshot snapshot : dataSnapshot.child("clubs").child(clubName).child("channels").getChildren()){
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


    //TODO: implement menu





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
        public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false);
            NotificationViewHolder nvh = new NotificationViewHolder(v);
            return nvh;
        }

        @Override
        public void onBindViewHolder(NotificationViewHolder holder, int position) {
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



