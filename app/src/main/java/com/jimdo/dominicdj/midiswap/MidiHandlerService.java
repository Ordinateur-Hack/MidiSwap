package com.jimdo.dominicdj.midiswap;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

public class MidiHandlerService extends IntentService {

    // some random values to identify the notification
    private static final int ONGOING_NOTIFICATION_ID = 42; // must not be 0!
    private static final String NOTIFICATION_CHANNEL_ID_MIDI_HANDLER = "com.jimdo.dominicdj.CHANNEL_MIDI_HANDLER";

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public MidiHandlerService() {
        // used to name the worked thread, important only for debugging
        super(MidiHandlerService.class.getSimpleName());
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        // ==========================================================================
        // Run our service in the foreground
        // ==========================================================================
        Intent notificationIntent = new Intent(this, Operations.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent myActivityPendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createMidiHandlerChannel();
            notificationBuilder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID_MIDI_HANDLER);
        } else {
            notificationBuilder = new Notification.Builder(this);
            notificationBuilder.setPriority(Notification.PRIORITY_LOW);
        }
        Notification notification = notificationBuilder
                .setContentIntent(myActivityPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getText(R.string.midi_handler_notification_title))
                .setContentText(getText(R.string.midi_handler_notification_message))
                .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Although we don't need this feature in our app, we have to create a new channel for the notification to
     * work in new Android versions (API >= 26)
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createMidiHandlerChannel() {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_MIDI_HANDLER, "Midi Handler",
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        channel.enableLights(false);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
        // TODO: what to do otherwise (if notificationManager is null)??
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // ==========================================================================
        // Receive and send MIDI data
        // ==========================================================================
        // do something in here
        while (true) {
            // TODO: when to stop midi processing, it has to be stopped!!
        }
    }
}
