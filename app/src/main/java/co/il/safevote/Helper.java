package co.il.safevote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Helper
{
    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 400;
    public static final String PERSON_GROUP_ID = "group_id";

    public static void showError(String message, Context context)
    {
        new AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("בסדר", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }
}
