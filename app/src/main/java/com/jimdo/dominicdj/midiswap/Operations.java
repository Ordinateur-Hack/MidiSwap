package com.jimdo.dominicdj.midiswap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.jimdo.dominicdj.midiswap.midimessage.MidiChannelController;
import com.jimdo.dominicdj.midiswap.midimessage.MidiControllerBuilder;
import com.jimdo.dominicdj.midiswap.operationrules.OperationRule;
import com.jimdo.dominicdj.midiswap.ruleslist.*;

import java.util.ArrayList;
import java.util.List;

public class Operations extends AppCompatActivity {

    private EditText ifRecvMsgEditText;
    private EditText thenSendMsgEditText;
    private EditText customMsgEditText;

    private static RulesAdapter rulesAdapter;
    private FloatingActionButton addFab;

    private boolean midiServiceStarted = false;

    private static final String TAG = Operations.class.getSimpleName();

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

        ifRecvMsgEditText = findViewById(R.id.edit_text_input_msg);
        thenSendMsgEditText = findViewById(R.id.edit_text_output_msg);
        customMsgEditText = findViewById(R.id.edit_text_custom_msg);
        restrictText();
        usbReceiver.register(this, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));

        // ==========================================================================
        // RecyclerView with Spinners
        // ==========================================================================
        RecyclerView rulesRecyclerView = findViewById(R.id.recycler_view_rules);
        rulesRecyclerView.setHasFixedSize(true);
        rulesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<RulesViewModel> rulesViewModels = new ArrayList<>();
        final SpinnerListener spinnerListener = new SpinnerListener(this);
        rulesViewModels.add(generateBasicViewModel(spinnerListener));

        final RulesListener rulesListener = new RulesListener();
        rulesAdapter = new RulesAdapter(rulesViewModels, spinnerListener, rulesListener);
        rulesRecyclerView.setAdapter(rulesAdapter);

        // FloatingActionButton
        addFab = findViewById(R.id.fab_add);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rulesAdapter.addItem(generateBasicViewModel(spinnerListener));
                // TODO: only for debug purpose
                Toast.makeText(getApplicationContext(), "Added rule", Toast.LENGTH_SHORT).show();
            }
        });


        // =============================================================================================
        // TODO: Add new rulesViewModels with a button
        // rulesAdapter.removeItem(RulesViewModel removeItem)
        // =============================================================================================
    }

    public static RulesAdapter getRulesAdapter() {
        return rulesAdapter;
    }

    /**
     * Helper method to generate a basic ViewModel.
     *
     * @return
     */
    private RulesViewModel generateBasicViewModel(View.OnClickListener settingsOnClickListener) {
        OperationRule newOperationRule = OperationRule.OperationRulesManager.getNewOperationRule();
        SpinnerAdapter adapterRecv = generateSpinnerAdapter(MidiControllerBuilder.getAvailableMidiControllers(true),
                settingsOnClickListener);
        SpinnerAdapter adapterSend = generateSpinnerAdapter(MidiControllerBuilder.getAvailableMidiControllers(false),
                settingsOnClickListener);
        return new RulesViewModel(newOperationRule, adapterRecv, adapterSend);
    }

    private SpinnerAdapter generateSpinnerAdapter(List<MidiChannelController> midiChannelControllers,
                                                  View.OnClickListener settingsOnClickListener) {
        return new SpinnerAdapter(this,
                R.layout.spinner_item, R.id.tv_midi_controller_name,
                android.R.layout.simple_spinner_dropdown_item, android.R.id.text1 /*text1 is the TextView
                in the simple_spinner_dropdown_item, provided by the Android framework*/, midiChannelControllers,
                settingsOnClickListener);
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
        ifRecvMsgEditText.setFilters(filters);
        thenSendMsgEditText.setFilters(filters);

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
        addTextWatcher(ifRecvMsgEditText);
        addTextWatcher(thenSendMsgEditText);
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

    /*
    public void updateMsgFromView(View v) {
        // at the moment: do nothing

        // Check user input
        // Remove all whitespaces and non-visible characters (e. g. tab, \n) and only use small caps
        // Although we already restricted the editorTexts to only allow UPPERCASE, it doesn't hurt to convert
        // everything to UPPERCASE letters again (Better safe than sorry...)
        String ifRecvMsg = ifRecvMsgEditText.getText().toString().replaceAll("\\s+", "").toUpperCase();
        String thenSendMsg = thenSendMsgEditText.getText().toString().replaceAll("\\s+", "").toUpperCase();

        if (ifRecvMsg.length() < 6 || thenSendMsg.length() < 6) {
            Toast.makeText(this, "Each of the massages has to be at least three bytes long!",
                    Toast.LENGTH_LONG).show();
        } else {
            int inCount = StringUtil.countCharOccurrence(ifRecvMsg, 'X');
            if (inCount > 2) {
                Toast.makeText(this, "Too many 'X's in your input message!",
                        Toast.LENGTH_LONG).show();
                return;
            }

            try {
                OperationRule newOperationRule = new OperationRule(ifRecvMsg, thenSendMsg);
                boolean addOperationWorked = OperationRulesManagerOld.addOperationRule(newOperationRule);
                if (addOperationWorked) {
                    Toast.makeText(this, "Rule updated via EditText", Toast.LENGTH_SHORT).show();
                    // Delete old OperationRule.
                    if (lastOperationRuleEditText != null) {
                        OperationRulesManagerOld.deleteOperationRule(lastOperationRuleEditText);
                    }
                    lastOperationRuleEditText = newOperationRule;
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }*/

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