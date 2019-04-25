package com.jimdo.dominicdj.midiswap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.jimdo.dominicdj.midiswap.USB.MyUsbDeviceConnection;
import com.jimdo.dominicdj.midiswap.USB.UsbCommunicationManager;
import com.jimdo.dominicdj.midiswap.Utils.Conversion;

public class Operations extends AppCompatActivity {

    private static final String TAG = Operations.class.getSimpleName();

    private EditText inputMsgEditText;
    private EditText outputMsgEditText;
    private EditText customMsgEditText;

    // without whitespaces, extra characters, all in UPPERCASE
    private String inputMsg;
    private String outputMsg;

    private boolean midiServiceStarted = false;

    private final BroadcastReceiverWithFlag usbReceiver = new BroadcastReceiverWithFlag() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onBackPressed(); // return to MainActivity
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operations);

        inputMsgEditText = findViewById(R.id.edit_text_input_msg);
        outputMsgEditText = findViewById(R.id.edit_text_output_msg);
        customMsgEditText = findViewById(R.id.edit_text_custom_msg);
        restrictText();
        usbReceiver.register(this, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
    }

    private void restrictText() {
        // TODO: adjust input filters so that there are now unnecessary spaces when the user wants to add a hex
        //  character not at the end, but in the middle of the text (caret position not at the end)
        InputFilter[] filters = new InputFilter[2];
        filters[0] = new InputFilter.AllCaps();
        filters[1] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                // although there is a AllCaps-filter include small caps letters as well in this regex
                if (source.length() == 0 || source.toString().matches("[ \\da-fA-FxX]+")) {
                    return null; // accept the original replacement
                } else {
                    return ""; // do not accept
                }
            }
        };
        inputMsgEditText.setFilters(filters);
        outputMsgEditText.setFilters(filters);

        InputFilter[] filtersCustomMsg = new InputFilter[2];
        filtersCustomMsg[0] = new InputFilter.AllCaps();
        filtersCustomMsg[1] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                // although there is a AllCaps-filter include small caps letters as well in this regex
                if (source.length() == 0 || source.toString().matches("[ \\da-fA-F]+")) {
                    return null; // accept the original replacement
                } else {
                    return ""; // do not accept
                }
            }
        };
        customMsgEditText.setFilters(filtersCustomMsg);

        // Format in HEX code with whitespace after every second character
        addTextWatcher(inputMsgEditText);
        addTextWatcher(outputMsgEditText);
        addTextWatcher(customMsgEditText);
    }

    private void addTextWatcher(final EditText editText) {
        TextWatcher hexTextWatcherOut = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // remove spacing char except if at favored position (in front of every third character)
                if ((s.length() % 3) != 0) {
                    if (s.charAt(s.length() - 1) == ' ') {
                        s.delete(s.length() - 1, s.length());
                    }
                }
                // Insert space where needed: in front of every third character
                if (s.length() > 0 && (s.length() % 3) == 0 && s.charAt(s.length() - 1) != ' ') {
                    s.insert(s.length() - 1, " ");
                }
            }
        };
        editText.addTextChangedListener(hexTextWatcherOut);
    }

    public void startMidiHandler(View v) {
        // start the MidiService only one time, even when user presses button repeatedly
        // TODO: grey out button in this case (midiService already started)
        if (!midiServiceStarted) {
            Intent midiServiceStartIntent = new Intent(this, MidiHandlerService.class);
            ComponentName serviceName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                serviceName = startForegroundService(midiServiceStartIntent);
            } else {
                serviceName = startService(midiServiceStartIntent);
            }
            Log.d(TAG, "Midi Handler was started. Service is: " + serviceName);
            midiServiceStarted = true;
        }
    }

    public void stopMidiHandlerFromView(View view) {
        stopMidiHandler();
    }

    private void stopMidiHandler() {
        // stop the MidiService only if it has been started
        if (midiServiceStarted) {
            Intent midiServiceStopIntent = new Intent(this, MidiHandlerService.class);
            boolean couldStopService = stopService(midiServiceStopIntent);
            if (couldStopService) {
                midiServiceStarted = false;
                Log.d(TAG, "MIDI Handler was successfully stopped.");
            } else {
                Log.d(TAG, "MIDI Handler couldn't be stopped.");
            }
        }
    }

    public void onSendMessage(View v) {
        int id = v.getId();
        String hexMessage = "1991257A"; // Standard message
        if (id == R.id.btn_send_on) {
            hexMessage = "19913A7A"; // NOTE ON, channel 1, note 3A, volume: 7A
        } else if (id == R.id.btn_send_off) {
            hexMessage = "18813A7A";
        } else if (id == R.id.btn_send_msg) {
            hexMessage = customMsgEditText.getText().toString().replaceAll("\\s+", "").toUpperCase();
        }

        final MyUsbDeviceConnection myUsbDeviceConnection = UsbCommunicationManager.getMyUsbDeviceConnection();
        if (myUsbDeviceConnection == null) {
            Log.d(TAG, "myUsbDeviceConnection is null, so we can't send any data.");
            return;
        }

        final String finalHexMessage = hexMessage;
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean sendSucceeded = false;
                try {
                    sendSucceeded = myUsbDeviceConnection.send(Conversion.toByteArray(finalHexMessage));
                } catch (MyUsbDeviceConnection.CalledFromWrongThreadException e) {
                    e.printStackTrace();
                }

                final boolean finalSendSucceeded = sendSucceeded;
                Operations.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalSendSucceeded) {
                            Toast.makeText(getApplicationContext(), "Sent message", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Couldn't send message", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();

    }

    @Override
    public void onBackPressed() {
        stopMidiHandler();
        Toast.makeText(this, "Connection closed", Toast.LENGTH_SHORT).show();
        stopMidiHandler();
        // we don't need the myUsbDeviceConnection anymore, so remove it
        UsbCommunicationManager.closeMyUsbDeviceConnection();
        super.onBackPressed(); // important, so that this activity finishes and frees up resources
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // take the user back, as if they pressed the left-facing triangle icon on the main android toolbar
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateMsg(View view) {
        // Check user input
        // Remove all whitespaces and non-visible characters (e. g. tab, \n) and only use small caps
        // Although we already restricted the editorTexts to only allow UPPERCASE, it doesn't hurt to convert
        // everything to UPPERCASE letters again (Better safe than sorry...)
        String inputMsgRaw = inputMsgEditText.getText().toString().replaceAll("\\s+", "").toUpperCase();
        String outputMsgRaw = outputMsgEditText.getText().toString().replaceAll("\\s+", "").toUpperCase();

        if (inputMsgRaw.length() < 6 || outputMsgRaw.length() < 6) {
            Toast.makeText(this, "Each of the massages has to be at least three bytes long!",
                    Toast.LENGTH_LONG).show();
        } else {
            int inCount = countChar(inputMsgRaw, 'X');
            if (inCount > 2) {
                Toast.makeText(this, "Too many 'X's in your input message!",
                        Toast.LENGTH_LONG).show();
                return;
            }
            inputMsg = inputMsgRaw;
            outputMsg = outputMsgRaw;
            OperationRules.updateRule(inputMsg, outputMsg);
            Toast.makeText(this, "Rule updated", Toast.LENGTH_SHORT).show();
        }
    }

    private int countChar(String string, Character charToCompare) {
        int count = 0;
        for (Character c : string.toCharArray()) {
            if (c == charToCompare) {
                count++;
            }
        }
        return count;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usbReceiver.unregister(this);
        stopMidiHandler();
        // we don't need the myUsbDeviceConnection anymore, so remove it
        // note that we don't close the myUsbDeviceConnection or stop the MIDI Handler in onStop()
        // because onStop() is called when the app becomes invisible and we want that MIDI processing to be
        // active, even if the app isn't in focus anymore (e. g. the device screen is turned off)
        UsbCommunicationManager.closeMyUsbDeviceConnection();
    }
}