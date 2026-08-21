/*
 * SimpleCore — designed and developed by Luis lopez martinez.
 * Copyright (c) 2026 Luis lopez martinez. Licensed under the MIT License.
 * SPDX-License-Identifier: MIT
 */
package simplecore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.HashSet;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/** A single Processing-bound task engine. */
public final class Core {
    public static final String LIBRARY_AUTHOR = "Luis lopez martinez";
    public static final String LIBRARY_LICENSE = "MIT";
    public static final int SIGNAL_KILL = -1;
    public static final int SIGNAL_PROTECT = -2;
    public static final int SIGNAL_UNPROTECT = -3;

    public static final int MOUSE_LEFT = PConstants.LEFT;
    public static final int MOUSE_RIGHT = PConstants.RIGHT;
    public static final int MOUSE_CENTER = PConstants.CENTER;

    private static final Comparator<Task> PRIORITY_ORDER = new Comparator<Task>() {
        public int compare(Task first, Task second) {
            return Integer.compare(second.priority, first.priority);
        }
    };
    private static final Comparator<Task> Z_ORDER = new Comparator<Task>() {
        public int compare(Task first, Task second) {
            return Integer.compare(first.z, second.z);
        }
    };

    private static Core instance;

    private final PApplet parent;
    private final ArrayList<Task> tasks = new ArrayList<Task>();
    private final ArrayList<Task> pendingTasks = new ArrayList<Task>();
    private final ArrayList<Task> drawableTasks = new ArrayList<Task>();
    private final HashSet<Integer> pressedKeys = new HashSet<Integer>();
    public final CoreMouse mouse = new CoreMouse();
    private boolean updating;
    private boolean shuttingDown;
    private boolean automaticRendering = true;
    private int lastTaskId;
    private Task caller;

    private Core(PApplet parent) {
        this.parent = parent;
        Viewport.attach(parent);
        parent.registerMethod("pre", this);
        parent.registerMethod("draw", this);
        parent.registerMethod("keyEvent", this);
        parent.registerMethod("mouseEvent", this);
        parent.registerMethod("dispose", this);
    }

    public static synchronized Core start(PApplet parent) {
        if (parent == null) throw new IllegalArgumentException("Core requires a PApplet host");
        if (instance == null) {
            instance = new Core(parent);
            return instance;
        }
        if (instance.parent != parent) {
            throw new IllegalStateException("Core is already running with a different PApplet host");
        }
        return instance;
    }

    public static synchronized Core getInstance() {
        if (instance == null) throw new IllegalStateException("Core has not been started");
        return instance;
    }

    static synchronized Core requireInstance() {
        return getInstance();
    }

    public static synchronized boolean isRunning() {
        return instance != null;
    }

    public static void shutdown() {
        Core current;
        synchronized (Core.class) {
            current = instance;
        }
        if (current != null) current.shutdownInternal();
    }

    public PApplet getParent() {
        return parent;
    }

    public boolean isAutomaticRendering() {
        return automaticRendering;
    }

    public void setAutomaticRendering(boolean enabled) {
        automaticRendering = enabled;
    }

    public float getTaskCanvasScale() {
        return Viewport.getScaleX();
    }

    /** @deprecated Use setMode() so SimpleCore and SimpleUI share one viewport. */
    @Deprecated
    public void setTaskCanvasScale(float value) {
        Viewport.setManualScale(value);
    }

    public void setMode(float width, float height) {
        setMode(width, height, ViewportMode.FIT);
    }

    public void setMode(float width, float height, ViewportMode mode) {
        Viewport.setMode(width, height, mode);
    }

    public float getLogicalWidth() {
        return Viewport.getLogicalWidth();
    }

    public float getLogicalHeight() {
        return Viewport.getLogicalHeight();
    }

    public Task getCaller() {
        return caller;
    }

    /** Returns true while the requested key is held down. */
    public boolean key(int code) {
        return pressedKeys.contains(code);
    }

    /** Processing keyboard callback; installed automatically by start(). */
    public void keyEvent(KeyEvent event) {
        if (event == null || shuttingDown) return;
        int code = normalizeKey(event);
        if (code == 0) return;
        if (event.getAction() == KeyEvent.PRESS) pressedKeys.add(code);
        else if (event.getAction() == KeyEvent.RELEASE) pressedKeys.remove(code);
    }

    /** Processing pointer callback; a touch is exposed as mouse.left on Android. */
    public void mouseEvent(MouseEvent event) {
        if (event == null || shuttingDown) return;
        Viewport.update();
        mouse.move(event.getX(), event.getY());
        if (event.getAction() == MouseEvent.PRESS) mouse.press(event.getButton());
        else if (event.getAction() == MouseEvent.RELEASE) mouse.release(event.getButton());
    }

    /** Clears keyboard and mouse state. */
    public void clearInput() {
        pressedKeys.clear();
        mouse.clear();
    }

    private int normalizeKey(KeyEvent event) {
        char value = event.getKey();
        if (value == PConstants.CODED) return event.getKeyCode();
        return Character.toUpperCase(value);
    }

    public int getTaskCount() {
        return tasks.size() + pendingTasks.size();
    }

    public ArrayList<Task> getTasksSnapshot() {
        ArrayList<Task> result = new ArrayList<Task>(tasks);
        result.addAll(pendingTasks);
        return result;
    }

