package co.il.safevote;

import android.os.Handler;
import android.os.Message;

import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.IOException;
import java.util.UUID;

public class IdentifyThread extends Thread
{
    Handler handler;
    Face[] faces;

    public IdentifyThread(Handler handler, Face[] faces)
    {
        this.handler = handler;
        this.faces = faces;
    }


    @Override
    public void run()
    {
        super.run();

        UUID [] uuids = {faces[0].faceId};
        Message message = new Message();

        MyFaceClient myFaceClient = new MyFaceClient();
        try
        {
            IdentifyResult[] identifyResult =  myFaceClient.faceServiceClient.identity(Helper.PERSON_GROUP_ID, uuids, 1);
            message.what = Helper.SUCCESS_CODE;
            message.obj = identifyResult;
        }
        catch (ClientException | IOException e)
        {
            message.what = Helper.ERROR_CODE;
            message.obj = e;
        }

        handler.sendMessage(message);
    }
}
