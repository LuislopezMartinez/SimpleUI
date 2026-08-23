package simplecore.audio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import processing.core.PApplet;
import simplecore.internal.AudioPlatform;
import simplecore.internal.FailedSoundBackend;
import simplecore.internal.SoundBackend;

/** Java Sound implementation, loaded internally by Core on Desktop. */
public final class DesktopAudioPlatform implements AudioPlatform {
    private static final String[] DEPENDENCIES = {
        "mp3spi-1.9.5.4.jar",
        "vorbisspi-1.0.3.3.jar",
        "jlayer-1.0.1.4.jar",
        "jorbis-0.0.17.4.jar",
        "tritonus-share-0.3.7.4.jar"
    };
    private static ClassLoader decoderLoader;
    private final PApplet parent;

    public DesktopAudioPlatform(PApplet parent) throws Exception {
        this.parent = parent;
        installDecoderLoader();
    }

    public SoundBackend load(String filename) {
        try {
            Thread.currentThread().setContextClassLoader(decoderLoader);
            return new DesktopSoundBackend(parent, filename);
        } catch (Exception failure) {
            return new FailedSoundBackend("Cannot load " + filename + ": " + failure.getMessage());
        }
    }

    private static synchronized void installDecoderLoader() throws Exception {
        if (decoderLoader != null) {
            Thread.currentThread().setContextClassLoader(decoderLoader);
            return;
        }
        File folder = new File(System.getProperty("java.io.tmpdir"), "simpleui-audio-0.7.0");
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IllegalStateException("Cannot create Desktop audio cache");
        }
        URL[] urls = new URL[DEPENDENCIES.length];
        byte[] buffer = new byte[16384];
        for (int index = 0; index < DEPENDENCIES.length; index++) {
            String name = DEPENDENCIES[index];
            File target = new File(folder, name);
            if (!target.isFile() || target.length() == 0) {
                try (InputStream source = DesktopAudioPlatform.class.getResourceAsStream("/simplecore/audio/deps/" + name)) {
                    if (source == null) throw new IllegalStateException("Missing embedded decoder " + name);
                    try (FileOutputStream output = new FileOutputStream(target)) {
                        int count;
                        while ((count = source.read(buffer)) >= 0) output.write(buffer, 0, count);
                    }
                }
            }
            target.deleteOnExit();
            urls[index] = target.toURI().toURL();
        }
        folder.deleteOnExit();
        decoderLoader = new URLClassLoader(urls, DesktopAudioPlatform.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(decoderLoader);
    }

    public String[] list(String folderName) {
        File folder = new File(parent.dataPath(folderName));
        File[] files = folder.listFiles();
        if (files == null) return new String[0];
        ArrayList<String> result = new ArrayList<String>();
        for (File file : files) {
            if (file.isFile()) result.add(folderName + "/" + file.getName());
        }
        return result.toArray(new String[result.size()]);
    }
}
