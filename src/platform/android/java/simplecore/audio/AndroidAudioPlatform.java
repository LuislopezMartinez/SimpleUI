package simplecore.audio;

import android.app.Activity;
import android.content.res.AssetManager;
import java.util.ArrayList;
import processing.core.PApplet;
import simplecore.internal.AudioPlatform;
import simplecore.internal.FailedSoundBackend;
import simplecore.internal.SoundBackend;

/** MediaPlayer implementation, loaded internally by Core in Android Mode. */
public final class AndroidAudioPlatform implements AudioPlatform {
    private final AssetManager assets;

    public AndroidAudioPlatform(PApplet parent) {
        Activity activity = parent.getActivity();
        if (activity == null) throw new IllegalStateException("Android Activity is unavailable");
        assets = activity.getAssets();
    }

    public SoundBackend load(String filename) {
        try {
            return new AndroidSoundBackend(assets, filename);
        } catch (Exception failure) {
            return new FailedSoundBackend("Cannot load " + filename + ": " + failure.getMessage());
        }
    }

    public String[] list(String folderName) {
        try {
            String[] names = assets.list(folderName);
            if (names == null) return new String[0];
            ArrayList<String> result = new ArrayList<String>();
            for (String name : names) result.add(folderName + "/" + name);
            return result.toArray(new String[result.size()]);
        } catch (Exception failure) {
            return new String[0];
        }
    }
}
