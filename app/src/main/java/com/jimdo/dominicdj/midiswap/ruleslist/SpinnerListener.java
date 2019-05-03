package com.jimdo.dominicdj.midiswap.ruleslist;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import com.jimdo.dominicdj.midiswap.R;
import com.jimdo.dominicdj.midiswap.midimessage.MidiChannelController;
import com.jimdo.dominicdj.midiswap.midimessage.MidiChannelMessage;
import com.jimdo.dominicdj.midiswap.midimessage.MidiConstants;
import com.jimdo.dominicdj.midiswap.operationrules.OperationRule;

import java.util.List;

import static com.jimdo.dominicdj.midiswap.midimessage.MidiConstants.MidiChannel;

public class SpinnerListener implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private Context context;
    // We have to make sure that onItemSelected is always called before onClick (users clicks on 'settings'-icon).
    // Otherwise this reference to the spinner will be outdated!
    private Spinner spinner;

    private static final String TAG = SpinnerListener.class.getSimpleName();

    public SpinnerListener(Context context) {
        this.context = context;
    }

    // ==========================================================================
    // OnItemSelectedListener for spinner items
    // ==========================================================================
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // the user has selected an item in the spinner
        Spinner spinner = (Spinner) parent;
        this.spinner = spinner;
        Object tag = spinner.getTag(R.id.TAG_SPINNER_OPERATION_RULE);
        if (!(tag instanceof OperationRule)) {
            return;
        }
        OperationRule currentOperationRule = (OperationRule) tag;

        MidiChannelController midiChannelController = (MidiChannelController) parent.getItemAtPosition(position);
        MidiChannelMessage midiChannelMessage = null;
        if (midiChannelController.isRecvController()) {
            midiChannelMessage = midiChannelController.getStandardMidiMessage();
            switchOperationRuleToNewMidiMessage(currentOperationRule, midiChannelMessage, true);
        } else {
            midiChannelMessage = midiChannelController.getStandardMidiMessage();
            switchOperationRuleToNewMidiMessage(currentOperationRule, midiChannelMessage, false);
        }
    }

    private void switchOperationRuleToNewMidiMessage(OperationRule operationRule, MidiChannelMessage midiChannelMessage, boolean isRecvMessage) {
        operationRule.removeAllMidiChannelMessages(isRecvMessage);
        // Construct a new MidiChannelMessage for the OperationRule
        operationRule.addMidiChannelMessage(midiChannelMessage, isRecvMessage);
        Log.d(TAG, operationRule.toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // ignore
    }


    // ==========================================================================
    // OnClickListener for 'settings'-button in spinner item
    // ==========================================================================
    @Override
    public void onClick(View v) {
        // ==========================================================================
        // Get object references.
        // ==========================================================================
        // Get ImageButton.
        if (!(v instanceof ImageButton)) {
            Log.d(TAG, "The view which called this method is no ImageButton.");
            return;
        }
        final ImageButton settingsButton = (ImageButton) v;

        // Get OperationRule from Spinner.
        Object operationRuleCheck = spinner.getTag(R.id.TAG_SPINNER_OPERATION_RULE);
        if (!(operationRuleCheck instanceof OperationRule)) {
            Log.d(TAG, "The spinner does not include a OperationRule.");
            return;
        }
        final OperationRule operationRule = (OperationRule) operationRuleCheck;

        // Get MidiController.
        Object midiChannelControllerCheck = settingsButton.getTag(R.id.TAG_MIDI_CHANNEL_CONTROLLER);
        if (!(midiChannelControllerCheck instanceof MidiChannelController)) {
            Log.d(TAG, "The settingsButton has no MidiChannelController tagged to it.");
            return;
        }
        final MidiChannelController midiChannelController = (MidiChannelController) midiChannelControllerCheck;
        final List<MidiChannel> selectedMidiChannels = midiChannelController.getSelectedMidiChannels();
        final List<MidiChannel> allAvailableMidiChannels = midiChannelController.getAllAvailableMidiChannels();


        // ==========================================================================
        // Construct the dialog
        // ==========================================================================
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

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
                        updateCheckedItemsAndOperationRule(operationRule, midiChannelController, allAvailableMidiChannels,
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
                updateCheckedItemsAndOperationRule(operationRule, midiChannelController, allAvailableMidiChannels,
                        selectedMidiChannels, checkedItems);
            }
        });
    }

    private void updateCheckedItemsAndOperationRule(OperationRule operationRule,
                                                    MidiChannelController midiChannelController,
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
                        operationRule.addMidiChannelMessage(newMidiChannelMessage, isRecvController);
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
                    operationRule.removeMidiChannelMessage(midiChannelMessageToDelete, isRecvController);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                } catch (MidiConstants.InvalidChannelNumberException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(TAG, operationRule.toString());

        // This method is (among others) called, when the user clicks on the neutral button.
        // In that case we don't want the dialog to go away, so that's why we don't call dialog.dismiss() here.
    }

}
