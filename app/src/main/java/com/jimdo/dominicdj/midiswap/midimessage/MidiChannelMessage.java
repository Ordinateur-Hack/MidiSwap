package com.jimdo.dominicdj.midiswap.midimessage;

import android.support.annotation.NonNull;

import static com.jimdo.dominicdj.midiswap.midimessage.MidiConstants.*;

public class MidiChannelMessage extends MidiMessage implements Cloneable {

    private MidiChannelEvent midiChannelEvent;
    private MidiChannel midiChannel;
    private String firstDataByte;
    private String secondDataByte;
    private boolean isRecvMessage;

    /**
     * @param midiChannelEvent
     * @param midiChannel
     * @param firstDataByte
     * @param secondDataByte
     * @param isRecvMessage
     * @throws InvalidChannelNumberException
     */
    public MidiChannelMessage(MidiChannelEvent midiChannelEvent, MidiChannel midiChannel,
                              String firstDataByte, String secondDataByte, boolean isRecvMessage) throws InvalidChannelNumberException {

        this.midiChannelEvent = midiChannelEvent;
        this.midiChannel = midiChannel;
        this.firstDataByte = firstDataByte;
        this.secondDataByte = secondDataByte;
        this.isRecvMessage = isRecvMessage;

        updateHexMessage();
    }

    private void updateHexMessage() throws InvalidChannelNumberException {
        // Construct MidiChannelMessage by concatenating its parts.
        String midiChannelByteRepresentation = midiChannel.getByteRepresentation(isRecvMessage);
        String midiChannelEventByteRepresentation = midiChannelEvent.getByteRepresentation();

        if (isRecvMessage) {
            /*// The Tyros attaches a swapped value-pair of MidiChannel and MidiChannelEvent to the beginning
            // of the messages it sends to a device, like our android phone
            // Example: if the message starts with "90...", the Tyros adds "09"
            // --> the whole message would then be "0990..."
            hexMessage = midiChannelByteRepresentation + midiChannelEventByteRepresentation;*/
            // I have thought that this was true, however I found out that at least the ControlChange MidiMessages
            // are just preceded by '0B'
            // TODO: make more researches about how the supplements (appendix) work on the Tyros
            hexMessage = "0B";
        } else {
            // the Tyros3 needs this value at the beginning of each channel message to recognize sent data
            // TODO: "Learn if this is only true for channel messages or for every message. If true for every message,
            //  then we can put this code into the constructor of MidiMessage
            hexMessage = "1B";
        }

        hexMessage += midiChannelEventByteRepresentation + midiChannelByteRepresentation
                + firstDataByte + secondDataByte;
    }

    public MidiChannelMessage(MidiChannelEvent midiChannelEvent, MidiChannel midiChannel,
                              ControlChange controlChange, boolean isRecvChannel) throws InvalidChannelNumberException {
        this(midiChannelEvent, midiChannel, controlChange.getByteRepresentation(), "XX", isRecvChannel);
    }


    public MidiChannel getMidiChannel() {
        return midiChannel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MidiChannelMessage) {
            MidiChannelMessage msg = (MidiChannelMessage) obj;
            return msg.midiChannelEvent == this.midiChannelEvent
                    && msg.midiChannel == this.midiChannel
                    && msg.firstDataByte.equals(this.firstDataByte)
                    && msg.secondDataByte.equals(this.secondDataByte)
                    && msg.isRecvMessage == this.isRecvMessage;
        }
        return false;
    }

    public MidiChannelMessage copyWithNewMidiChannel(MidiChannel newMidiChannel)
            throws CloneNotSupportedException, InvalidChannelNumberException {
        MidiChannelMessage newMidiChannelMessage = (MidiChannelMessage) this.clone();
        newMidiChannelMessage.midiChannel = newMidiChannel;
        newMidiChannelMessage.updateHexMessage();
        return newMidiChannelMessage;
    }

    @NonNull
    @Override
    public String toString() {
        return hexMessage;
    }

}
