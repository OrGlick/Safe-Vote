package co.il.safevote.Activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import co.il.safevote.R;


public class EmailVerificationWaitingActivity extends AppCompatActivity
{
    TextView firstTvEmailVerification, secondTvEmailVerification;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification_wating);

        getSupportActionBar().hide(); // remove action bar
        inItFindViewById();
        firstTvEmailVerification.setText("זוהית בהצלחה");
        sendEmailVerification();
    }

    private void inItFindViewById()
    {
        firstTvEmailVerification = findViewById(R.id.first_text_view_email_verification);
        secondTvEmailVerification = findViewById(R.id.second_text_view_email_verification);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
    }

    private void sendEmailVerification()
    {
        if(firebaseUser.isEmailVerified())
            moveToFaceRecognitionActivity();
        else
        {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        secondTvEmailVerification.setText("נשלח אליך אימייל לאימות, חזור לכאן לאחר שלחצת על הקישור שבגוף האימייל. אם אינך מוצא את האימייל חפש אותו בתיקיית הספאם.");
                        waitForVerification();
                    } else {
                        secondTvEmailVerification.setText("התרחשה שגיאה בשליחת האימייל לאימות. אנא נסה שוב מאוחר יותר.");
                    }
                }
            });
        }
    }

    private void waitForVerification()
    {
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                reload();
                if(firebaseUser.isEmailVerified())
                    moveToFaceRecognitionActivity();
                else
                    waitForVerification();
            }
        }.start();
    }

    private void moveToFaceRecognitionActivity()
    {
        Intent intentToFaceRecognitionActivity = new Intent(EmailVerificationWaitingActivity.this, FaceRecognitionActivity.class);
        startActivity(intentToFaceRecognitionActivity);
    }

    private void reload()
    {
        auth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                    firebaseUser = auth.getCurrentUser();
            }
        });
    }
}