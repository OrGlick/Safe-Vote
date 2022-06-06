package co.il.safevote;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etEmail, etPassword;
    Button btnConfirm;
    String email, password;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    String tag = "TAG1";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth  = FirebaseAuth.getInstance();
        inIt();
    }

    private void inIt()
    {
        etEmail = findViewById(R.id.edit_text_email);
        etPassword = findViewById(R.id.edit_text_password);
        btnConfirm = findViewById(R.id.button_confirm_email_and_password);
        btnConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();
        if (view == btnConfirm && !email.equals("") && !password.equals(""))
        {
            removeSpacesFromTheEnd();
            if (isEmailAndPasswordValid())
            {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                if (task.isSuccessful())
                                {
                                    firebaseUser = auth.getCurrentUser();
                                    Log.d(tag, "login successful");
                                    Log.d(tag, "is email verified: "+firebaseUser.isEmailVerified());
                                    Intent intentToWaiting = new Intent(LoginActivity.this, EmailVerificationWatingActivity.class);
                                    startActivity(intentToWaiting);


                                }
                                else
                                {
                                    Log.d(tag, "login failed, exception: "+task.getException());
                                    // TODO: 01/06/2022 show an error massage.
                                    //  Store the amount of times this device had tried to login to prevent brute force
                                }
                            }
                        });

            }
        }
    }

    private void removeSpacesFromTheEnd()
    {
        if (password.charAt(password.length()-1) == ' ')
            password = password.substring(0, password.length()-1);
        if (email.charAt(email.length()-1) == ' ')
            email = email.substring(0, email.length()-1);
    }

    private boolean isEmailAndPasswordValid()
    {



        return true;
    }
}