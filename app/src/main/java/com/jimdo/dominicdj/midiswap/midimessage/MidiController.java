package com.jimdo.dominicdj.midiswap.midimessage;

public class MidiController {

    private String name;
    private MidiMessage midiMessage;

    public MidiController(String name, MidiMessage midiMessage) {
        this.name = name;
        this.midiMessage = midiMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MidiMessage getMidiMessage() {
        return midiMessage;
    }

    // to display the object as a string in spinner
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MidiController) {
            MidiController m = (MidiController) obj;
            if (m.getName().equals(name) && m.getMidiMessage().equals(midiMessage)) {
                return true;
            }
        }
        return false;
    }
}
