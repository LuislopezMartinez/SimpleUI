import java.util.ArrayList;
import processing.core.PApplet;
import simplecore.Core;
import simplecore.Task;

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
