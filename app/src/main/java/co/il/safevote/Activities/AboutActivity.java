package co.il.safevote.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import co.il.safevote.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().hide(); // remove action bar
    }
}