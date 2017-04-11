package edu.upenn.cis350.clubapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by david on 4/3/2017.
 */

public class AddAnnouncementActivity extends AppCompatActivity {

    private EditText inputTitle, inputBody;
    private Button btnMakeAnnouncement;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseDatabase db;

    String clubID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_announcement);
        clubID = getIntent().getStringExtra("CLUB");

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        btnMakeAnnouncement = (Button) findViewById(R.id.make_announcement);
        inputTitle = (EditText) findViewById(R.id.title);
        inputBody = (EditText) findViewById(R.id.body);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        btnMakeAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = inputTitle.getText().toString().trim();
                String body = inputBody.getText().toString().trim();

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(getApplicationContext(), "Enter a title!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(body)) {
                    Toast.makeText(getApplicationContext(), "Enter an announcement!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //create announcement
                DatabaseReference announcementRef = db.getReference().child("clubs").child(clubID).child("announcements").child(Long.toString(System.currentTimeMillis()));
                announcementRef.child("title").setValue(title);
                announcementRef.child("body").setValue(body);
                //TODO: set channel?

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
