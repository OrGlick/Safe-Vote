package co.il.safevote.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import co.il.safevote.NotificationReceiver;
import co.il.safevote.R;

public class SplashActivity extends AppCompatActivity
{
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide(); // remove action bar from the splash screen

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                init();
            }
        }, 1500); // wait so the user will be able to see the animation if the internet is too fast (lol), and then connect to firebase server
    }

    private void init()
    {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Date For Elections");
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                int dayFromRTDB = snapshot.child("day").getValue(Integer.class);
                int monthFromRTDB = snapshot.child("month").getValue(Integer.class);
                int yearFromRTDB = snapshot.child("year").getValue(Integer.class);

                String electionDateFromRTDB = makeDateString(yearFromRTDB, monthFromRTDB, dayFromRTDB);

                calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int second = calendar.get(Calendar.SECOND);

                String currentDate = makeDateString(year, month, day);
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                if(currentDate.equals(electionDateFromRTDB) && isVotingHours(hour, second))
                    intent.putExtra("state", "can vote");
                else
                {
                    notificationElectionTime(dayFromRTDB, monthFromRTDB, yearFromRTDB); // I don't want to show the notification if it's the election time and the user is already in the app
                    intent.putExtra("elections date", electionDateFromRTDB);
                    intent.putExtra("state", "can not vote");
                }
                startActivity(intent);
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(SplashActivity.this, 1, intent, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private String makeDateString(int year, int month, int day)
    {
        return day + "." + month + "." + year;
    }
}