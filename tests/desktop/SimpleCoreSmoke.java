import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import simplecore.Core;
import simplecore.Task;
import simplecore.Viewport;
import simplecore.ViewportMode;

public class SimpleCoreSmoke {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
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

        PApplet host = new PApplet();
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
        try { Core.start(new PApplet()); }
        catch (IllegalStateException expected) { rejectedSecondHost = true; }
        check(rejectedSecondHost, "A second PApplet host must be rejected");

        ArrayList<String> log = new ArrayList<String>();
        ProbeTask low = new ProbeTask("low", 1, log);
        ProbeTask high = new ProbeTask("high", 10, log);
        SpawnerTask spawner = new SpawnerTask(log);
        check(core.getTaskById(low.id) == low, "getTaskById failed");
        check(low.id > 0 && high.id > low.id, "Task identifiers must be positive and unique");
        check(low.mouse == core.mouse && !low.key(Task._A), "Tasks must share the Core input state");
        low.scale(2).scalex(-1).scaley(0.5f);
        check(low.getEffectiveScaleX() == -1.0f && low.getEffectiveScaleY() == 0.5f,
            "Task scale/scalex/scaley composition is incorrect");
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

        core.setAutomaticRendering(false);
        check(!core.isAutomaticRendering(), "Manual rendering mode was not retained");
        Core.shutdown();
        check(!Core.isRunning(), "Core.shutdown must release the singleton");
        check(high.destroyedCount == 1 && spawner.destroyedCount == 1 &&
            spawner.child.destroyedCount == 1, "Shutdown must destroy every remaining task once");

        Core restarted = Core.start(new PApplet());
        ProbeTask fresh = new ProbeTask("fresh", 0, new ArrayList<String>());
        check(fresh.id == 1, "A clean Core restart must reset task identifiers");
        Core.shutdown();
        check(!Core.isRunning(), "Restarted Core did not shut down");

        System.out.println("SimpleCore singleton and task lifecycle passed.");
    }
}
