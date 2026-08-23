package simplecore.audio;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import simplecore.internal.SoundBackend;

final class AndroidSoundBackend implements SoundBackend {
    private MediaPlayer player;

    AndroidSoundBackend(AssetManager assets, String filename) throws Exception {
        MediaPlayer openedPlayer = new MediaPlayer();
        try (AssetFileDescriptor descriptor = assets.openFd(filename)) {
            openedPlayer.setDataSource(
                descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            openedPlayer.prepare();
        } catch (Exception failure) {
            openedPlayer.release();
            throw failure;
        }
        player = openedPlayer;
    }

    public boolean isLoaded() { return player != null; }
    public String getError() { return null; }

    public synchronized void playFromStart() {
        if (player == null) return;
        player.seekTo(0);
        player.start();
    }

    public synchronized void pause() { if (player != null && player.isPlaying()) player.pause(); }
    public synchronized void resume() { if (player != null) player.start(); }

    public synchronized void stop() {
        if (player == null) return;
        if (player.isPlaying()) player.pause();
        player.seekTo(0);
    }

    public synchronized void rewind() { if (player != null) player.seekTo(0); }
    public boolean isPlaying() { return player != null && player.isPlaying(); }
    public int getPosition() { return player == null ? 0 : player.getCurrentPosition(); }
    public int getDuration() { return player == null ? 0 : player.getDuration(); }
    public synchronized void setPosition(int milliseconds) { if (player != null) player.seekTo(milliseconds); }
    public synchronized void setLoop(boolean looping) { if (player != null) player.setLooping(looping); }
    public synchronized void setVolume(float volume) { if (player != null) player.setVolume(volume, volume); }

    public synchronized void dispose() {
        if (player == null) return;
        player.release();
        player = null;
    }
}
