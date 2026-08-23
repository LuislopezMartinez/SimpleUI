package simplecore.internal;

/** Internal platform bridge used by simplecore.Sound. */
public interface SoundBackend {
    boolean isLoaded();
    String getError();
    void playFromStart();
    void pause();
    void resume();
    void stop();
    void rewind();
    boolean isPlaying();
    int getPosition();
    int getDuration();
    void setPosition(int milliseconds);
    void setLoop(boolean looping);
    void setVolume(float volume);
    void dispose();
}
