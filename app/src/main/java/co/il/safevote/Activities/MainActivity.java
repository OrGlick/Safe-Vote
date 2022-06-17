package co.il.safevote.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import co.il.safevote.R;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide(); // remove action bar
        handleIntent();
    }

    private void handleIntent() //move or stay in the activity as a result of the intent
    {
        Intent intent = getIntent(); //get the intent from Splash Activity
        String state = intent.getStringExtra("state");

        if(state.equals("can vote"))
        {
            // it's elections time. move to login activity
            Intent intentToLogin = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intentToLogin);
        }
        else if(state.equals("can not vote"))
        {
            String  electionsDate = intent.getStringExtra("elections date");
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("האפליקציה תיפתח להצבעה בתאריך " + electionsDate + ", בין השעות 07:00 - 22:00. אין מה לדאוג, נשלח לכם התראה כשתוכלו להצביע")
                    .setCancelable(false)
                    .create().show();
        }

    }
}