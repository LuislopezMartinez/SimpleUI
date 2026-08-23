import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PFont;
import processing.awt.PGraphicsJava2D;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.opengl.PGraphics2D;
import simplecore.Core;
import simplecore.Scene;
import simplecore.Task;
import simplecore.Viewport;
import simplecore.ViewportMode;

public class SimpleCoreSmoke {
    private static final class ClockApplet extends PApplet {
        int now;
        public int millis() { return now; }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static PApplet p2dHost() {
        PApplet host = new PApplet();
        host.g = new PGraphics2D();
        return host;
    }

    private static PApplet java2dHost() {
        PApplet host = new PApplet();
        host.g = new PGraphicsJava2D();
        return host;
    }

    private static class ProbeTask extends Task {
        final String name;
        final ArrayList<String> log;
        int destroyedCount;

        ProbeTask(String name, int priority, ArrayList<String> log) {
            this.name = name;
            this.priority = priority;
            this.log = log;
        }

        protected void initialize() { log.add("init:" + name); }
        protected void frame() {
            check(getCore().getCaller() == this, "getCaller must identify the running task");
            log.add("frame:" + name);
        }
        protected void onDestroy() {
            destroyedCount++;
            log.add("destroy:" + name);
        }
    }

    private static final class SpawnerTask extends ProbeTask {
        ProbeTask child;

        SpawnerTask(ArrayList<String> log) { super("spawner", 5, log); }

        protected void frame() {
            super.frame();
            if (child == null) child = new ProbeTask("child", 20, log);
        }
    }

