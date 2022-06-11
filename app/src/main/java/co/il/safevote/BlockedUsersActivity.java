package co.il.safevote;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class BlockedUsersActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);
    }

    //prevent the user from returning to the login activity
    @Override
    public void onBackPressed() {
    }
}