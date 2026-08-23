package simplecore.internal;

/** Inert backend that keeps loading failures safe and inspectable. */
public final class FailedSoundBackend implements SoundBackend {
    private final String error;

    public FailedSoundBackend(String error) {
        this.error = error == null || error.length() == 0 ? "Unknown audio error" : error;
    }

    public boolean isLoaded() { return false; }
    public String getError() { return error; }
    public void playFromStart() {}
    public void pause() {}
    public void resume() {}
    public void stop() {}
    public void rewind() {}
    public boolean isPlaying() { return false; }
    public int getPosition() { return 0; }
    public int getDuration() { return 0; }
    public void setPosition(int milliseconds) {}
    public void setLoop(boolean looping) {}
    public void setVolume(float volume) {}
    public void dispose() {}
}
