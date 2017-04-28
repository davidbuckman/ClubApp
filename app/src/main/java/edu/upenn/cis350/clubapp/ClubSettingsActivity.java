package edu.upenn.cis350.clubapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.channels.Channel;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by david on 3/6/2017.
 */

public class ClubSettingsActivity extends AppCompatActivity {

    //new data structures for list of channels
    private HashSet<String> usersChannels;
    private HashSet<UserChannel> clubChannels;

    //Getting reference to Firebase Database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    ListView channelList;

    private EditText userEmail;
    private EditText userTitle;
    private Button btnInviteUser;
    private Button inviteUser;
    private Button leaveGroup;
    private Button deleteGroup;
    private CheckBox adminBox;
    private boolean adminAuth = false;

    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    String clubID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_settings);

        clubID = getIntent().getStringExtra("CLUB");

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

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //Getting reference to Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference mDatabaseReference = database.getReference();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(ClubSettingsActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        // update admin status
        isAdmin(user, clubID);

        userEmail = (EditText) findViewById(R.id.user_email);
        userTitle = (EditText) findViewById(R.id.user_title);
        btnInviteUser = (Button) findViewById(R.id.invite_user_button);
        inviteUser = (Button) findViewById(R.id.inviteUser);
        leaveGroup = (Button) findViewById(R.id.leave_club_button);
        deleteGroup = (Button) findViewById(R.id.delete_club_button);
        adminBox = (CheckBox) findViewById(R.id.adminBox);

        userEmail.setVisibility(View.GONE);
        userTitle.setVisibility(View.GONE);
        inviteUser.setVisibility(View.GONE);
        adminBox.setVisibility(View.GONE);


        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        btnInviteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userEmail.setVisibility(View.VISIBLE);
                userTitle.setVisibility(View.VISIBLE);
                inviteUser.setVisibility(View.VISIBLE);
                adminBox.setVisibility(View.VISIBLE);
            }
        });

        inviteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = userEmail.getText().toString().trim();

                // check if admin status is checked.
                final boolean adminStatus = adminBox.isChecked();

                //get title
                //String title = userTitle.getText().toString().trim();
                //System.out.println("THE USER TITLE = " + title);


                if (!email.equals("")) {
                    mDatabaseReference.child("users").orderByChild("email").equalTo(email)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getChildrenCount() > 1) {
                                        Log.e("ClubSettingsActivity", "Multiple users with same email");
                                        Toast.makeText(ClubSettingsActivity.this, "Whoops! There are multiple registered users with this email address. Please let our team know!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    } else if (dataSnapshot.getChildrenCount() == 0) {
                                        Toast.makeText(ClubSettingsActivity.this, "There is no user with that email!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    } else {

                                        final String inviteeUid = dataSnapshot.getChildren().iterator().next().getKey();
                                        mDatabaseReference.child("clubs").child(clubID).child("members").child(user.getUid()).child("isAdmin")
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        boolean isAdmin = (boolean) dataSnapshot.getValue();
                                                        if (isAdmin) {
                                                            // Update user's invited to list
                                                            mDatabaseReference.child("users").child(inviteeUid).child("invitations").child(clubID).child("isAdmin").setValue(adminStatus);

                                                            String title = userTitle.getText().toString().trim();
                                                            System.out.println("USER TITLE = " + title);

                                                            if(title.isEmpty()){
                                                                System.out.println("using default");
                                                                mDatabaseReference.child("users").child(inviteeUid).child("invitations").child(clubID).child("title").setValue("General Member");
                                                            } else {
                                                                System.out.println("using: " + title);
                                                                mDatabaseReference.child("users").child(inviteeUid).child("invitations").child(clubID).child("title").setValue(title);
                                                            }
                                                            Toast.makeText(ClubSettingsActivity.this, "User has been invited!", Toast.LENGTH_LONG).show();
                                                            progressBar.setVisibility(View.GONE);
                                                        } else {
                                                            Toast.makeText(ClubSettingsActivity.this, "Only admins can invite users!", Toast.LENGTH_SHORT).show();
                                                            progressBar.setVisibility(View.GONE);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                } else if (email.equals("")) {
                    userEmail.setError("Enter an email");
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(ClubSettingsActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        leaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseReference.child("users").child(user.getUid()).child("clubs").child(clubID).removeValue();
                mDatabaseReference.child("clubs").child(clubID).child("members").child(user.getUid()).removeValue();
                Intent intent = new Intent(ClubSettingsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mDatabaseReference.child("clubs").child(clubID).child("members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        boolean isAdmin = dataSnapshot.child(user.getUid()).child("isAdmin").getValue(Boolean.class);
                        if (isAdmin) {
                            deleteGroup.setVisibility(View.VISIBLE);
                            deleteGroup.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    for (DataSnapshot member : dataSnapshot.getChildren()) {
                                        String memberID = member.getKey();
                                        mDatabaseReference.child("users").child(memberID).child("clubs").child(clubID).removeValue();
                                    }
                                    mDatabaseReference.child("clubs").child(clubID).removeValue();
                                    finish();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


        // Initialize data structures to contain channel info
        clubChannels = new HashSet<>();
        usersChannels = new HashSet<>();


        //get data and display
        DatabaseReference ref = mDatabaseReference.child("clubs").child(clubID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // populate clubChannels with all channels in club
                for (DataSnapshot snapshot : dataSnapshot.child("channels").getChildren()) {
                    //determine if current channel is in the user's list
                    System.out.println("this channel in club: " + snapshot.getKey().toString());
                    clubChannels.add(new UserChannel(snapshot.getKey().toString()));
                    if (usersChannels.contains(snapshot.getKey().toString())) {

                    }
                }

                //populate userChannels with list of channels this user is subscribed to
                for (DataSnapshot snapshot : dataSnapshot.child("members").child(user.getUid()).child("channels").getChildren()) {
                    System.out.println("\n   channel: " + snapshot.getKey());
                    usersChannels.add(snapshot.getKey().toString());
                }
                System.out.println("size of user channel set= " + usersChannels.size());
                UserChannel generalChannel = null;
                for(UserChannel u: clubChannels) {
                    if(usersChannels.contains(u.getName())) {
                        u.setActive(true);
                    }
                    if (u.getName().equals("general")) {
                        generalChannel = u;
                    }
                }
                if (generalChannel != null) {
                    clubChannels.remove(generalChannel);
                }

                //todo: pass to adapter
                populateChannelList( clubChannels.toArray(new UserChannel[clubChannels.size()]));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });




    }

    private void populateChannelList(UserChannel[] clubChannels) {
        final ChannelAdapter ca = new ChannelAdapter(this, clubChannels);

        channelList = (ListView) findViewById(R.id.channel_listview);
        channelList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item,
                                    int position, long id) {
                UserChannel uChannel = ca.getItem(position);
                // invert state of user channel
                uChannel.setActive(!uChannel.getActive());
              //  PlanetViewHolder viewHolder = (PlanetViewHolder) item.getTag();
               // viewHolder.getCheckBox().setChecked(planet.isChecked());
            }
        });

        channelList.setAdapter(ca);

    }

    class ChannelAdapter extends ArrayAdapter<UserChannel> {
        UserChannel[] channels = null;
        Context context;
        public ChannelAdapter(Context context, UserChannel[] channelArr) {
            super(context,R.layout.checkbox_row,channelArr);
            this.context = context;
            channels = channelArr;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserChannel channel = this.getItem(position);

            TextView label;
            CheckBox box;
            if(convertView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.checkbox_row, parent, false);
                label = (TextView) convertView.findViewById(R.id.textView);
                box = (CheckBox) convertView.findViewById(R.id.checkBox);
                box.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        UserChannel uChannel = (UserChannel) cb.getTag();
                        uChannel.setActive(cb.isChecked());
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if(uChannel.getActive()) {
                            mDatabaseReference.child("clubs").child(clubID).child("members")
                                    .child(user.getUid()).child("channels").child(uChannel.getName()).setValue(true);
                        } else {
                            mDatabaseReference.child("clubs").child(clubID).child("members")
                                    .child(user.getUid()).child("channels").child(uChannel.getName()).removeValue();
                        }
                        System.out.println("HI");
                    }
                });

                label.setText(channels[position].getName());
                box.setChecked(channels[position].getActive());
                convertView.setTag(new ViewHolder(label, box));

            } else {
                ViewHolder viewH = (ViewHolder) convertView.getTag();
                box = viewH.checkbox;
                label = viewH.text;

            }

            box.setTag(channel);

            return convertView;
        }


    }

    private class ViewHolder {
        protected TextView text;
        protected CheckBox checkbox;

        ViewHolder(TextView text, CheckBox box) {
            this.text = text;
            this.checkbox = box;
        }

    }

  
  // I got rid of isAdmin due to asynchronousness making it too complicated and just built the
  // admin check into the function itself. Leaving your todo but currently this function is useless -dcb

    public boolean isAdmin(final FirebaseUser user, final String clubID) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference mDatabaseReference = database.getReference();
        //get data and display
        DatabaseReference ref = mDatabaseReference;

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            //mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("auth", "old adminAuth:" + adminAuth);
                //get all members for the club
                adminAuth = (boolean) dataSnapshot.child("clubs").child(clubID).child("members").child(user.getUid()).child("isAdmin").getValue();
                Log.d("auth", "new adminAuth:" + adminAuth);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // TODO
        return adminAuth;
    }



    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

}