    public static void main(String[] args) {
        check("Luis lopez martinez".equals(Core.LIBRARY_AUTHOR),
            "Unexpected SimpleCore author metadata");
        check("MIT".equals(Core.LIBRARY_LICENSE),
            "Unexpected SimpleCore license metadata");
        boolean rejectedWithoutCore = false;
        try { new Task(); }
        catch (IllegalStateException expected) { rejectedWithoutCore = true; }
        check(rejectedWithoutCore, "Task creation without Core must fail");

        ClockApplet host = new ClockApplet();
        host.g = new PGraphics2D();
        host.now = 100;
        Core core = Core.start(host);
        check(Core.start(host) == core, "Core.start must return the existing singleton");
        check(Core.getInstance() == core && Core.isRunning(), "Core singleton lookup failed");
        host.width = 800;
        host.height = 600;
        core.setMode(400, 400, ViewportMode.FIT);
        check(Viewport.getScaleX() == 1.5f && Viewport.getScaleY() == 1.5f,
            "FIT viewport scale is incorrect");
        check(Viewport.getOffsetX() == 100.0f && Viewport.getOffsetY() == 0.0f,
            "FIT viewport offset is incorrect");

        check(core.loadImages("missing-images").length == 0,
            "A missing image folder must return an empty array");
        check(core.loadFonts("missing-fonts", 24).length == 0,
            "A missing font folder must return an empty array");
        check(core.loadImage("unsupported.txt") == null,
            "Unsupported image formats must be rejected safely");
        check(core.loadFont("unsupported.txt", 24) == null,
            "Unsupported font formats must be rejected safely");
        boolean rejectedFontSize = false;
        try { core.loadFonts("fonts", 0); }
        catch (IllegalArgumentException expected) { rejectedFontSize = true; }
        check(rejectedFontSize, "Font loading must require a positive size");

        core.fadeOff(0x80402010, 1000);
        check(core.isFading() && !core.isFaded(), "fadeOff must begin an active transition");
        host.now = 600;
        core.pre();
        check(core.isFading() && !core.isFaded(), "fadeOff must remain active before its duration");
        host.now = 1100;
        core.pre();
        check(!core.isFading() && core.isFaded(), "fadeOff must finish fully covered");
        core.fadeOn(500);
        check(core.isFading() && !core.isFaded(), "fadeOn must begin an active reveal");
        host.now = 1600;
        core.pre();
        check(!core.isFading() && !core.isFaded(), "fadeOn must finish fully visible");
        core.fadeOff(0);
        check(!core.isFading() && core.isFaded(), "A zero-duration fadeOff must be immediate");
        core.fadeOn(0);
        check(!core.isFading() && !core.isFaded(), "A zero-duration fadeOn must be immediate");

        core.keyEvent(new KeyEvent(null, 0, KeyEvent.PRESS, 0, 'a', 65));
        check(core.key(Task._A), "Lowercase A must be normalized for key(_A)");
        core.keyEvent(new KeyEvent(null, 0, KeyEvent.PRESS, 0, (char)PConstants.CODED, PConstants.LEFT));
        check(core.key(Task._LEFT), "Coded arrow must be available through key(_LEFT)");
        core.mouseEvent(new MouseEvent(null, 0, MouseEvent.PRESS, 0, 0, 0, PConstants.LEFT, 1));
        check(core.mouse.left, "Left mouse press was not retained");
        core.keyEvent(new KeyEvent(null, 0, KeyEvent.RELEASE, 0, 'a', 65));
        core.mouseEvent(new MouseEvent(null, 0, MouseEvent.RELEASE, 0, 0, 0, PConstants.LEFT, 1));
        check(!core.key(Task._A) && !core.mouse.left, "Released input must be cleared");

        boolean rejectedSecondHost = false;
        try { Core.start(p2dHost()); }
        catch (IllegalStateException expected) { rejectedSecondHost = true; }
        check(rejectedSecondHost, "A second PApplet host must be rejected");

        ArrayList<String> log = new ArrayList<String>();
        ProbeTask low = new ProbeTask("low", 1, log);
        ProbeTask high = new ProbeTask("high", 10, log);
        SpawnerTask spawner = new SpawnerTask(log);
        check(core.getTaskById(low.id) == low, "getTaskById failed");
        check(low.id > 0 && high.id > low.id, "Task identifiers must be positive and unique");
        check(low.mouse == core.mouse && !low.key(Task._A), "Tasks must share the Core input state");
        check(!low.timer("shot", 300), "A new timer must wait for its first interval");
        host.now += 299;
        check(!low.timer("shot", 300), "A timer must not fire before its interval");
        host.now += 1;
        check(low.timer("shot", 300) && !low.timer("shot", 300),
            "A timer must fire only once per elapsed interval");
        low.resetTimer("shot");
        host.now += 299;
        check(!low.timer("shot", 300), "resetTimer must restart the interval");
        host.now += 1;
        check(low.timer("shot", 300), "A reset timer must fire after the new interval");
        boolean rejectedTimerDuration = false;
        try { low.timer("invalid", 0); }
        catch (IllegalArgumentException expected) { rejectedTimerDuration = true; }
        check(rejectedTimerDuration, "Timer durations must be positive");
        low.scale(2).scalex(-1).scaley(0.5f);
        check(low.getEffectiveScaleX() == -1.0f && low.getEffectiveScaleY() == 0.5f,
            "Task scale/scalex/scaley composition is incorrect");
        low.setAlpha(123).tint(0xFF12AB34);
        check(low.tintColor == 0xFF12AB34, "Task tint color was not retained");
        check(low.alpha == 123.0f, "Task tint must preserve alpha");
        low.clearTint();
        check(low.tintColor == 0xFFFFFFFF && low.alpha == 123.0f,
            "Clearing Task tint must preserve alpha");
        low.setGraph(new PImage(20, 10, PConstants.ARGB)).setPosition(100, 100).scale(1).setAngle(90);
        check(low.containsPoint(100, 109), "Rotated Task must contain a point on its long axis");
        check(!low.containsPoint(109, 100), "Rotated Task must reject a point outside its short axis");
        core.mouseEvent(new MouseEvent(null, 0, MouseEvent.PRESS, 0, 250, 150, PConstants.LEFT, 1));
        check(core.points.size() == 1 && low.isTouched() && low.point == core.points.get(0),
            "isTouched must capture the active point inside a rotated Task");
        core.mouseEvent(new MouseEvent(null, 0, MouseEvent.DRAG, 0, 265, 150, PConstants.LEFT, 1));
        check(low.isTouched() && Math.abs(low.point.deltaX - 10.0f) < 0.001f,
            "A captured point must remain attached and expose logical movement");
        core.mouseEvent(new MouseEvent(null, 0, MouseEvent.RELEASE, 0, 265, 150, PConstants.LEFT, 1));
        check(core.points.isEmpty() && !low.isTouched() && low.point == null,
            "A released point must clear Task capture");
        high.setGraph(new PImage(20, 10, PConstants.ARGB)).setPosition(100, 118).setAngle(90);
        check(low.overlap(high) && high.overlap(low), "Rotated Task boxes must overlap symmetrically");
        high.setPosition(100, 121);
        check(!low.overlap(high), "Separated rotated Task boxes must not overlap");
        low.text(null, 18, "Score", PConstants.LEFT, 20, 30, 0xFFFFFFFF, 255);
        check(low.isDrawable(), "A Task with queued text must participate in rendering");

        core.pre();
        check(log.indexOf("frame:high") < log.indexOf("frame:spawner"), "Priority order is incorrect");
        check(log.indexOf("frame:spawner") < log.indexOf("frame:low"), "Priority order is incorrect");
        check(spawner.child != null && spawner.child.liveFrames == 0,
            "A task created during update must wait until the next frame");

        core.pre();
        check(spawner.child.liveFrames == 1, "Pending task did not run on the next frame");

        low.setKillProtection(true);
        core.signal(low, Core.SIGNAL_KILL);
        check(low.live, "Protected task must ignore SIGNAL_KILL");
        core.signal(low, Core.SIGNAL_UNPROTECT);
        core.signal(low, Core.SIGNAL_KILL);
        core.pre();
        check(!Core.exists(low) && low.destroyedCount == 1, "Killed task must be destroyed once");

        Scene level = core.createScene().activate();
        Task cameraTarget = new Task();
        cameraTarget.setGraph(new PImage(20, 20, PConstants.ARGB)).setPosition(150, 150);
        check(cameraTarget.scene == level && level.contains(cameraTarget),
            "Tasks created with an active Scene must be attached automatically");
        level.camera.setDeadZone(100, 100, 200, 200).setBounds(0, 0, 1000, 800);
        level.camera.setTarget(cameraTarget);
        core.pre();
        check(level.camera.x == 0 && level.camera.y == 0,
            "A target inside the dead zone must not move the camera");
        cameraTarget.setPosition(350, 150);
        core.pre();
        check(level.camera.x == 50 && level.camera.worldToScreenX(cameraTarget.x) == 300,
            "The camera must retain its target at the reached dead-zone edge");
        cameraTarget.setPosition(950, 750);
        core.pre();
        check(level.camera.x == 600 && level.camera.y == 400,
            "Camera bounds must prevent exposing space outside the world");
        Scene inactive = core.createScene();
        Task pausedTask = new Task();
        inactive.add(pausedTask);
        core.pre();
        check(pausedTask.liveFrames == 0, "Tasks in an inactive Scene must remain paused");
        inactive.activate();
        core.pre();
        check(pausedTask.liveFrames == 1, "Activating a Scene must resume its Tasks");
        core.setScene(null);

        core.setAutomaticRendering(false);
        check(!core.isAutomaticRendering(), "Manual rendering mode was not retained");
        Core.shutdown();
        check(!Core.isRunning(), "Core.shutdown must release the singleton");
        check(high.destroyedCount == 1 && spawner.destroyedCount == 1 &&
            spawner.child.destroyedCount == 1, "Shutdown must destroy every remaining task once");

        Core restarted = Core.start(java2dHost());
        ProbeTask fresh = new ProbeTask("fresh", 0, new ArrayList<String>());
        check(fresh.id == 1, "A clean Core restart must reset task identifiers");
        Core.shutdown();
        check(!Core.isRunning(), "Restarted Core did not shut down");

        System.out.println("SimpleCore singleton and task lifecycle passed.");
    }
}
