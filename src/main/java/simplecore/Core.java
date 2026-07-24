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
import processing.core.PApplet;

/** A single Processing-bound task engine. */
public final class Core {
    public static final String LIBRARY_AUTHOR = "Luis lopez martinez";
    public static final String LIBRARY_LICENSE = "MIT";
    public static final int SIGNAL_KILL = -1;
    public static final int SIGNAL_PROTECT = -2;
    public static final int SIGNAL_UNPROTECT = -3;

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
    private boolean updating;
    private boolean shuttingDown;
    private boolean automaticRendering = true;
    private int lastTaskId;
    private float canvasScale = 1.0f;
    private Task caller;

    private Core(PApplet parent) {
        this.parent = parent;
        parent.registerMethod("pre", this);
        parent.registerMethod("draw", this);
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
        return canvasScale;
    }

    public void setTaskCanvasScale(float value) {
        canvasScale = Math.max(0.0001f, value);
    }

    public Task getCaller() {
        return caller;
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
        parent.scale(canvasScale);
        try {
            for (Task task : drawableTasks) if (task.isDrawable()) task.render();
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
        caller = null;
        updating = false;
        lastTaskId = 0;
        synchronized (Core.class) {
            if (instance == this) instance = null;
        }
        if (firstFailure != null) throw firstFailure;
    }
}
