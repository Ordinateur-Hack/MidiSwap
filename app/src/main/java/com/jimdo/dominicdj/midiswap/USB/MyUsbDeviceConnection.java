package com.jimdo.dominicdj.midiswap.USB;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telecom.Call;
import android.util.Log;
import com.jimdo.dominicdj.midiswap.Utils.Conversion;

import java.nio.ByteBuffer;

/**
 * Defines the connection to a specific USB-device and allows to communicate with it.
 */
public class MyUsbDeviceConnection {

    private UsbDeviceConnection connection;
    private UsbInterface intf; // only used to release resources
    private UsbEndpoint sendEndpoint; // from android to USB-device
    private UsbEndpoint receiveEndpoint; // from USB-device to android

    private static int TIMEOUT = 3000;

    // some random values for the handlers
    public static final int SEND_SUCCESS_CHECK = 148;

    private static final String TAG = MyUsbDeviceConnection.class.getSimpleName();


    public MyUsbDeviceConnection(UsbDeviceConnection connection, UsbInterface intf, UsbEndpoint sendEndpoint,
                                 UsbEndpoint receiveEndpoint) {
        this.connection = connection;
        this.intf = intf;
        this.sendEndpoint = sendEndpoint;
        this.receiveEndpoint = receiveEndpoint;
    }

    /**
     * Sends a byte-array to the USB-device.<br>
     * Note that this method is executed on the calling thread. So make sure not to use the UI thread,
     * instead created a worker trhead. An exception is thrown if you do not respect this.
     * @param bytes the bytes to send
     * @return true if successfully sent bytes, false otherwise
     * @throws CalledFromWrongThreadException if called from UI thread
     */
    public boolean send(final byte[] bytes) throws CalledFromWrongThreadException {
        // maybe helpful:
        // https://stackoverflow.com/questions/42023388/no-response-from-android-bulktransfer-with-proper-endpoints
        // https://stackoverflow.com/questions/12345953/android-usb-host-asynchronous-interrupt-transfer
        if (Looper.myLooper() == Looper.getMainLooper()) { // if on UI thread
            throw new CalledFromWrongThreadException("This method is not allowed to be called from the UI thread.");
        }

        final String hexMessage = Conversion.toHexString(bytes);

        int sentBytes = connection.bulkTransfer(sendEndpoint, bytes, bytes.length, TIMEOUT);
        if (sentBytes >= 0) {
            Log.d(TAG, "Successfully sent " + sentBytes + " bytes. Message: " + hexMessage);
            return true;
        } else {
            Log.d(TAG, "Couldn't send " + sentBytes + " bytes. Message: " + hexMessage);
            return false;
        }
    }

    /**
     * Receives a data package from the USB-device.<br>
     * Note that this method is executed on the calling thread. So make sure not to use the UI thread,
     * instead create a worker thread. An exception is thrown if you do not respect this.
     * @return the received bytes
     * @throws CalledFromWrongThreadException if called from UI thread
     */
    public ByteBuffer receive() throws CalledFromWrongThreadException {
        if (Looper.myLooper() == Looper.getMainLooper()) { // if on UI thread
            throw new CalledFromWrongThreadException("This method is not allowed to be called from the UI thread.");
        }
        ByteBuffer buffer = ByteBuffer.allocate(512);
        UsbRequest request = new UsbRequest();
        request.initialize(connection, receiveEndpoint);

        // queue an inbound request on the bulk transfer endpoint
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            request.queue(buffer);
        } else {
            request.queue(buffer, 512);
        }
        // wait for event (confirmation that request was completed)
        if (connection.requestWait() == request) {
            Log.d(TAG, "USB request successfully completed.");
        } else {
            Log.d(TAG, "USB request failed!");
            buffer = null;
        }
        return buffer;
    }

    public void releaseUsb() {
        if (connection != null) {
            if (intf != null) {
                connection.releaseInterface(intf);
            }
            connection.close();
        }
        Log.d(TAG, "MyUsbDeviceConnection successfully closed.");
    }

    public class CalledFromWrongThreadException extends Exception {

        public CalledFromWrongThreadException(String message) {
            super(message);

        }
    }

}
