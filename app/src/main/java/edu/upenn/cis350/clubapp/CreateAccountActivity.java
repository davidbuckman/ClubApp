package edu.upenn.cis350.clubapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by david on 2/14/2017.
 */

public class CreateAccountActivity extends AppCompatActivity {

    private EditText mFirstName;
    private EditText mLastName;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);

        mFirstName       = (EditText) findViewById(R.id.editFirstName);
        mLastName        = (EditText) findViewById(R.id.editLastName);
        mEmail           = (EditText) findViewById(R.id.editEmail);
        mPassword        = (EditText) findViewById(R.id.editPassword);
        mConfirmPassword = (EditText) findViewById(R.id.editConfirmPassword);
    }

    public void signUp(View view) {
        System.out.println(mFirstName.getText().toString());
    }

}
