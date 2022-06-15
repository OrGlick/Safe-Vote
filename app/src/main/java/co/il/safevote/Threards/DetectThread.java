package co.il.safevote.Threards;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import co.il.safevote.AzureCreds;
import co.il.safevote.Helper;

public class DetectThread extends Thread
{
    Handler handler;
    ByteArrayInputStream byteArrayInputStream;
    FaceServiceClient faceServiceClient;

    public DetectThread(Handler handler, ByteArrayInputStream byteArrayInputStream)
    {
        this.handler = handler;
        this.byteArrayInputStream = byteArrayInputStream;
    }

    @Override
    public void run()
    {
        super.run();

        // get Azure creds from firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("AzureCreds");
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                AzureCreds creds = snapshot.getValue(AzureCreds.class);
                faceServiceClient = new FaceServiceRestClient(creds.getEndPoint(), creds.getApiKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {}
        });

        //wait until we get the Azure creds from firebase and create the faceServiceClient
        //if we're still waiting for the creds, faceServiceClient we be null
        while (faceServiceClient == null)
        {
            try
            {
                //wait 10 milliseconds
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        Message message = new Message();
        try
        {
            // detect face
            Face[] faces = faceServiceClient.detect(byteArrayInputStream, true, false, null);
            message.what = Helper.SUCCESS_CODE;
            message.obj = faces;
        }
        catch (ClientException | IOException e)
        {
            message.what = Helper.ERROR_CODE;
            message.obj = e;
        }
        handler.sendMessage(message);
    }
}
