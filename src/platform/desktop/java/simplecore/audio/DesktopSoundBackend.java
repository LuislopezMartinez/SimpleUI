package simplecore.audio;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import processing.core.PApplet;
import simplecore.internal.SoundBackend;

final class DesktopSoundBackend implements SoundBackend {
    private final Clip clip;
    private String error;
    private boolean looping;

    DesktopSoundBackend(PApplet parent, String filename) throws Exception {
        InputStream source = parent.createInput(filename);
        if (source == null) throw new IllegalArgumentException("File not found");
        Clip openedClip = null;
        try (BufferedInputStream buffered = new BufferedInputStream(source);
             AudioInputStream encoded = AudioSystem.getAudioInputStream(buffered)) {
            AudioFormat input = encoded.getFormat();
            AudioFormat pcm = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                input.getSampleRate(),
                16,
                input.getChannels(),
                input.getChannels() * 2,
                input.getSampleRate(),
                false
            );
            try (AudioInputStream decoded = AudioSystem.getAudioInputStream(pcm, encoded)) {
                openedClip = AudioSystem.getClip();
                openedClip.open(decoded);
            }
        } catch (Exception failure) {
            if (openedClip != null) openedClip.close();
            throw failure;
        }
        clip = openedClip;
    }

    public boolean isLoaded() { return clip != null && clip.isOpen(); }
    public String getError() { return error; }

    public synchronized void playFromStart() {
        if (!isLoaded()) return;
        clip.stop();
        clip.setMicrosecondPosition(0);
        if (looping) clip.loop(Clip.LOOP_CONTINUOUSLY);
        else clip.start();
    }

    public synchronized void pause() { if (isLoaded()) clip.stop(); }
    public synchronized void resume() { if (isLoaded()) clip.start(); }

    public synchronized void stop() {
        if (!isLoaded()) return;
        clip.stop();
        clip.setMicrosecondPosition(0);
    }

    public synchronized void rewind() { if (isLoaded()) clip.setMicrosecondPosition(0); }
    public boolean isPlaying() { return isLoaded() && clip.isRunning(); }
    public int getPosition() { return isLoaded() ? (int)(clip.getMicrosecondPosition() / 1000L) : 0; }
    public int getDuration() { return isLoaded() ? (int)(clip.getMicrosecondLength() / 1000L) : 0; }

    public synchronized void setPosition(int milliseconds) {
        if (!isLoaded()) return;
        long requested = (long)milliseconds * 1000L;
        clip.setMicrosecondPosition(Math.min(requested, clip.getMicrosecondLength()));
    }

    public synchronized void setLoop(boolean looping) {
        this.looping = looping;
        if (isLoaded() && clip.isRunning()) clip.loop(looping ? Clip.LOOP_CONTINUOUSLY : 0);
    }

    public synchronized void setVolume(float volume) {
        if (!isLoaded() || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl gain = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
        float value = volume <= 0.0f ? gain.getMinimum() : (float)(20.0 * Math.log10(volume));
        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), value)));
    }

    public synchronized void dispose() {
        if (clip == null) return;
        clip.stop();
        clip.close();
    }
}
