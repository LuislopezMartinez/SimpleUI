package simplecore;

import processing.core.PApplet;
import simplecore.internal.SoundBackend;

/** Cross-platform MP3, Ogg Vorbis and WAV playback controlled by SimpleCore. */
public final class Sound {
    private final Core owner;
    private final String filename;
    private final SoundBackend backend;
    private float volume = 1.0f;
    private boolean looping;
    private boolean paused;
    private boolean disposed;

    Sound(Core owner, String filename, SoundBackend backend) {
        this.owner = owner;
        this.filename = filename;
        this.backend = backend;
    }

    public String getFilename() { return filename; }
    public boolean isLoaded() { return !disposed && backend.isLoaded(); }
    public String getError() { return backend.getError(); }
    public boolean isDisposed() { return disposed; }

    public synchronized Sound play() {
        if (!isLoaded()) return this;
        backend.playFromStart();
        paused = false;
        return this;
    }

    public synchronized Sound play(boolean repeat) {
        setLoop(repeat);
        return play();
    }

    public synchronized Sound play(float newVolume) {
        setVolume(newVolume);
        return play();
    }

    public synchronized Sound pause() {
        if (isLoaded() && backend.isPlaying()) {
            backend.pause();
            paused = true;
        }
        return this;
    }

    public synchronized Sound resume() {
        if (isLoaded() && paused) {
            backend.resume();
            paused = false;
        }
        return this;
    }

    public synchronized Sound stop() {
        if (isLoaded()) backend.stop();
        paused = false;
        return this;
    }

    public synchronized Sound rewind() {
        if (isLoaded()) backend.rewind();
        return this;
    }

    public synchronized Sound setLoop(boolean repeat) {
        looping = repeat;
        if (isLoaded()) backend.setLoop(repeat);
        return this;
    }

    public synchronized Sound setVolume(float newVolume) {
        if (!Float.isFinite(newVolume)) return this;
        volume = PApplet.constrain(newVolume, 0.0f, 1.0f);
        if (isLoaded()) backend.setVolume(volume);
        return this;
    }

    public synchronized Sound setPosition(int milliseconds) {
        if (isLoaded()) backend.setPosition(Math.max(0, milliseconds));
        return this;
    }

    public boolean isPlaying() { return isLoaded() && backend.isPlaying(); }
    public boolean isPaused() { return isLoaded() && paused; }
    public boolean isLooping() { return looping; }
    public float getVolume() { return volume; }
    public int getPosition() { return isLoaded() ? backend.getPosition() : 0; }
    public int getDuration() { return isLoaded() ? backend.getDuration() : 0; }

    public synchronized void dispose() {
        if (disposed) return;
        disposed = true;
        paused = false;
        backend.dispose();
        owner.unregisterSound(this);
    }

    void disposeFromCore() {
        synchronized (this) {
            if (disposed) return;
            disposed = true;
            paused = false;
            backend.dispose();
        }
    }
}
