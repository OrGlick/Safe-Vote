package co.il.safevote;

import android.os.Handler;
import android.os.Message;

import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class DetectThread extends Thread
{
    Handler handler;
    ByteArrayInputStream byteArrayInputStream;

    public DetectThread(Handler handler, ByteArrayInputStream byteArrayInputStream)
    {
        this.handler = handler;
        this.byteArrayInputStream = byteArrayInputStream;
    }

    @Override
    public void run()
    {
        super.run();
        MyFaceClient myFaceClient = new MyFaceClient();
        Message message = new Message();
        /*
        try
        {
            Face[] faces = myFaceClient.faceServiceClient.detect(byteArrayInputStream, true, false, null);
            message.what = Helper.SUCCESS_CODE;
            message.obj = faces;
        }
        catch (ClientException | IOException e)
        {
            message.what = Helper.ERROR_CODE;
            message.obj = e;
        }
        handler.sendMessage(message);

         */
    }
}
