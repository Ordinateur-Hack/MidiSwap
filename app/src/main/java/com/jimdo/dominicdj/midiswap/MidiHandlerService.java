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

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MidiHandlerService extends IntentService {

    // some random values to identify the notification
    private static final int ONGOING_NOTIFICATION_ID = 42; // must not be 0!
    private static final String NOTIFICATION_CHANNEL_ID_MIDI_HANDLER = "com.jimdo.dominicdj.CHANNEL_MIDI_HANDLER";

    MyUsbDeviceConnection myUsbDeviceConnection;

    private MidiUsbRequest midiUsbRequest;
    private Timer recvTimer;

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
        myUsbDeviceConnection = UsbCommunicationManager.getMyUsbDeviceConnection();
        if (myUsbDeviceConnection == null) {
            Log.d(TAG, "myUsbDeviceConnection is null, so we can't process the data");
            Toast.makeText(this.getApplicationContext(), "Something went wrong with the USB-communication ;(",
                    Toast.LENGTH_LONG).show();
            stopSelf();
        }
        midiUsbRequest = new MidiUsbRequest();
        recvTimer = new Timer();
        recvTimer.schedule(midiUsbRequest, 0, 10); // TODO: maybe adjust this value to fit the MIDI protocol
        while (true) {
            // do this in order to keep the MidiHandlerService alive; otherwise it would stop itself automatically
        }
    }

    private class MidiUsbRequest extends TimerTask {

        @Override
        public void run() {
            processData();
        }
    }

    /**
     * Process the data. This is basically the main logic of the program.<br>
     * It follows this procedure:
     * <ol>
     * <li>Receive the data from the USB-device-connection</li>
     * <li>Process the data by getting the appropriate {@link OperationRules}</li>
     * <li>Send data back to the USB-device using the USB-device-connection</li>
     * </ol>
     */
    private void processData() {
        // ==========================================================================
        // Receive data
        // ==========================================================================
        ByteBuffer receivedBytes = myUsbDeviceConnection.receive();
        String data = Conversion.toHexString(receivedBytes.array());
        Log.d(TAG, data);
        // regex "\\s+" matches sequence of one or more whitespace characters
        data = data.replaceAll("\\s+", "");

        // ==========================================================================
        // Process the data
        // ==========================================================================
        String[] rule = OperationRules.getRule();
        String ifRecvRegex = rule[0];
        String thenSendMsg = rule[1];

        // NOTES
        /*Pattern p = Pattern.compile("9\\d [0-9a-fA-F]{2} [0-9a-fA-F]{2}"); // e. g. 90 XX XX
        Matcher m = p.matcher(data);
        if (m.find()) {
            int startIndex = m.start();
            String noteValue = data.substring(startIndex + 3, startIndex + 5);
            Log.d(TAG, "Keyy pressed: " + data);
            Log.d(TAG, "Note: " + noteValue);

            if (!data.substring(startIndex + 5, startIndex + 7).equals("00")) { // pressure
                String msg = "1BB14A" + noteValue;
                usbCommunicationManager.send(Conversion.toByteArray(msg));
            }
        }*/

        // MODULATION WHEEL
        /*Pattern p = Pattern.compile("(?i)B1 01 [\\da-f]{2}"); // e. g. B1 01 XX (00-7F)
        Matcher m = p.matcher(data);
        if (m.find()) {
            String modulationData = m.group();
            Log.d(TAG, "Found modulation wheel byte: " + modulationData);
            String hexValue = modulationData.substring(6, 8);
            String msg = "1BB14A" + hexValue;
            usbCommunicationManager.send(Conversion.toByteArray(msg));
        }*/

        // CUSTOM
        // don't do anything if user hasn't already set inputMsg or outputMsg
        if (ifRecvRegex == null || thenSendMsg == null) {
            return;
        }

        // Check for XX values in input and adjust regex pattern accordingly
        Matcher substituteX = Pattern.compile("XX").matcher(ifRecvRegex);
        Log.d(TAG, "Regex pattern input (before modification): " + ifRecvRegex);
        int subIndex = 0;
        if (substituteX.find()) {
            subIndex = substituteX.start();
            ifRecvRegex = ifRecvRegex.replace("XX", "[\\da-fA-F]{2}");
            Log.d(TAG, "Regex pattern for input (after modification): " + ifRecvRegex);
        }

        // Search for ifRecvRegex in data received from USB-device
        Matcher m = Pattern.compile(ifRecvRegex).matcher(data);
        if (m.find()) {
            String dataValueToReplace = m.group().substring(subIndex, subIndex + 2);
            // TODO: check for XX values in output
            // in case: replace XX in output with respective value in data (usb) at position of XX in input
            // TODO: check if there is after all a dataValuetoReplace (if there is XX in input)
            String msg = thenSendMsg.replace("XX", dataValueToReplace);

            // ==========================================================================
            // Send data back
            // ==========================================================================
            myUsbDeviceConnection.send(Conversion.toByteArray(msg));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recvTimer != null)
            recvTimer.cancel();
        if (midiUsbRequest != null)
            midiUsbRequest.cancel();
    }

}
