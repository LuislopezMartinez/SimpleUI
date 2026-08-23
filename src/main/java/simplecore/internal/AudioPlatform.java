package simplecore.internal;

/** Internal sound factory selected by Core for the current Processing mode. */
public interface AudioPlatform {
    SoundBackend load(String filename);
    String[] list(String folderName);
}
