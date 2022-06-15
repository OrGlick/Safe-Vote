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
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String electionDateFromRTDB;
    Calendar calendar;
    String currentDate;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init()
    {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Date For Elections");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("טוען...");
        progressDialog.show();
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                int dayFromRTDB = snapshot.child("day").getValue(Integer.class);
                int monthFromRTDB = snapshot.child("month").getValue(Integer.class);
                int yearFromRTDB = snapshot.child("year").getValue(Integer.class);
                notificationElectionTime(dayFromRTDB, monthFromRTDB, yearFromRTDB);

                String electionDateFromRTDB = makeDateString(yearFromRTDB, monthFromRTDB, dayFromRTDB);

                calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int second = calendar.get(Calendar.SECOND);

                String currentDate = makeDateString(year, month, day);

                if(currentDate.equals(electionDateFromRTDB) && isVotingHours(hour, second))
                {
                    progressDialog.dismiss();

                    // it's elections time. move to login activity
                    Intent intentToLogin = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intentToLogin);
                }
                else
                {
                    progressDialog.dismiss();
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("האפליקציה תיפתח להצבעה בתאריך " + electionDateFromRTDB + ", בין השעות 07:00 - 22:00. אין מה לדאוג, נשלח לכם התראה כשתוכלו להצביע")
                            .setCancelable(false)
                            .create().show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // allow voting only between 07:00 - 22:00
    private boolean isVotingHours(int hour, int second)
    {
        if(hour == 21)
            return second <= 59;
        return hour >= 7 && hour < 21;
    }

    // send notification to the user on the election day, at 07:00
    private void notificationElectionTime(int day, int month, int year)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month-1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);   //the notification will sent immediately if the 07:00 on the election day is in the past

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 1, intent, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private String makeDateString(int year, int month, int day)
    {
        return day + "." + month + "." + year;
    }
}