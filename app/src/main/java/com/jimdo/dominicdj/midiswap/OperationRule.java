package com.jimdo.dominicdj.midiswap;

import android.support.annotation.NonNull;
import android.util.Log;
import com.jimdo.dominicdj.midiswap.midimessage.MidiChannelMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OperationRule {

    private List<MidiChannelMessage> midiRecvMessages = new ArrayList<>(3);
    private List<MidiChannelMessage> midiSendMessages = new ArrayList<>(3);

    public OperationRule() {
    }

    // not needed at the moment
    public OperationRule(@NonNull List<MidiChannelMessage> midiRecvMessages, @NonNull List<MidiChannelMessage> midiSendMessages) {
        addMessages(midiRecvMessages, this.midiRecvMessages);
        addMessages(midiSendMessages, this.midiSendMessages);
    }

    // ==========================================================================
    // Getters
    // ==========================================================================

    @NotNull
    public List<MidiChannelMessage> getMidiRecvMessages() {
        return midiRecvMessages;
    }

    @NotNull
    public List<MidiChannelMessage> getMidiSendMessages() {
        return midiSendMessages;
    }


    // ==========================================================================
    // Add new MidiMessages
    // ==========================================================================

    private void addMessages(@NonNull List<MidiChannelMessage> newMidiMessages, List<MidiChannelMessage> listToAdd) {
        if (newMidiMessages.size() == 0) {
            throw new IllegalArgumentException("You have to add at least one MidiMessage to this OperationRule.");
        }
        for (MidiChannelMessage newMidiMessage : newMidiMessages) {
            // Check if the 'to-add-list' doesn't already contain a 'new' MidiMessage.
            if (!listToAdd.contains(newMidiMessage)) {
                listToAdd.add(newMidiMessage);
            }
        }
    }

    public void addMidiChannelMessages(@NonNull List<MidiChannelMessage> newMidiChannelMessages, boolean isRecvMessage) {
        if (isRecvMessage) {
            addMessages(newMidiChannelMessages, this.midiRecvMessages);
        } else {
            addMessages(newMidiChannelMessages, this.midiSendMessages);
        }
    }

    public void addMidiChannelMessage(@NonNull MidiChannelMessage newMidiChannelMessage, boolean isRecvMessage) {
        List<MidiChannelMessage> newMidiChannelMessages = Arrays.asList(newMidiChannelMessage);
        addMidiChannelMessages(newMidiChannelMessages, isRecvMessage);
    }


    // ==========================================================================
    // Remove old MidiMessages
    // ==========================================================================

    private void removeMessages(@NonNull List<MidiChannelMessage> midiMessagesToDelete, List<MidiChannelMessage> listToDeleteFrom) {
        if (midiMessagesToDelete.size() == 0) {
            throw new IllegalArgumentException("You have to remove at least one MidiMessage from this OperationRule.");
        }
        for (MidiChannelMessage midiMessageToDelete : midiMessagesToDelete) {
            // The remove method already checks if the 'remove-from-list' contains the MidiMessage that should be deleted.
            boolean worked = listToDeleteFrom.remove(midiMessageToDelete);
            Log.d("OperationRule", "Remove worked? : " + worked);
        }
    }

    public void removeMidiChannelMessages(@NonNull List<MidiChannelMessage> midiChannelMessagesToDelete, boolean isRecvMessage) {
        if (isRecvMessage) {
            removeMessages(midiChannelMessagesToDelete, this.midiRecvMessages);
        } else {
            removeMessages(midiChannelMessagesToDelete, this.midiSendMessages);
        }
    }

    public void removeMidiChannelMessage(@NonNull MidiChannelMessage midiChannelMessageToDelete, boolean isRecvMessage) {
        List<MidiChannelMessage> midiChannelMessagesToDelete = Arrays.asList(midiChannelMessageToDelete);
        removeMidiChannelMessages(midiChannelMessagesToDelete, isRecvMessage);
    }

    public void removeAllMidiChannelMessages(boolean isRecvMessage) {
        if (isRecvMessage) {
            midiRecvMessages = new ArrayList<>(3);
        } else {
            midiSendMessages = new ArrayList<>(3);
        }
    }


    // ==========================================================================
    // Divers
    // ==========================================================================

    @Override
    public String toString() {
        // TODO: Adjust string representation
        return "OperationRule[" + midiRecvMessages + ", " + midiSendMessages + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OperationRule) {
            OperationRule rule = (OperationRule) obj;
            if (rule.midiRecvMessages.equals(this.midiRecvMessages)
                    && rule.midiSendMessages.equals(this.midiSendMessages)) {
                return true;
            }
        }
        return false;
    }

}
