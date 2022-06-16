package co.il.safevote.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import co.il.safevote.Helper;
import co.il.safevote.R;
import co.il.safevote.User;

public class VotingActivity extends AppCompatActivity implements View.OnClickListener {
    ImageButton btnSheker, btnRaka, btnHetz, btnBlank, btnMaki, btnEretz, btnShabas, btnMahapch, btnSevev, btnIsrael, btnHazakim, btnAchva;

    FirebaseDatabase firebaseDatabase;
    // get references to the parties counters on the database
    DatabaseReference shekerReference, rakaReference, hetzReference, makiReference, eretzReference, shabasReference, mahapachReference
            , sevevReference, israelReference, hazakimReference, achvaReference, userReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    User userFromRTDB;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        getSupportActionBar().hide(); // remove action bar
        initFindViewByIdAndOthers();
        initFirebasePartiesConfig();
        initFirebaseUserConfig();
    }

    private void initFindViewByIdAndOthers()
    {
        btnSheker = findViewById(R.id.btn_sheker);
        btnSheker.setOnClickListener(this);
        btnRaka = findViewById(R.id.btn_raka);
        btnRaka.setOnClickListener(this);
        btnHetz = findViewById(R.id.btn_hetz);
        btnHetz.setOnClickListener(this);
        btnBlank = findViewById(R.id.btn_blank);
        btnBlank.setOnClickListener(this);
        btnMaki = findViewById(R.id.btn_maki);
        btnMaki.setOnClickListener(this);
        btnEretz = findViewById(R.id.btn_eretz);
        btnEretz.setOnClickListener(this);
        btnShabas = findViewById(R.id.btn_shabas);
        btnShabas.setOnClickListener(this);
        btnMahapch = findViewById(R.id.btn_mahapach);
        btnMahapch.setOnClickListener(this);
        btnSevev = findViewById(R.id.btn_sevev);
        btnSevev.setOnClickListener(this);
        btnIsrael = findViewById(R.id.btn_israel);
        btnIsrael.setOnClickListener(this);
        btnHazakim = findViewById(R.id.btn_hazakim);
        btnHazakim.setOnClickListener(this);
        btnAchva = findViewById(R.id.btn_achva);
        btnAchva.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("מאשר...");
    }

    // get a reference to the parties location in the database
    private void initFirebasePartiesConfig()
    {
        firebaseDatabase = FirebaseDatabase.getInstance();
        shekerReference = firebaseDatabase.getReference("Parties").child("שקר");
        rakaReference = firebaseDatabase.getReference("Parties").child("רק איציק");
        hetzReference = firebaseDatabase.getReference("Parties").child("חץ");
        makiReference = firebaseDatabase.getReference("Parties").child("המרכז הקיצוני");
        eretzReference = firebaseDatabase.getReference("Parties").child("ארץ עיר");
        shabasReference = firebaseDatabase.getReference("Parties").child("האסירים");
        mahapachReference = firebaseDatabase.getReference("Parties").child("מהפך");
        sevevReference = firebaseDatabase.getReference("Parties").child("הסביבתית");
        israelReference = firebaseDatabase.getReference("Parties").child("ישראל שלנו");
        hazakimReference = firebaseDatabase.getReference("Parties").child("חזקים ביחד");
        achvaReference = firebaseDatabase.getReference("Parties").child("אחווה ושלום");
    }

    private void initFirebaseUserConfig()
    {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userReference = firebaseDatabase.getReference("Users/" + firebaseUser.getUid());
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                userFromRTDB = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View view)
    {
        if(view == btnSheker)
            shouldSubmitVoting("שקר", shekerReference);
        else if(view == btnRaka)
            shouldSubmitVoting("רק איציק", rakaReference);
        else if(view == btnHetz)
            shouldSubmitVoting("חץ", hetzReference);
        else if(view == btnBlank)
            blankVote();
        else if(view == btnEretz)
            shouldSubmitVoting("ארץ עיר", eretzReference);
        else if(view == btnMaki)
            shouldSubmitVoting("המרכז הקיצוני", makiReference);
        else if(view == btnSevev)
            shouldSubmitVoting("המפלגה הסביבתית", sevevReference);
        else if(view == btnMahapch)
            shouldSubmitVoting("מהפך", mahapachReference);
        else if(view == btnShabas)
            shouldSubmitVoting("האסירים", shabasReference);
        else if(view == btnAchva)
            shouldSubmitVoting("אחווה ושלום", achvaReference);
        else if(view == btnHazakim)
            shouldSubmitVoting("חזקים ביחד", hazakimReference);
        else if(view == btnIsrael)
            shouldSubmitVoting("ישראל שלנו", israelReference);

    }

    // asks the user if he approves his vote, and if yes vote
    private void shouldSubmitVoting(String partyName, DatabaseReference referenceToVoting)
    {
        char c = '"';
        new AlertDialog.Builder(VotingActivity.this)
                .setMessage("בחרת במפלגת " + c + partyName + c + ". שים לב! לאחר הבחירה, לא ניתן לבטלה או לשנותה. להצביע?")
                .setPositiveButton("כן", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.dismiss();
                        progressDialog.show();

                        // another check for extra safety if the user has not already voted or blocked from voting
                        if(!userFromRTDB.getIsVoted() && !userFromRTDB.getIsBlocked())
                        {

                            referenceToVoting.setValue(ServerValue.increment(1)); // add 1 vote to the party the voter chose
                            userReference.child("isVoted").setValue(true); // set his "isVoted" value in RTDB to false so he won't be able to vote again
                            firebaseAuth.signOut(); // sign out the user
                            progressDialog.dismiss();

                            // send the user back to the login activity and show him a "your vote has been submitted successfully" dialog
                            backToLogin();
                        }
                        else
                            Helper.showError("כבר הצבעת או נחסמת", VotingActivity.this);
                    }
                })
                .setNegativeButton("לא", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }

    private void backToLogin()
    {
        Intent intent = new Intent(VotingActivity.this, LoginActivity.class);
        intent.putExtra("vote state", "success");
        startActivity(intent);
    }

    // blank vote
    private void blankVote()
    {
        new AlertDialog.Builder(VotingActivity.this)
                .setMessage("בחרת בפתק לבן, הצבעתך לא תיחשב." + " שים לב! לאחר הבחירה, לא ניתן לבטלה או לשנותה. להצביע?")
                .setPositiveButton("כן", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.dismiss();
                        progressDialog.show();

                        // another check for extra safety if the user has not already voted or blocked from voting
                        if(!userFromRTDB.getIsVoted() && !userFromRTDB.getIsBlocked())
                        {

                            userReference.child("isVoted").setValue(true); // set his "isVoted" value in RTDB to false so he won't be able to vote again
                            firebaseAuth.signOut(); // sign out the user
                            progressDialog.dismiss();

                            // send the user back to the login activity and show him a "your vote has been submitted successfully" dialog
                            backToLogin();
                        }
                        else
                            Helper.showError("כבר הצבעת או נחסמת", VotingActivity.this);
                    }
                })
                .setNegativeButton("לא", new DialogInterface.OnClickListener()
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