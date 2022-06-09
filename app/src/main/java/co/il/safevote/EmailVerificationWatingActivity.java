package co.il.safevote;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class EmailVerificationWatingActivity extends AppCompatActivity
{
    TextView tvEmailVerification;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    String tag = "TAG1";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification_wating);

        inItFindViewById();
        tvEmailVerification.setText("זוהית בהצלחה. ");
        sendEmailVerification();
    }

    private void inItFindViewById()
    {
        tvEmailVerification = (TextView) findViewById(R.id.text_view_email_verification);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
    }

    private void sendEmailVerification()
    {
        if(firebaseUser.isEmailVerified())
            moveToFaceRecognitionActivity();
        else {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        tvEmailVerification.setText("check");
                        tvEmailVerification.setText("נשלח אליך אימייל לאימות. חזור לכאן לאחר שלחצת על הקישור שבגוף המייל. אם אינך מוצא את המייל חפש אותו בתיקיית הספאם.");
                        waitForVerification();
                    } else {
                        tvEmailVerification.setText("התרחשה שגיאה בשליחת האימייל לאימות. אנא נסה שוב מאוחר יותר.");
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
                Log.d(tag, "tik");
            }

            @Override
            public void onFinish() {
                reload();
                if(firebaseUser.isEmailVerified())
                {
                    moveToFaceRecognitionActivity();
                }
                else
                {
                    waitForVerification();
                }
            }
        }.start();
    }

    private void moveToFaceRecognitionActivity()
    {
        Intent intentToFaceRecognitionActivity = new Intent(EmailVerificationWatingActivity.this, FaceRecognitionActivity.class);
        startActivity(intentToFaceRecognitionActivity);
    }


    private void reload()
    {
        auth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    firebaseUser = auth.getCurrentUser();
                    Log.d(tag, "is verified: "+firebaseUser.isEmailVerified());
                }
            }
        });
    }
}