    public Task getTaskById(int id) {
        for (Task task : tasks) if (task.id == id) return task;
        for (Task task : pendingTasks) if (task.id == id) return task;
        return null;
    }

    public static boolean exists(Task task) {
        return task != null && task.live && !task.destroyed;
    }

    public void signal(Task task, int signal) {
        if (task == null || task.destroyed) return;
        switch (signal) {
            case SIGNAL_KILL:
                if (!task.killProtection) task.live = false;
                break;
            case SIGNAL_PROTECT:
                task.killProtection = true;
                break;
            case SIGNAL_UNPROTECT:
                task.killProtection = false;
                break;
            default:
                break;
        }
    }

    public void signal(int type, int signal) {
        for (Task task : getTasksSnapshot()) if (task.type == type) signal(task, signal);
    }

    public void signal(String className, int signal) {
        if (className == null) return;
        for (Task task : getTasksSnapshot()) {
            if (className.equals(task.className)) signal(task, signal);
        }
    }

    public void letMeAlone() {
        for (Task task : getTasksSnapshot()) {
            if (task != caller && !task.killProtection) task.live = false;
        }
    }

    int registerTask(Task task) {
        if (shuttingDown) throw new IllegalStateException("Cannot create tasks while Core is shutting down");
        int id = generateTaskId();
        if (updating) pendingTasks.add(task);
        else tasks.add(task);
        return id;
    }

    private int generateTaskId() {
        int firstCandidate = lastTaskId == Integer.MAX_VALUE ? 1 : lastTaskId + 1;
        int candidate = firstCandidate;
        do {
            if (candidate > 0 && getTaskById(candidate) == null) {
                lastTaskId = candidate;
                return candidate;
            }
            candidate = candidate == Integer.MAX_VALUE ? 1 : candidate + 1;
        } while (candidate != firstCandidate);
        throw new IllegalStateException("SimpleCore has no free Task identifiers");
    }

    /** Processing callback invoked before the sketch draw method. */
    public void pre() {
        if (shuttingDown) return;
        Viewport.update();
        if (!parent.focused) clearInput();
        Collections.sort(tasks, PRIORITY_ORDER);
        updating = true;
        try {
            for (int index = 0; index < tasks.size();) {
                Task task = tasks.get(index);
                caller = task;
                if (!task.live) {
                    destroyTaskAt(index);
                    continue;
                }
                task.beginFrame();
                if (task.liveFrames == 0) {
                    task.initialize();
                    if (!task.live) {
                        destroyTaskAt(index);
                        continue;
                    }
                }
                task.liveFrames++;
                task.frame();
                if (!task.live) {
                    destroyTaskAt(index);
                    continue;
                }
                index++;
            }
        } finally {
            caller = null;
            updating = false;
            if (!pendingTasks.isEmpty()) {
                tasks.addAll(pendingTasks);
                pendingTasks.clear();
            }
        }
    }

    /** Processing callback invoked after the sketch draw method. */
    public void draw() {
        if (!shuttingDown && automaticRendering) renderTasks();
    }

    public void renderTasks() {
        if (shuttingDown) return;
        drawableTasks.clear();
        for (Task task : tasks) if (task.isDrawable()) drawableTasks.add(task);
        Collections.sort(drawableTasks, Z_ORDER);
        parent.pushMatrix();
        parent.translate(Viewport.getOffsetX(), Viewport.getOffsetY());
        parent.scale(Viewport.getScaleX(), Viewport.getScaleY());
        try {
            for (Task task : drawableTasks) {
                if (!task.isDrawable()) continue;
                task.render();
                task.renderText();
            }
        } finally {
            parent.popMatrix();
            drawableTasks.clear();
        }
    }

    private void destroyTaskAt(int index) {
        Task task = tasks.remove(index);
        destroyTask(task);
    }

    private void destroyTask(Task task) {
        if (task == null || task.destroyed) return;
        task.live = false;
        task.destroyed = true;
        Task previousCaller = caller;
        caller = task;
        try {
            task.onDestroy();
        } finally {
            caller = previousCaller;
        }
    }

    /** Processing disposal callback. */
    public void dispose() {
        synchronized (Core.class) {
            if (instance != this) return;
        }
        shutdownInternal();
    }

    private void shutdownInternal() {
        synchronized (Core.class) {
            if (instance != this || shuttingDown) return;
            shuttingDown = true;
        }
        parent.unregisterMethod("pre", this);
        parent.unregisterMethod("draw", this);
        parent.unregisterMethod("keyEvent", this);
        parent.unregisterMethod("mouseEvent", this);
        parent.unregisterMethod("dispose", this);

        RuntimeException firstFailure = null;
        IdentityHashMap<Task, Boolean> seen = new IdentityHashMap<Task, Boolean>();
        ArrayList<Task> allTasks = new ArrayList<Task>(tasks);
        allTasks.addAll(pendingTasks);
        for (Task task : allTasks) {
            if (seen.put(task, Boolean.TRUE) != null) continue;
            try {
                destroyTask(task);
            } catch (RuntimeException failure) {
                if (firstFailure == null) firstFailure = failure;
                else firstFailure.addSuppressed(failure);
            }
        }

        tasks.clear();
        pendingTasks.clear();
        drawableTasks.clear();
        clearInput();
        caller = null;
        updating = false;
        lastTaskId = 0;
        synchronized (Core.class) {
            if (instance == this) instance = null;
        }
        if (firstFailure != null) throw firstFailure;
    }
}
