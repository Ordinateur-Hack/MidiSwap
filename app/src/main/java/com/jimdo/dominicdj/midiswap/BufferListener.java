package com.jimdo.dominicdj.midiswap;

import Usb.RecvBuffer;
import Usb.UsbCommunicationManager;
import Utils.Conversion;

import java.nio.ByteBuffer;


// NOT IN USE
public class BufferListener implements RecvBuffer.BufferChangeListener {

    private UsbCommunicationManager usbCommunicationManager;

    private static final String TAG = BufferListener.class.getSimpleName();


    public BufferListener(UsbCommunicationManager usbCommunicationManager) {
        this.usbCommunicationManager = usbCommunicationManager;
    }

    @Override
    public void onUpdateByte(ByteBuffer byteBuffer) {
        String data = Conversion.toHexString(byteBuffer.array());

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

        /*// MODULATION WHEEL
        Pattern p = Pattern.compile("(?i)B1 01 [\\da-f]{2}"); // e. g. B1 01 XX (00-7F)
        Matcher m = p.matcher(data);
        if (m.find()) {
            String modulationData = m.group();
            Log.d(TAG, "Found modulation wheel byte: " + modulationData);
            String hexValue = modulationData.substring(6, 8);
            String msg = "1BB14A" + hexValue;
            usbCommunicationManager.send(Conversion.toByteArray(msg));
        }*/
    }
}
