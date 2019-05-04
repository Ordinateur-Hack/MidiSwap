package com.jimdo.dominicdj.midiswap;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;
import com.jimdo.dominicdj.midiswap.USB.MyUsbDeviceConnection;
import com.jimdo.dominicdj.midiswap.USB.UsbCommunicationManager;
import com.jimdo.dominicdj.midiswap.Utils.Conversion;
import com.jimdo.dominicdj.midiswap.midimessage.MidiChannelMessage;
import com.jimdo.dominicdj.midiswap.operationrules.OperationRule;
import com.jimdo.dominicdj.midiswap.operationrules.OperationRulesManagerOld;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MidiHandlerService extends IntentService {

    // some random values to identify the notification
    private static final int ONGOING_NOTIFICATION_ID = 42; // must not be 0!
    private static final String NOTIFICATION_CHANNEL_ID_MIDI_HANDLER = "com.jimdo.dominicdj.CHANNEL_MIDI_HANDLER";

    private MyUsbDeviceConnection myUsbDeviceConnection;

    private ScheduledFuture<?> midiSchedule;
    private static final int SCHEDULE_RATE_NANO = 600;

    private static final String TAG = MidiHandlerService.class.getSimpleName();


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
        // Run our service in the foreground and build a notification
        // ==========================================================================
        if (intent == null) {
            Log.d(TAG, "Intent is null");
            return super.onStartCommand(intent, flags, startId);
        }

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
                .setSmallIcon(R.drawable.ic_import_export_black_24dp)
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
        if (intent == null) {
            return;
        }
        // ==========================================================================
        // Receive and send MIDI data
        // ==========================================================================
        myUsbDeviceConnection = UsbCommunicationManager.getMyUsbDeviceConnection();
        if (myUsbDeviceConnection == null) {
            Log.d(TAG, "myUsbDeviceConnection is null, so we can't process the data");
            Toast.makeText(this.getApplicationContext(), "Something went wrong with the USB-communication ;(",
                    Toast.LENGTH_LONG).show();
            stopSelf();
        }

        // Use the ExecutorService to schedule processData() to execute periodically
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final Runnable midiUsbRequest = new Runnable() {
            @Override
            public void run() {
                processData();
            }
        };
        midiSchedule = scheduler.scheduleAtFixedRate(midiUsbRequest, 0, SCHEDULE_RATE_NANO,
                TimeUnit.NANOSECONDS);
        while (true) {
            // do this in order to keep the MidiHandlerService alive; otherwise it would stop itself automatically
        }
    }

    /**
     * Process the data. This is basically the main logic of the app.<br>
     * It follows this procedure:
     * <ol>
     * <li>Receive the data from the USB-device (using the {@link MyUsbDeviceConnection})</li>
     * <li>Process the data by getting the appropriate {@link OperationRulesManagerOld}</li>
     * <li>Send data back to the USB-device (using the {@link MyUsbDeviceConnection})</li>
     * </ol>
     */
    private void processData() {
        Log.d(TAG, "in process data");
        // ==========================================================================
        // 1. Receive data
        // ==========================================================================
        // we get a response via our onFinishedReceiveHandler including the received bytes
        ByteBuffer receivedBytes = null;
        try {
            receivedBytes = myUsbDeviceConnection.receive();
        } catch (MyUsbDeviceConnection.CalledFromWrongThreadException e) {
            e.printStackTrace();
        }
        // Return if we didn't receive any bytes.
        if (receivedBytes == null) {
            return;
        }

        // ==========================================================================
        // 2. Process the data
        // ==========================================================================
        String data = Conversion.toHexString(receivedBytes.array());
        Log.d(TAG, data);
        // regex "\\s+" matches sequence of one or more whitespace characters
        data = data.replaceAll("\\s+", "");

        // Go through all OperationRules.
        List<OperationRule> operationRules = OperationRule.OperationRulesManager.getOperationRules();
        Log.d(TAG, "OperationRules are: " + operationRules.toString());
        for (OperationRule operationRule : operationRules) {
            // Go through all receive MidiMessages.
            List<MidiChannelMessage> recvMessages = operationRule.getMidiRecvMessages();
            for (MidiChannelMessage recvMessage : recvMessages) {
                String ifRecvMsg = recvMessage.getHexMessage();
                Log.d(TAG, "ifRecvMsg is: " + ifRecvMsg);

                // Construct the sendMessage by concatenating all of the send messages.
                StringBuilder thenSendMsgBuilder = new StringBuilder();
                for (MidiChannelMessage sendMessage : operationRule.getMidiSendMessages()) {
                    thenSendMsgBuilder.append(sendMessage.getHexMessage());
                }
                String thenSendMsg = thenSendMsgBuilder.toString();
                // TODO: can we assume that midiSendMessag is not null at this point?

                // === How it works ===
                // 1. Search for 'XX' pattern in ifRecvMsg and replace it with our regex pattern for hex messages.
                //    Save the position of this 'XX' value.
                // 2. Search the input data (received from USB-device) for ifRecvMsg.
                //    If data was found, extract the data value (e. g. a controller value ranging from 0 to 127)
                // 3. Search for 'XX' pattern in thenSendMsg and replace it with the data value of the previous step.
                //    Finally, send the message to the USB-device.
                // ====================

                // Work with ifRecvMsg (1.)
                Matcher substituteX = Pattern.compile("XX").matcher(ifRecvMsg);
                int subIndex = 0;
                if (substituteX.find()) {
                    subIndex = substituteX.start();
                    ifRecvMsg = ifRecvMsg.replace("XX", "[\\da-fA-F]{2}");
                    Log.d(TAG, "Regex pattern for input: " + ifRecvMsg);
                }

                // Work with data received from USB-device. (2.)
                Matcher m = Pattern.compile(ifRecvMsg).matcher(data);
                String matchedData = null;
                boolean hitEnd = false;
                while (!hitEnd) {
                    if (m.find()) {
                        matchedData = m.group();
                    } else {
                        // matcher.hitEnd() method refers to the input regex, not the input data,
                        // so we can't use this method here.
                        // Instead, we just check the boolean return value from matcher.find().
                        // We reached the end, if it returns false.
                        hitEnd = true;
                    }
                }

                // If we have found an input sequence that matches the pattern, get the bytes
                // at the position of the 'XX' pattern in ifRecvMsg. (still 2.)
                if (matchedData != null) {
                    String dataValueToReplace = matchedData.substring(subIndex, subIndex + 2);
                    // Replace 'XX' in thenSendMsg with respective value in input data (received from USB-device)
                    // at the position of 'XX' in ifRecvMsg.
                    String msg = thenSendMsg.replace("XX", dataValueToReplace); // replace with previous data (3.)
                    // TODO: check for XX values in output
                    // TODO: check if there is after all a dataValuetoReplace (if there is XX in input)
                    // ==========================================================================
                    // 3. Send data back
                    // ==========================================================================
                    try {
                        myUsbDeviceConnection.send(Conversion.toByteArray(msg));
                    } catch (MyUsbDeviceConnection.CalledFromWrongThreadException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (midiSchedule != null) {
            midiSchedule.cancel(true);
        }
    }

}
