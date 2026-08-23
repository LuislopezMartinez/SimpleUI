import java.util.HashSet;
import java.util.ServiceLoader;
import javax.sound.sampled.spi.AudioFileReader;
import processing.awt.PGraphicsJava2D;
import processing.core.PApplet;
import simplecore.Core;
import simplecore.Sound;

public final class DesktopAudioSmoke {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        PApplet host = new PApplet();
        host.g = new PGraphicsJava2D();
        Core core = Core.start(host);
        Sound missing = core.loadSound("audio/does-not-exist.mp3");

        HashSet<String> readers = new HashSet<String>();
        for (AudioFileReader reader : ServiceLoader.load(AudioFileReader.class)) {
            readers.add(reader.getClass().getName());
        }
        check(readers.contains("javazoom.spi.mpeg.sampled.file.MpegAudioFileReader"),
            "Bundled MP3SPI provider is unavailable");
        check(readers.contains("javazoom.spi.vorbis.sampled.file.VorbisAudioFileReader"),
            "Bundled VorbisSPI provider is unavailable");

        check(!missing.isLoaded(), "A missing sound must not report loaded");
        check(missing.getError() != null && missing.getError().length() > 0,
            "A failed sound must expose its error");
        missing.setVolume(2.0f).setLoop(true).play().pause().resume().stop().rewind();
        check(missing.getVolume() == 1.0f && missing.isLooping(),
            "Failed sounds must retain safe public state");
        check(core.loadSounds("audio/missing-folder").length == 0,
            "A missing sound folder must produce an empty array");
        Core.shutdown();
        check(missing.isDisposed(), "Core.shutdown must dispose loaded sounds");

        System.out.println("Desktop MP3/Ogg providers and Sound lifecycle passed.");
    }
}
