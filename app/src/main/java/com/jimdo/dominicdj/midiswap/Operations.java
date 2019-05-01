package com.jimdo.dominicdj.midiswap;

import android.content.*;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.jimdo.dominicdj.midiswap.USB.MyUsbDeviceConnection;
import com.jimdo.dominicdj.midiswap.USB.UsbCommunicationManager;
import com.jimdo.dominicdj.midiswap.Utils.Conversion;
import com.jimdo.dominicdj.midiswap.midimessage.MidiChannelController;
import com.jimdo.dominicdj.midiswap.midimessage.MidiChannelMessage;
import com.jimdo.dominicdj.midiswap.midimessage.MidiConstants;
import com.jimdo.dominicdj.midiswap.midimessage.MidiControllerBuilder;

import java.util.List;

import static com.jimdo.dominicdj.midiswap.midimessage.MidiConstants.MidiChannel;

public class Operations extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = Operations.class.getSimpleName();

    private EditText ifRecvMsgEditText;
    private EditText thenSendMsgEditText;
    private EditText customMsgEditText;

    private Spinner receiveMsgSpinner;
    private Spinner sendMsgSpinner;

    private OperationRule myOperationRule; // TODO: later we should ask for the current OperationRule in a RecyclerView
    private OperationRule lastOperationRuleEditText;
    private OperationRule lastOperationRuleSpinner;

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

        ifRecvMsgEditText = findViewById(R.id.edit_text_input_msg);
        thenSendMsgEditText = findViewById(R.id.edit_text_output_msg);
        customMsgEditText = findViewById(R.id.edit_text_custom_msg);
        restrictText();
        usbReceiver.register(this, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));

        // ==========================================================================
        // Spinners
        // ==========================================================================
        myOperationRule = new OperationRule();
        OperationRulesManager.addOperationRule(myOperationRule);

        receiveMsgSpinner = findViewById(R.id.spinner_receive_controller);
        sendMsgSpinner = findViewById(R.id.spinner_send_controller);
        // load data for spinner
        initSpinner(receiveMsgSpinner, MidiControllerBuilder.getAvailableMidiControllers(true));
        receiveMsgSpinner.setSelection(0); // TODO: do another way, not by guessing
        initSpinner(sendMsgSpinner, MidiControllerBuilder.getAvailableMidiControllers(false));
        sendMsgSpinner.setSelection(2);
    }

    private void initSpinner(Spinner spinner, List<MidiChannelController> midiChannelControllersList) {
        ArrayAdapter<MidiChannelController> adapter = new CustomMidiControllerAdapter(this,
                R.layout.midi_controller_spinner_item, R.id.tv_midi_controller_name,
                android.R.layout.simple_spinner_dropdown_item, android.R.id.text1 /*text1 is the TextView
                in the simple_spinner_dropdown_item, provided by the Android framework*/, midiChannelControllersList);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinner_receive_controller || parent.getId() == R.id.spinner_send_controller) {
            MidiChannelController midiChannelController = (MidiChannelController) parent.getItemAtPosition(position);

            MidiChannelMessage midiChannelMessage = null;
            switch (parent.getId()) {
                case R.id.spinner_receive_controller:
                    midiChannelMessage = midiChannelController.getStandardMidiMessage();
                    switchToNewMidiChannelMessage(midiChannelMessage, true);
                    break;
                case R.id.spinner_send_controller:
                    midiChannelMessage = midiChannelController.getStandardMidiMessage();
                    switchToNewMidiChannelMessage(midiChannelMessage, false);
                    break;
            }
        }
    }

    private void switchToNewMidiChannelMessage(MidiChannelMessage midiChannelMessage, boolean isRecvMessage) {
        myOperationRule.removeAllMidiChannelMessages(isRecvMessage);
        // Construct a new MidiChannelMessage for the OperationRule
        myOperationRule.addMidiChannelMessage(midiChannelMessage, isRecvMessage);
        Toast.makeText(getApplicationContext(), myOperationRule.toString(), Toast.LENGTH_SHORT).show();
    }

    public void onClickSettings(View view) {
        Object obj = view.getTag(R.id.TAG_MIDI_CONTROLLER);
        if (!(obj instanceof MidiChannelController)) { // null check included
            Log.d(TAG, "Couldn't handle onClickSettings correctly.");
            return;
        }

        // TODO: later ask the RecyclerView for the current OperationRule where the user pressed the 'settings'-icon;
        //  decide whether user clicked on recv-side or send-side of OperationRule (with the help of RecyclerView)
        // INITIALIZE NEW OPERATION_RULES
        // what to do when the user inits a new OperationRule?
        // at the moment: just set our only reference to a new standard OperationRule
        // later we will introduce a RecyclerView to add multiple OperationRules

        final MidiChannelController midiChannelController = (MidiChannelController) obj;
        final List<MidiChannel> selectedMidiChannels = midiChannelController.getSelectedMidiChannels();
        final List<MidiChannel> allAvailableMidiChannels = midiChannelController.getAllAvailableMidiChannels();

        // ==========================================================================
        // Construct the dialog
        // ==========================================================================
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title.
        builder.setTitle(R.string.dialog_title_pick_midi_channels);

        // Set content area.
        final int size = allAvailableMidiChannels.size();
        final CharSequence[] items = new CharSequence[size];
        final boolean[] checkedItems = new boolean[size];

        for (int i = 0; i < size; i++) {
            MidiChannel midiChannel = allAvailableMidiChannels.get(i);
            items[i] = midiChannel.getReadableName();
            // Check an item in the list if it is selected according to the MidiChannelController.
            if (selectedMidiChannels.contains(midiChannel)) {
                checkedItems[i] = true;
            }
        }

        // -- Fill the dialog with checked options of the MidiController and then
        // -- track which items are checked or unchecked during runtime (by the user).
        builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    // item is checked now (and wasn't checked before)
                    checkedItems[which] = true; // user selected item
                } else {
                    // item is not checked now (but was checked before)
                    checkedItems[which] = false; // user deselected item
                }
            }
        });

        // Set action buttons.
        builder
                .setPositiveButton(R.string.ok_action_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateCheckedItemsAndOperationRule(midiChannelController, allAvailableMidiChannels,
                                selectedMidiChannels, checkedItems);
                    }
                })
                .setNegativeButton(R.string.cancel_action_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing when the user cancelled the dialog
                    }
                })
                .setNeutralButton(R.string.neutral_action_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // see https://stackoverflow.com/a/15619098
                        // Do nothing here because we override the OnClickListener for this button later to change the
                        // close behaviour. However, we still need to pass an OnClickListener here because on older
                        // versions of Android the button doesn't get instantiated unless we pass a handler for it.
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        // Overriding the handler immediately after show is probably a better approach than OnShowListener
        // see https://stackoverflow.com/a/15619098 for an explanation (we want to prevent any delay)
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCheckedItemsAndOperationRule(midiChannelController, allAvailableMidiChannels,
                        selectedMidiChannels, checkedItems);
            }
        });
    }

    private void updateCheckedItemsAndOperationRule(MidiChannelController midiChannelController,
                                                    List<MidiChannel> allAvailableMidiChannels,
                                                    List<MidiChannel> selectedMidiChannels,
                                                    boolean[] checkedItems) {
        // Go through every item in the list and check if is newly checked or unchecked.
        for (int i = 0; i < allAvailableMidiChannels.size(); i++) {
            // We don't need to check for IndexOutOfBoundException because
            // the user can only select as many items as displayed and we filled our
            // dialog list with the alternativeMidiChannels-list.
            // However, for safety reasons, we do this nevertheless.
            MidiChannel midiChannelToDiscuss = allAvailableMidiChannels.get(i);
            boolean wasChecked = selectedMidiChannels.contains(midiChannelToDiscuss);
            boolean isChecked = checkedItems[i];

            // Adjust the OperationRule according to the newly selected/unselected MidiMessages. Ignore ones that
            // have been or haven't been selected beforehand (before the user opened the dialog) in order to reduce
            // overhead, i. e. we don't do anything, if the state of an item hasn't changed.
            if (isChecked) {
                if (!wasChecked) {
                    // item is checked now and wasn't checked before
                    selectedMidiChannels.add(midiChannelToDiscuss);
                    midiChannelController.selectMidiChannel(midiChannelToDiscuss);
                    boolean isRecvController = midiChannelController.isRecvController();

                    try {
                        // Construct a new MidiChannelMessage for the OperationRule.
                        MidiChannelMessage newMidiChannelMessage =
                                midiChannelController.getStandardMidiMessage().copyWithNewMidiChannel(midiChannelToDiscuss);
                        myOperationRule.addMidiChannelMessage(newMidiChannelMessage, isRecvController);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    } catch (MidiConstants.InvalidChannelNumberException e) {
                        e.printStackTrace();
                    }
                }
            } else if (wasChecked) {
                // item is not checked now, but was checked before
                selectedMidiChannels.remove(midiChannelToDiscuss);
                midiChannelController.unselectMidiChannel(midiChannelToDiscuss);
                boolean isRecvController = midiChannelController.isRecvController();
                try {
                    // Construct the MidiChannelMessage for that the OperationRule knows which Message to delete.
                    MidiChannelMessage midiChannelMessageToDelete =
                            midiChannelController.getStandardMidiMessage().copyWithNewMidiChannel(midiChannelToDiscuss);
                    myOperationRule.removeMidiChannelMessage(midiChannelMessageToDelete, isRecvController);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                } catch (MidiConstants.InvalidChannelNumberException e) {
                    e.printStackTrace();
                }
            }
        }
        Toast.makeText(getApplicationContext(), myOperationRule.toString(), Toast.LENGTH_SHORT).show();
        // This method is (among others) called, when the user clicks on the neutral button.
        // In that case we don't want the dialog to go away, so that's why we don't call dialog.dismiss() here.
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // ignore
    }

    public void updateMsgFromView(View v) {
        // at the moment: do nothing

        /*// Check user input
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
                boolean addOperationWorked = OperationRulesManager.addOperationRule(newOperationRule);
                if (addOperationWorked) {
                    Toast.makeText(this, "Rule updated via EditText", Toast.LENGTH_SHORT).show();
                    // Delete old OperationRule.
                    if (lastOperationRuleEditText != null) {
                        OperationRulesManager.deleteOperationRule(lastOperationRuleEditText);
                    }
                    lastOperationRuleEditText = newOperationRule;
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }*/
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