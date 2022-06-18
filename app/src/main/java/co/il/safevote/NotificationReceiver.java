package co.il.safevote;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import co.il.safevote.Activities.LoginActivity;

public class NotificationReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent1)
    {
        String text = "האפליקציה נפתחה להצבעה. בואו להשפיע!";
        long when = System.currentTimeMillis();
        String title = "יום הבחירות החל";

        Intent intent = new Intent(context, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "M_CH_ID");

        String channelId = "CHANNEL_ID";
        NotificationChannel channel = new NotificationChannel(channelId,
                "Channel title",
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
        builder.setChannelId(channelId);

        Notification notification = builder.setContentIntent(pendingIntent)
                .setWhen(when).setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true).setContentTitle(title)
                .setContentText(text).build();
        notificationManager.notify(1, notification);
    }
}
