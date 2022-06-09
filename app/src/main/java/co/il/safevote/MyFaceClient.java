package co.il.safevote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;

public class MyFaceClient
{
     FaceServiceClient faceServiceClient;
     AzureCreds creds;
    String tag = "TAG";
    public MyFaceClient()
    {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://elections-system-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference databaseReference = firebaseDatabase.getReference("AzureCreds");
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                creds = snapshot.getValue(AzureCreds.class);
                Log.d("TAG", creds.getApiKey() + creds.getEndPoint());
                faceServiceClient = new FaceServiceRestClient(creds.getEndPoint(), creds.getApiKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {}
        });
        Log.d(tag, "api key: "+creds.getApiKey());
    }
}
