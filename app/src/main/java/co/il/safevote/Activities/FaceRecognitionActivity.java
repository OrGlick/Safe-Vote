package co.il.safevote.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
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
import java.util.UUID;

import co.il.safevote.Helper;
import co.il.safevote.R;
import co.il.safevote.Threards.DetectThread;
import co.il.safevote.Threards.IdentifyThread;
import co.il.safevote.User;

public class FaceRecognitionActivity extends AppCompatActivity implements View.OnClickListener
{
    Button btnOpenCamera;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    boolean isGranted = false;
    int countOfErrors;
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

        getSupportActionBar().hide(); // remove action bar
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
        databaseReference = firebaseDatabase.getReference("Users/"+firebaseUser.getUid());
        getCurrentUserFromRTDB(); // RTDB is realtime database
        handleImage();
        progressDialog = new ProgressDialog(FaceRecognitionActivity.this);
        countOfErrors = 6; // the user has only 3 chances to be identified
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
                //if everything is ok
                if (result.getResultCode() == RESULT_OK && result.getData() != null)
                {
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("מזהה...");
                    progressDialog.show();
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    ByteArrayInputStream inputStream = bitmapToInputStream(bitmap);
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
                                Helper.showError(String.valueOf(message.obj), FaceRecognitionActivity.this);
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

    // check how much faces where detected by Azure
    private boolean isOnlyOneFaceDetected(Face[] faces)
    {
        if(countOfErrors > 1)
        {
            //no face detected
            if (faces.length == 0)
            {
                countOfErrors--;
                String error = "לא זוהו פנים. יש לך עוד " + countOfErrors +" ניסיונות, ולאחר הניסיון השישי תיחסם/י להצבעה מהאפליקציה. אנא נסה/י שוב";
                Helper.showError(error, FaceRecognitionActivity.this);
                return false;
            }
            else if(faces.length > 1) //more than 1 face detected
            {
                countOfErrors--;
                String error = "אנא צלם/י רק אדם אחד. יש לך עוד " + countOfErrors +" ניסיונות, ולאחר הניסיון השישי תיחסם/י להצבעה מהאפליקציה. אנא נסה/י שוב";
                Helper.showError(error, FaceRecognitionActivity.this);
                return false;
            }
            //only 1 face detected. continue the process
            return true;
        }
        else
            blockUser();
        return false;
    }

    //check if the identified person is equals to the one saved on firebase
    private void isVerified(IdentifyResult[] identifyResults)
    {
        if(somethingIdentified(identifyResults))
        {
            if(identifyResults[0].candidates.get(0).personId.equals(UUID.fromString(userFromRTDB.getAzurePersonId())))
            {
                //user identified successfully. moving to voting activity
                Intent intentToVotingActivity = new Intent(FaceRecognitionActivity.this, VotingActivity.class);
                startActivity(intentToVotingActivity);
            }
            else
                handleTheErrors();
        }
        else
            handleTheErrors();
    }

    //check if the face was identified
    private boolean somethingIdentified(IdentifyResult[] identifyResults)
    {
        return identifyResults[0].candidates.size() != 0;
    }

    //handle the error, and block the user if needed
    private void handleTheErrors()
    {
        if(countOfErrors != 0)
        {
            //if it's the first error, it's just saying try again
            if(countOfErrors == 6)
            {
                countOfErrors--;
                Helper.showError("אין זיהוי. אנא נס/י שוב", FaceRecognitionActivity.this);
            }
            else //if it's not the first error, it's saying how much chances where left
            {
                countOfErrors--;
                String error = "לא זוהית. יש לך עוד " + countOfErrors +" ניסיונות, ולאחר הניסיון השישי תיחסם/י להצבעה מהאפליקציה. אנא נסה/י שוב";
                Helper.showError( error , FaceRecognitionActivity.this);
            }
        }
        else
            blockUser(); // block the user from voting
    }

    //block current user from voting from any device
    private void blockUser()
    {
        databaseReference.child("isBlocked").setValue(true);
        //move to blocked activity
        Intent intentToBlockedUsers = new Intent(FaceRecognitionActivity.this, BlockedUsersActivity.class);
        startActivity(intentToBlockedUsers);
    }

    //convert the bitmap to ByteArrayInputStream
    private ByteArrayInputStream bitmapToInputStream(Bitmap bitmap)
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
            }
            else //אם זה כבר פעם שלישית מפנים את המשתמש להגדרות של האפליקציה
            {
                askTheUserToGoToSetting();
            }
        } else//יש הרשאה
            isGranted = true;
    }

    //ask the user with a dialog to open the setting page of the app, so he can give the app the camera permission
    private void askTheUserToGoToSetting()
    {
        new AlertDialog.Builder(this)
                .setTitle("דרושה הרשאה")
                .setMessage("על מנת לזהות את פניך נדרשת הרשאה למצלמה. בלעדיה לא תוכל להצביע")
                .setPositiveButton("קח אותי להגדרות האפליקציה", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.dismiss();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);//הגדרת הintent להגדרות של האפליקציה
                        Uri uri = Uri.fromParts("package", FaceRecognitionActivity.this.getPackageName(), null);
                        intent.setData(uri);//הפניה להגדרות של האפליקציה הנוכחית
                        FaceRecognitionActivity.this.startActivity(intent);
                    }
                })
                .setNegativeButton("סגור", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {//פעולה שמופעלת אחרי בקשת הרשאה
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