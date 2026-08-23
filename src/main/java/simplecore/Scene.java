/*
 * SimpleCore — designed and developed by Luis lopez martinez.
 * Copyright (c) 2026 Luis lopez martinez. Licensed under the MIT License.
 * SPDX-License-Identifier: MIT
 */
package simplecore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A group of Tasks sharing one 2D camera. */
public final class Scene {
    private final Core core;
    private final ArrayList<Task> sceneTasks = new ArrayList<Task>();
    public final List<Task> tasks = Collections.unmodifiableList(sceneTasks);
    public final Camera camera;

    public Scene() {
        core = Core.requireInstance();
        camera = new Camera(this);
        core.registerScene(this);
    }

    public Scene add(Task task) {
        if (task == null) return this;
        if (task.getCore() != core) throw new IllegalArgumentException("Task belongs to another Core");
        if (task.scene == this) return this;
        if (task.scene != null) task.scene.removeInternal(task);
        task.scene = this;
        if (!sceneTasks.contains(task)) sceneTasks.add(task);
        return this;
    }

    public Scene remove(Task task) {
        if (task != null && task.scene == this) {
            removeInternal(task);
            task.scene = null;
        }
        return this;
    }

    public boolean contains(Task task) {
        return task != null && task.scene == this && sceneTasks.contains(task);
    }

    public Scene activate() {
        core.setScene(this);
        return this;
    }

    public boolean isActive() {
        return core.getScene() == this;
    }

    Core getCore() {
        return core;
    }

    void removeInternal(Task task) {
        sceneTasks.remove(task);
        if (camera.target == task) camera.clearTarget();
    }

    void clearInternal() {
        for (Task task : new ArrayList<Task>(sceneTasks)) {
            if (task.scene == this) task.scene = null;
        }
        sceneTasks.clear();
        camera.clearTarget();
    }
}
