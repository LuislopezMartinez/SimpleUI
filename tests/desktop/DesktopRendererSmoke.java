import processing.awt.PGraphicsJava2D;
import processing.core.PApplet;
import processing.opengl.PGraphics2D;
import simpleui.desktop.SimpleUI;

public class DesktopRendererSmoke {
    public static void main(String[] args) {
        PApplet host = new PApplet();
        host.g = new PGraphicsJava2D();
        SimpleUI.setAutomaticEventHandling(false);
        SimpleUI.attach(host);
        if (SimpleUI.host() != host) throw new AssertionError("JAVA2D host was not attached");
        SimpleUI.detach();

        host.g = new PGraphics2D();
        SimpleUI.attach(host);
        if (SimpleUI.host() != host) throw new AssertionError("P2D host was not attached");
        SimpleUI.detach();
        System.out.println("Desktop JAVA2D/P2D renderer validation passed.");
    }
}
