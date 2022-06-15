package co.il.safevote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handleIntent();
    }

    private void handleIntent()
    {
        Intent intent = getIntent();
        String state = intent.getStringExtra("state");

        Intent intent1;
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