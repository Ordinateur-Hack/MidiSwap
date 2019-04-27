package com.jimdo.dominicdj.midiswap.midimessage;

import com.jimdo.dominicdj.midiswap.midimessage.MidiChannelMessage;
import com.jimdo.dominicdj.midiswap.midimessage.MidiController;

import java.util.ArrayList;
import java.util.List;

import static com.jimdo.dominicdj.midiswap.midimessage.MidiConstants.*;

public class MidiControllerBuilder {

    public static List<MidiController> getAvailableMidiControllers(boolean shouldBeRecvControllers) {
        List<MidiController> midiControllersList = new ArrayList<>();

        try {
            // Group often used alternative MidiChannels.
            // ...

            // ==========================================================================
            // Construct new MIDI-controllers.
            // ==========================================================================
            // Midi Channel Message Controllers
            midiControllersList.add(
                    new MidiController("Modulation",
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.MODULATION, shouldBeRecvControllers,
                                    MidiChannel.getVoiceMidiChannels())));

            midiControllersList.add(
                    new MidiController("Portamento Time",
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.PORTAMENTO_TIME, shouldBeRecvControllers,
                                    MidiChannel.getVoiceMidiChannels())));

            midiControllersList.add(
                    new MidiController("Volume",
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.MAIN_VOLUME, shouldBeRecvControllers,
                                    MidiChannel.getVoiceAndStyleMidiChannels())));

            midiControllersList.add(
                    new MidiController("Panpot",
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.PANPOT, shouldBeRecvControllers,
                                    MidiChannel.getVoiceAndStyleMidiChannels())));

            midiControllersList.add(
                    new MidiController("Harmonic Content",
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.HARMONIC_CONTENT, shouldBeRecvControllers,
                                    MidiChannel.getVoiceAndStyleMidiChannels())));

            midiControllersList.add(
                    new MidiController("Release Time",
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.RELEASE_TIME, shouldBeRecvControllers,
                                    MidiChannel.getVoiceMidiChannels())));

            midiControllersList.add(
                    new MidiController("Attack Time",
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.ATTACK_TIME, shouldBeRecvControllers,
                                    MidiChannel.getVoiceMidiChannels())));

            midiControllersList.add(
                    new MidiController("Brightness",
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.BRIGHTNESS, shouldBeRecvControllers,
                                    MidiChannel.getVoiceAndStyleMidiChannels())));

            midiControllersList.add(
                    new MidiController("Decay Time",
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.DECAY_TIME, shouldBeRecvControllers,
                                    MidiChannel.getVoiceMidiChannels())));

            midiControllersList.add(
                    new MidiController("Reverb",
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.EFFECT_DEPHT_REVERB, shouldBeRecvControllers,
                                    MidiChannel.getVoiceAndStyleMidiChannels())));

            midiControllersList.add(
                    new MidiController("Chorus",
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.EFFECT_DEPHT_CHORUS, shouldBeRecvControllers,
                                    MidiChannel.getVoiceAndStyleMidiChannels())));

            midiControllersList.add(
                    new MidiController("Variation",
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.EFFECT_DEPHT_VARIATION, shouldBeRecvControllers,
                                    MidiChannel.getVoiceAndStyleMidiChannels())));

            // Other controllers
            // TODO: create new MidiControllers to use

        } catch (InvalidChannelNumberException e) {
            e.printStackTrace();
        }
        return midiControllersList;
    }

}
