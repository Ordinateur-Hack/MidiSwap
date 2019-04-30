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
                    new MidiController("Modulation", shouldBeRecvControllers, MidiChannel.getVoiceMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.MODULATION, shouldBeRecvControllers)));

            midiControllersList.add(
                    new MidiController("Portamento Time", shouldBeRecvControllers, MidiChannel.getVoiceMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.PORTAMENTO_TIME, shouldBeRecvControllers)));

            midiControllersList.add(
                    new MidiController("Volume", shouldBeRecvControllers, MidiChannel.getVoiceMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.MAIN_VOLUME, shouldBeRecvControllers)));

            midiControllersList.add(
                    new MidiController("Panpot", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.PANPOT, shouldBeRecvControllers)));

            midiControllersList.add(
                    new MidiController("Harmonic Content", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.HARMONIC_CONTENT, shouldBeRecvControllers)));

            midiControllersList.add(
                    new MidiController("Release Time", shouldBeRecvControllers, MidiChannel.getVoiceMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.RELEASE_TIME, shouldBeRecvControllers)));

            midiControllersList.add(
                    new MidiController("Attack Time", shouldBeRecvControllers, MidiChannel.getVoiceMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.ATTACK_TIME, shouldBeRecvControllers)));

            midiControllersList.add(
                    new MidiController("Brightness", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.BRIGHTNESS, shouldBeRecvControllers)));

            midiControllersList.add(
                    new MidiController("Decay Time", shouldBeRecvControllers, MidiChannel.getVoiceMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.DECAY_TIME, shouldBeRecvControllers)));

            midiControllersList.add(
                    new MidiController("Reverb", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.EFFECT_DEPHT_REVERB, shouldBeRecvControllers)));

            midiControllersList.add(
                    new MidiController("Chorus", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.EFFECT_DEPHT_CHORUS, shouldBeRecvControllers)));

            midiControllersList.add(
                    new MidiController("Variation", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.EFFECT_DEPHT_VARIATION, shouldBeRecvControllers)));

            // Other controllers
            // TODO: create new MidiControllers to use

        } catch (InvalidChannelNumberException e) {
            e.printStackTrace();
        }
        return midiControllersList;
    }

}
