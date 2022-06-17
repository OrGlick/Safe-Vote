package co.il.safevote.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import co.il.safevote.Helper;
import co.il.safevote.R;
import co.il.safevote.User;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener
{
    EditText etEmail, etPassword;
    TextView tvShowSelectedDate;
    Button btnConfirm, btnSelectIdDate;
    String email, id, idDate;
    DatePickerDialog datePickerDialog;

    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    User userFromRTDB;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth  = FirebaseAuth.getInstance();
        auth.signOut();
        inIt();
        initDatePickerDialog();
        welcomeVoter();
    }

    private void inIt() //config all things
    {
        etEmail = findViewById(R.id.edit_text_email);
        etPassword = findViewById(R.id.edit_text_password);
        tvShowSelectedDate = findViewById(R.id.tv_show_selected_id_date);
        btnConfirm = findViewById(R.id.button_confirm_email_and_password);
        btnConfirm.setOnClickListener(this);
        btnSelectIdDate = findViewById(R.id.button_select_id_date);
        btnSelectIdDate.setOnClickListener(this);
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("מאמת...");
    }

    //show the date picker dialog
    private void initDatePickerDialog()
    {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day)
            {
                month+=1;
                idDate = makeDateString(year, month, day);
                tvShowSelectedDate.setText("תאריך: " + idDate);
            }
        };

        Calendar calendar = Calendar.getInstance();
        int year1 = calendar.get(Calendar.YEAR);
        int month1 = calendar.get(Calendar.MONTH);
        int day1 = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, dateSetListener, year1, month1, day1);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    //convert the date to String with date format
    private String makeDateString(int year, int month, int day)
    {
        String day1 = String.valueOf(day);
        String month1 = String.valueOf(month);
        String year1 = String.valueOf(year);
        return day1 + "." + month1 + "." + year1;
    }

    //show a dialog to the user who voted and came to here from Voting Activity
    private void welcomeVoter()
    {
        Intent intent = getIntent();
        String voted = intent.getStringExtra("vote state");
        if(voted != null)
        {
            if(voted.equals("success"))
            {
                new AlertDialog.Builder(this)
                        .setMessage("הצבעתך נקלטה בהצלחה!")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                dialogInterface.dismiss();
                            }
                        })
                        .create().show();
            }
        }

    }

    @Override
    public void onClick(View view)
    {
        if(view == btnConfirm)
        {
            progressDialog.show();
            email = etEmail.getText().toString();
            id = etPassword.getText().toString();
            if (view == btnConfirm && !email.equals("") && !id.equals("") && !idDate.equals("")) //if not null
            {
                removeSpaceFromTheEnd();
                if (isEmailValid(email) && isIdValid(id)) //check validation of the email and the ID
                {
                    String pass = id + idDate;
                    auth.signInWithEmailAndPassword(email, pass) //sign in
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        firebaseUser = auth.getCurrentUser();
                                        checkUserVotingState();
                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Helper.showError("אימייל, תעודת זהות או תאריך הנפקה לא נכונים", LoginActivity.this);
                                    }
                                }
                            });

                }
                else
                    showErrorAndCloseProgressDialog();
            }
            else
                showErrorAndCloseProgressDialog();
        }
        else if(view == btnSelectIdDate)
            datePickerDialog.show();
    }

    //show errors on alert dialog
    private void showErrorAndCloseProgressDialog()
    {
        progressDialog.dismiss();
        Helper.showError("אנא הכנס/י אימייל ותעודת זהות תקינים", LoginActivity.this);
    }

    //check if the user already voted or blocked
    private void checkUserVotingState()
    {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users/" + firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                progressDialog.dismiss();

                userFromRTDB = snapshot.getValue(User.class);
                //if the user has already voted, show him a dialog explaining that
                if(userFromRTDB.getIsVoted())
                {
                    Helper.showError("כבר הצבעת. לא ניתן להצביע יותר מפעם אחת.", LoginActivity.this);
                }
                else if(userFromRTDB.getIsBlocked()) //if the user is blocked from voting
                {
                    // move to blocked users activity
                    Intent intent = new Intent(LoginActivity.this, BlockedUsersActivity.class);
                    startActivity(intent);
                }
                else // if everything is ok
                {
                    //move to the email verification activity
                    Intent intent = new Intent(LoginActivity.this, EmailVerificationWaitingActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //remove space from the end of the email
    private void removeSpaceFromTheEnd()
    {
        if (id.charAt(id.length()-1) == ' ')
            id = id.substring(0, id.length()-1);
        if (email.charAt(email.length()-1) == ' ')
            email = email.substring(0, email.length()-1);
    }

    //check if the email is in the format of "a@a.com"
    private boolean isEmailValid(String email)
    {
        char c;
        for(int i = 0; i < email.length(); i++)
        {
            c = email.charAt(i);
            if((c >= 'a' && c <= 'z') || (c >= '1' && c <= '9'))
            {
                for(int j = i+1; j < email.length(); j++)
                {
                    c = email.charAt(j);
                    if(c == '@')
                    {
                        for(int k = j+1; k < email.length(); k++)
                        {
                            c = email.charAt(k);
                            if((c >= 'a' && c <= 'z') || (c >= '1' && c <= '9'))
                            {
                                for(int l = k+1; l < email.length(); l++)
                                {
                                    c = email.charAt(l);
                                    if(c == '.')
                                    {
                                        for(int m = l+1; m < email.length(); m++)
                                        {
                                            c = email.charAt(m);
                                            if((c >= 'a' && c <= 'z') || (c >= '1' && c <= '9') || c == '-' || c =='_')
                                            {
                                                return true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    //the id is valid if it's 9 characters
    private boolean isIdValid(String id)
    {
        return id.length() == 9;
    }

    //inflate menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.m_menu, menu);
        return true;
    }

    //menu button's
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.contact_us_item)
        {
            intentToEmailApp();
            return true;
        }
        else if(item.getItemId() == R.id.about_item)
        {
            openAboutActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //contact me via email
    private void intentToEmailApp()
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + "yglyq935@gmail.com"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //so the user will return the app when he will press back
        try
        {
            startActivity(Intent.createChooser(intent, "שלח אימייל על ידי..."));
        }
        catch (android.content.ActivityNotFoundException e) // if no email applications are installed
        {
            Toast.makeText(this, "לא נמצאו אפליקציות מתאימות לשליחת אימייל", Toast.LENGTH_SHORT).show();
        }
    }

    //open about activity
    private void openAboutActivity()
    {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    //cancel the availability to go back to voting activity after voting
    @Override
    public void onBackPressed() {

    }
}