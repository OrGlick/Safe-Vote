package co.il.safevote;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

public class FaceRecognitionActivity extends AppCompatActivity
{
    Button btnOpenCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);

        initFindViewById();
    }

    private void initFindViewById()
    {
        btnOpenCamera = findViewById(R.id.btn_open_camera);
    }
}