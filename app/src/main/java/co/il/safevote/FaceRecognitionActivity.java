package co.il.safevote;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.IdentifyResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class FaceRecognitionActivity extends AppCompatActivity implements View.OnClickListener
{
    Button btnOpenCamera;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    boolean isGranted = false;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ProgressDialog progressDialog;
    Face[] faces;

    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    User userFromRTDB;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);

        init();
    }

    // config all things
    private void init()
    {
        btnOpenCamera = findViewById(R.id.btn_open_camera);
        btnOpenCamera.setOnClickListener(this);
        sp = getSharedPreferences("data", 0);
        editor = sp.edit();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        getCurrentUserFromRTDB(); // RTDB is realtime database
        handleImage();
    }

    private void getCurrentUserFromRTDB()
    {
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                userFromRTDB = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    public void onClick(View v)
    {
        permission();//בקשת הרשאה
        if (isGranted)
            takePicture();
    }

    //open the camera and the user should photograph their face
    private void takePicture()
    {
        Intent intentToCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activityResultLauncher.launch(intentToCamera);
    }

    //handling the image
    private void handleImage()
    {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>()
        {
            @Override
            public void onActivityResult(ActivityResult result)
            {
                progressDialog = new ProgressDialog(FaceRecognitionActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.show();

                //if everything is ok
                if (result.getResultCode() == RESULT_OK && result.getData() != null)
                {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    ByteArrayInputStream inputStream = BitmapToOutputStream(bitmap);

                    Handler handler = new Handler(new Handler.Callback()
                    {
                        @Override
                        public boolean handleMessage(@NonNull Message message)
                        {
                            progressDialog.dismiss();

                            if (message.what == Helper.SUCCESS_CODE)
                            {
                                faces = (Face[]) message.obj;
                                identify(faces);
                            }
                            else if (message.what == Helper.ERROR_CODE)
                            {
                                Helper.showError(String.valueOf(message.obj), FaceRecognitionActivity.this);
                            }
                            return true;
                        }
                    });

                    //start the detection thread
                    DetectThread detectThread = new DetectThread(handler, inputStream);
                    detectThread.start();
                }
            }
        });
    }

    private void identify(Face[] faces)
    {
        if(isOnlyOneFaceDetected(faces))
        {
            Handler handler = new Handler(new Handler.Callback()
            {
                @Override
                public boolean handleMessage(@NonNull Message message)
                {
                    if(message.what == Helper.SUCCESS_CODE)
                    {
                        IdentifyResult[] identifyResults = (IdentifyResult[]) message.obj;
                        isVerified(identifyResults);
                    }
                    else if(message.what == Helper.ERROR_CODE)
                    {
                        Helper.showError("שגיאה. אנא נס/י שוב מאוחר יותר", FaceRecognitionActivity.this);
                    }
                    return true;
                }
            });

            IdentifyThread identifyThread = new IdentifyThread(handler, faces);
            identifyThread.start();
        }
    }

    int count = 0; //the user has only 3 chances to be identified
    //check if the identified person is equals to the one saved on firebase
    private void isVerified(IdentifyResult[] identifyResults)
    {
        if(identifyResults[0].candidates.get(0).personId.equals(userFromRTDB.azurePersonId))
        {
            //user identified successfully. move to voting activity
            Intent intentToVotingActivity = new Intent(FaceRecognitionActivity.this, VotingActivity.class);
            startActivity(intentToVotingActivity);
        }
        else
        {
            if(count <= 3)
            {
                //if it's the first error, it's just saying try again
                if(count == 0)
                {
                    count++;
                    Helper.showError("לא זוהית. אנא נס/י שוב", FaceRecognitionActivity.this);
                }
                else //if it's the second of third error, it's saying how much chances where left
                {
                    count++;
                    Helper.showError(" ניסיונות. לאחר הניסיון השלישי תיחסם להצבעה מהאפליקציה" + count + "לא זוהית. יש לך עוד ", FaceRecognitionActivity.this);
                }

            }
            else
            {
                //block current user from voting
                databaseReference.child(userFromRTDB.databaseKey).child("isBlocked").setValue(true);
                //move to blocked activity
                Intent intentToBlockedUsers = new Intent(FaceRecognitionActivity.this, BlockedUsersActivity.class);
                startActivity(intentToBlockedUsers);
            }
        }
    }

    private boolean isOnlyOneFaceDetected(Face[] faces)
    {
        if (faces.length == 0)
        {
            Helper.showError("לא זוהו פנים. אנא נסה/י שוב", FaceRecognitionActivity.this);
        }
        else if(faces.length > 1)
        {
            Helper.showError("אנא צלם/י רק אדם אחד", FaceRecognitionActivity.this);
        }
        return true;
    }

    private ByteArrayInputStream BitmapToOutputStream(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }


    public void dialog()//בונה את הדיאלוג אם המשתמש לא נתן הרשאה ומנסה עוד פעם לשלוח הודעה
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("על מנת להזדהות, יש לאשר הרשאה למצלמה");//הסבר למשתמש
        builder.setTitle("נדרשת הרשאה למצלמה");
        builder.setCancelable(true);

        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)//אם המשתמש לוחץ allow
            {
                ActivityCompat.requestPermissions(FaceRecognitionActivity.this, new String[]{Manifest.permission.CAMERA}, 200);//מבקש הרשאה
            }
        });

        builder.setNegativeButton(
                "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)//אם המשתמש לוחץ no
                    {
                        dialog.cancel();//סוגר את הדיאלוג
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();//מציד את הדיאלוג
    }

    public void permission() {//פעולה שמטפלת בהרשאה
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {//אם אין הרשאה
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))//אם זה פעם שניה שמבקשים הרשאה
                dialog();
            else if (!sp.getBoolean("firstCheckPermission", false)) {//אחרת אם זה פעם ראשונה שמבקשים הרשאה
                ActivityCompat.requestPermissions(FaceRecognitionActivity.this, new String[]{Manifest.permission.CAMERA}, 100);//מבקש הרשאה
                editor.putBoolean("firstCheckPermission", true);
                editor.commit();
            } else {//אם זה כבר פעם שלישית מפנים את המשתמש להגדרות של האפליקציה
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);//הגדרת הintent להגדרות של האפליקציה
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);//הפניה להגדרות של האפליקציה הנוכחית
                this.startActivity(intent);
            }
        } else//יש הרשאה
            isGranted = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {//פעולה שמופעלת אחרי בקשת הרשאה
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)//אם המשתמש אישר
                isGranted = true;
            else//המשתמש לא אישר
                isGranted = false;
        }
        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isGranted = true;
            } else
                isGranted = false;
        }
    }
}