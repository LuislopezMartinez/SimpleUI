import processing.core.PApplet;
import processing.opengl.PGraphics2D;
import simpleui.android.SimpleUI;

public class AndroidRendererSmoke {
    public static void main(String[] args) {
        PApplet host = new PApplet();
        SimpleUI.setAutomaticEventHandling(false);
        SimpleUI.attach(host);
        if (SimpleUI.host() != host) throw new AssertionError("Traditional Android host was not attached");
        SimpleUI.detach();

        host.g = new PGraphics2D();
        SimpleUI.attach(host);
        if (SimpleUI.host() != host) throw new AssertionError("P2D host was not attached");
        SimpleUI.detach();
        System.out.println("Android traditional/P2D renderer validation passed.");
    }
}
