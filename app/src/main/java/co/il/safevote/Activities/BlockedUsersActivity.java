package co.il.safevote.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import co.il.safevote.R;

public class BlockedUsersActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);

        getSupportActionBar().hide(); // remove action bar
    }

    //prevent the user from returning to the login activity
    @Override
    public void onBackPressed() {
    }
}