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
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.File;
import java.lang.reflect.Constructor;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.event.TouchEvent;
import simplecore.internal.AudioPlatform;
import simplecore.internal.FailedSoundBackend;
import simplecore.internal.SoundBackend;

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

    private static final int DEFAULT_FADE_DURATION = 500;

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
    private static PApplet videoModeHost;
    private static int videoModeWidth;
    private static int videoModeHeight;

    private final PApplet parent;
    private final Object taskLock = new Object();
    private final ArrayList<Task> tasks = new ArrayList<Task>();
    private final ArrayList<Task> pendingTasks = new ArrayList<Task>();
    private final ArrayList<Task> drawableTasks = new ArrayList<Task>();
    private final ArrayList<Scene> scenes = new ArrayList<Scene>();
    private final ArrayList<Sound> sounds = new ArrayList<Sound>();
    // Input callbacks and P2D drawing can run on different Android threads.
    // Snapshot iterators prevent structural changes from crashing GLThread.
    private final CopyOnWriteArrayList<TouchPoint> activePoints =
        new CopyOnWriteArrayList<TouchPoint>();
    public final java.util.List<TouchPoint> points = Collections.unmodifiableList(activePoints);
    private final HashSet<Integer> pressedKeys = new HashSet<Integer>();
    public final CoreMouse mouse = new CoreMouse();
    private boolean updating;
    private boolean shuttingDown;
    private boolean automaticRendering = true;
    private int lastTaskId;
    private Task caller;
    private AudioPlatform audioPlatform;
    private String audioPlatformError;
    private Scene activeScene;
    private int fadeColor = 0xFF000000;
    private float fadeAmount;
    private float fadeStartAmount;
    private float fadeTargetAmount;
    private int fadeStartMillis;
    private int fadeDuration;
    private boolean fading;
    private final boolean androidRuntime;

    private Core(PApplet parent) {
        this.parent = parent;
        androidRuntime = detectAndroidRuntime(parent);
        Viewport.attach(parent);
        parent.registerMethod("pre", this);
        parent.registerMethod("draw", this);
        parent.registerMethod("keyEvent", this);
        parent.registerMethod("mouseEvent", this);
        if (androidRuntime) parent.registerMethod("touchEvent", this);
        parent.registerMethod("dispose", this);
    }

    /** Selects the surface and logical resolution for a SimpleCore-only sketch. */
    public static void setVideoMode(PApplet parent, int width, int height, String renderer) {
        if (parent == null) throw new IllegalArgumentException("Core requires a PApplet host");
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Video mode dimensions must be greater than zero");
        if (renderer == null || renderer.trim().isEmpty()) throw new IllegalArgumentException("Video mode requires an explicit renderer");
        videoModeHost = parent;
        videoModeWidth = width;
        videoModeHeight = height;
        if (detectAndroidRuntime(parent)) parent.fullScreen(renderer);
        else parent.size(width, height, renderer);
    }

    /** Starts a SimpleCore-only sketch using the video mode selected in settings(). */
    public static synchronized Core start(PApplet parent, ViewportMode mode) {
        if (videoModeHost != parent) {
            throw new IllegalStateException("Call Core.setVideoMode(this, width, height, renderer) from settings() before Core.start(this, mode)");
        }
        if (mode == null) throw new IllegalArgumentException("Viewport mode is required");
        if (detectAndroidRuntime(parent)) {
            parent.orientation(videoModeWidth > videoModeHeight ? PConstants.LANDSCAPE : PConstants.PORTRAIT);
        }
        Core core = start(parent);
        core.setMode(videoModeWidth, videoModeHeight, mode);
        return core;
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

    public Scene createScene() {
        return new Scene();
    }

    public void setScene(Scene scene) {
        if (scene != null && !scenes.contains(scene)) {
            throw new IllegalArgumentException("Scene belongs to another Core");
        }
        activeScene = scene;
        if (activeScene != null) activeScene.camera.update();
    }

    public Scene getScene() {
        return activeScene;
    }

    void registerScene(Scene scene) {
        if (scene == null || scenes.contains(scene)) return;
        scenes.add(scene);
    }

    void attachToActiveScene(Task task) {
        if (activeScene != null) activeScene.add(task);
    }

    /** Loads one supported image from the sketch data folder. */
    public PImage loadImage(String filename) {
        String safeFilename = normalizeResourcePath(filename);
        if (!isImageFilename(safeFilename)) return null;
        try {
            return parent.loadImage(safeFilename);
        } catch (RuntimeException failure) {
            return null;
        }
    }

    /** Loads supported images from a data-folder directory, alphabetically. */
    public PImage[] loadImages(String folderName) {
        ArrayList<PImage> result = new ArrayList<PImage>();
        for (String filename : listResourceFiles(folderName)) {
            if (!isImageFilename(filename)) continue;
            PImage image = loadImage(joinResourcePath(folderName, filename));
            if (image != null) result.add(image);
        }
        return result.toArray(new PImage[result.size()]);
    }

    /** Loads one TTF/OTF font at the requested size, or a prebuilt VLW font. */
    public PFont loadFont(String filename, float size) {
        requireFontSize(size);
        String safeFilename = normalizeResourcePath(filename);
        if (!isFontFilename(safeFilename)) return null;
        try {
            if (safeFilename.toLowerCase(java.util.Locale.ROOT).endsWith(".vlw")) {
                return parent.loadFont(safeFilename);
            }
            return parent.createFont(safeFilename, size, true);
        } catch (RuntimeException failure) {
            return null;
        }
    }

    /** Loads supported fonts from a data-folder directory, alphabetically. */
    public PFont[] loadFonts(String folderName, float size) {
        requireFontSize(size);
        ArrayList<PFont> result = new ArrayList<PFont>();
        for (String filename : listResourceFiles(folderName)) {
            if (!isFontFilename(filename)) continue;
            PFont font = loadFont(joinResourcePath(folderName, filename), size);
            if (font != null) result.add(font);
        }
        return result.toArray(new PFont[result.size()]);
    }

    private ArrayList<String> listResourceFiles(String folderName) {
        String safeFolder = normalizeResourcePath(folderName);
        ArrayList<String> result = new ArrayList<String>();
        if (safeFolder.length() == 0) return result;
        try {
            if (isAndroidRuntime()) {
                Object activity = parent.getClass().getMethod("getActivity").invoke(parent);
                Object assets = activity.getClass().getMethod("getAssets").invoke(activity);
                String[] names = (String[])assets.getClass()
                    .getMethod("list", String.class).invoke(assets, safeFolder);
                if (names != null) Collections.addAll(result, names);
            } else {
                File directory = new File(parent.dataPath(safeFolder));
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) if (file.isFile()) result.add(file.getName());
                }
            }
        } catch (Exception ignored) {}
        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    private boolean isAndroidRuntime() {
        return androidRuntime;
    }

    private static boolean detectAndroidRuntime(PApplet parent) {
        try {
            parent.getClass().getMethod("getActivity");
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static String normalizeResourcePath(String value) {
        if (value == null) return "";
        String result = value.replace('\\', '/').trim();
        while (result.startsWith("/")) result = result.substring(1);
        while (result.endsWith("/")) result = result.substring(0, result.length() - 1);
        return result;
    }

    private static String joinResourcePath(String folderName, String filename) {
        String folder = normalizeResourcePath(folderName);
        String file = normalizeResourcePath(filename);
        return folder.length() == 0 ? file : folder + "/" + file;
    }

    private static boolean isImageFilename(String filename) {
        String lower = filename.toLowerCase(java.util.Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") ||
            lower.endsWith(".jpeg") || lower.endsWith(".gif") || lower.endsWith(".tga");
    }

    private static boolean isFontFilename(String filename) {
        String lower = filename.toLowerCase(java.util.Locale.ROOT);
        return lower.endsWith(".ttf") || lower.endsWith(".otf") || lower.endsWith(".vlw");
    }

    private static void requireFontSize(float size) {
        if (!Float.isFinite(size) || size <= 0.0f) {
            throw new IllegalArgumentException("Font size must be positive and finite");
        }
    }

    /** Covers the screen with black using the default duration. */
    public void fadeOff() {
        fadeOff(0xFF000000, DEFAULT_FADE_DURATION);
    }

    /** Covers the screen with black in the requested number of milliseconds. */
    public void fadeOff(int durationMillis) {
        fadeOff(0xFF000000, durationMillis);
    }

    /** Covers the screen with a Processing color, preserving its alpha. */
    public void fadeOff(int color, int durationMillis) {
        updateFade();
        fadeColor = color;
        beginFade(1.0f, durationMillis);
    }

    /** Reveals the screen using the previous fade color and default duration. */
    public void fadeOn() {
        fadeOn(DEFAULT_FADE_DURATION);
    }

    /** Reveals the screen using the previous fade color. */
    public void fadeOn(int durationMillis) {
        updateFade();
        beginFade(0.0f, durationMillis);
    }

    /** Returns true while a fade transition is moving. */
    public boolean isFading() {
        updateFade();
        return fading;
    }

    /** Returns true once fadeOff has completely covered the screen. */
    public boolean isFaded() {
        updateFade();
        return !fading && fadeAmount >= 1.0f;
    }

    private void beginFade(float targetAmount, int durationMillis) {
        fadeStartAmount = fadeAmount;
        fadeTargetAmount = targetAmount;
        fadeStartMillis = parent.millis();
        fadeDuration = Math.max(0, durationMillis);
        fading = fadeDuration > 0 && fadeStartAmount != fadeTargetAmount;
        if (!fading) fadeAmount = fadeTargetAmount;
    }

    private void updateFade() {
        if (!fading) return;
        float elapsed = Math.max(0, parent.millis() - fadeStartMillis);
        float progress = Math.min(1.0f, elapsed / fadeDuration);
        float smoothProgress = progress * progress * (3.0f - 2.0f * progress);
        fadeAmount = fadeStartAmount + (fadeTargetAmount - fadeStartAmount) * smoothProgress;
        if (progress >= 1.0f) {
            fadeAmount = fadeTargetAmount;
            fading = false;
        }
    }

    private void renderFade() {
        if (fadeAmount <= 0.0f) return;
        float colorAlpha = (fadeColor >>> 24) & 0xFF;
        float overlayAlpha = colorAlpha * fadeAmount;
        if (overlayAlpha <= 0.0f) return;
        parent.pushMatrix();
        parent.pushStyle();
        try {
            parent.resetMatrix();
            parent.rectMode(PConstants.CORNER);
            parent.noStroke();
            parent.fill(fadeColor, overlayAlpha);
            parent.rect(0, 0, parent.width, parent.height);
        } finally {
            parent.popStyle();
            parent.popMatrix();
        }
    }

    /** Loads an MP3, Ogg Vorbis or PCM WAV file from the sketch data folder. */
    public Sound loadSound(String filename) {
        String safeFilename = filename == null ? "" : filename.replace('\\', '/').trim();
        SoundBackend backend;
        if (safeFilename.length() == 0) {
            backend = new FailedSoundBackend("Sound filename must not be empty");
        } else {
            AudioPlatform platform = getAudioPlatform();
            backend = platform == null
                ? new FailedSoundBackend(audioPlatformError)
                : platform.load(safeFilename);
            if (backend == null) backend = new FailedSoundBackend("Audio backend returned no sound");
        }
        Sound sound = new Sound(this, safeFilename, backend);
        sounds.add(sound);
        return sound;
    }

    /** Loads every supported sound in a data-folder directory, alphabetically. */
    public Sound[] loadSounds(String folderName) {
        String safeFolder = folderName == null ? "" : folderName.replace('\\', '/').trim();
        AudioPlatform platform = getAudioPlatform();
        if (platform == null || safeFolder.length() == 0) return new Sound[0];
        String[] filenames = platform.list(safeFolder);
        if (filenames == null) return new Sound[0];
        ArrayList<String> supported = new ArrayList<String>();
        for (String filename : filenames) {
            if (filename == null) continue;
            String lower = filename.toLowerCase(java.util.Locale.ROOT);
            if (lower.endsWith(".mp3") || lower.endsWith(".ogg") || lower.endsWith(".wav")) {
                supported.add(filename.replace('\\', '/'));
            }
        }
        Collections.sort(supported, String.CASE_INSENSITIVE_ORDER);
        Sound[] result = new Sound[supported.size()];
        for (int index = 0; index < result.length; index++) result[index] = loadSound(supported.get(index));
        return result;
    }

    private AudioPlatform getAudioPlatform() {
        if (audioPlatform != null || audioPlatformError != null) return audioPlatform;
        String className = isAndroidRuntime()
            ? "simplecore.audio.AndroidAudioPlatform"
            : "simplecore.audio.DesktopAudioPlatform";
        try {
            Class<?> platformType = Class.forName(className);
            Constructor<?> constructor = platformType.getConstructor(PApplet.class);
            audioPlatform = (AudioPlatform)constructor.newInstance(parent);
        } catch (Exception failure) {
            Throwable cause = failure.getCause() == null ? failure : failure.getCause();
            audioPlatformError = "Cannot initialize audio: " + cause.toString();
        }
        return audioPlatform;
    }

    void unregisterSound(Sound sound) {
        sounds.remove(sound);
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
        if (!androidRuntime) updateMousePoint();
    }

    /** Processing Android multitouch callback; installed automatically. */
    public void touchEvent(TouchEvent event) {
        if (event == null || shuttingDown || !androidRuntime) return;
        Viewport.update();
        try {
            int count = ((Number)event.getClass().getMethod("getNumPointers").invoke(event)).intValue();
            ArrayList<TouchPoint> next = new ArrayList<TouchPoint>(count);
            for (int index = 0; index < count; index++) {
                int id = invokeTouchInt(event, "getPointerId", index);
                float screenX = invokeTouchFloat(event, "getPointerX", index);
                float screenY = invokeTouchFloat(event, "getPointerY", index);
                float area = invokeTouchFloat(event, "getPointerArea", index);
                float pressure = invokeTouchFloat(event, "getPointerPressure", index);
                TouchPoint point = findActivePoint(id);
                float logicalX = Viewport.screenToDesignX(screenX);
                float logicalY = Viewport.screenToDesignY(screenY);
                if (point == null) point = new TouchPoint(id, logicalX, logicalY, area, pressure);
                else point.update(logicalX, logicalY, area, pressure);
                next.add(point);
            }
            activePoints.clear();
            activePoints.addAll(next);
        } catch (Exception failure) {
            activePoints.clear();
        }
    }

    private static int invokeTouchInt(TouchEvent event, String method, int index) throws Exception {
        return ((Number)event.getClass().getMethod(method, Integer.TYPE).invoke(event, index)).intValue();
    }

    private static float invokeTouchFloat(TouchEvent event, String method, int index) throws Exception {
        return ((Number)event.getClass().getMethod(method, Integer.TYPE).invoke(event, index)).floatValue();
    }

    private void updateMousePoint() {
        boolean pressed = mouse.left || mouse.right || mouse.center;
        if (!pressed) {
            activePoints.clear();
            return;
        }
        TouchPoint point = findActivePoint(0);
        if (point == null) {
            activePoints.clear();
            activePoints.add(new TouchPoint(0, mouse.x, mouse.y, 1.0f, 1.0f));
        } else {
            point.update(mouse.x, mouse.y, 1.0f, 1.0f);
        }
    }

    /** Clears keyboard and mouse state. */
    public void clearInput() {
        pressedKeys.clear();
        mouse.clear();
        activePoints.clear();
    }

    TouchPoint findActivePoint(int id) {
        for (TouchPoint point : activePoints) if (point.id == id) return point;
        return null;
    }

    boolean isActivePoint(TouchPoint target) {
        if (target == null) return false;
        for (TouchPoint point : activePoints) if (point == target) return true;
        return false;
    }

    private int normalizeKey(KeyEvent event) {
        char value = event.getKey();
        if (value == PConstants.CODED) return event.getKeyCode();
        return Character.toUpperCase(value);
    }

    public int getTaskCount() {
        synchronized (taskLock) {
            return tasks.size() + pendingTasks.size();
        }
    }

    public ArrayList<Task> getTasksSnapshot() {
        synchronized (taskLock) {
            ArrayList<Task> result = new ArrayList<Task>(tasks);
            result.addAll(pendingTasks);
            return result;
        }
    }

    public Task getTaskById(int id) {
        synchronized (taskLock) {
            for (Task task : tasks) if (task.id == id) return task;
            for (Task task : pendingTasks) if (task.id == id) return task;
            return null;
        }
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
        synchronized (taskLock) {
            if (shuttingDown) throw new IllegalStateException("Cannot create tasks while Core is shutting down");
            int id = generateTaskId();
            if (updating) pendingTasks.add(task);
            else tasks.add(task);
            return id;
        }
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
        updateFade();
        if (!parent.focused) clearInput();
        synchronized (taskLock) {
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
                    if (task.scene != null && task.scene != activeScene) {
                        index++;
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
                if (activeScene != null) activeScene.camera.update();
            }
        }
    }

    /** Processing callback invoked after the sketch draw method. */
    public void draw() {
        if (shuttingDown) return;
        try {
            if (automaticRendering) renderTasks();
            updateFade();
            renderFade();
        } finally {
            for (TouchPoint point : activePoints) {
                point.deltaX = 0.0f;
                point.deltaY = 0.0f;
            }
        }
    }

    public void renderTasks() {
        if (shuttingDown) return;
        synchronized (taskLock) {
            drawableTasks.clear();
            for (Task task : tasks) {
                if (task.isDrawable() && (task.scene == null || task.scene == activeScene)) {
                    drawableTasks.add(task);
                }
            }
            Collections.sort(drawableTasks, Z_ORDER);
            parent.pushMatrix();
            parent.translate(Viewport.getOffsetX(), Viewport.getOffsetY());
            parent.scale(Viewport.getScaleX(), Viewport.getScaleY());
            try {
                for (Task task : drawableTasks) {
                    if (!task.isDrawable()) continue;
                    parent.pushMatrix();
                    try {
                        if (task.scene != null) {
                            parent.translate(-task.scene.camera.x, -task.scene.camera.y);
                        }
                        task.render();
                        task.renderText();
                    } finally {
                        parent.popMatrix();
                    }
                }
            } finally {
                parent.popMatrix();
                drawableTasks.clear();
            }
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
        task.releaseInternalState();
        Scene owner = task.scene;
        Task previousCaller = caller;
        caller = task;
        try {
            task.onDestroy();
        } finally {
            if (owner != null) owner.removeInternal(task);
            task.scene = null;
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
        if (androidRuntime) parent.unregisterMethod("touchEvent", this);
        parent.unregisterMethod("dispose", this);

        RuntimeException firstFailure = null;
        IdentityHashMap<Task, Boolean> seen = new IdentityHashMap<Task, Boolean>();
        ArrayList<Task> allTasks;
        synchronized (taskLock) {
            allTasks = new ArrayList<Task>(tasks);
            allTasks.addAll(pendingTasks);
        }
        for (Task task : allTasks) {
            if (seen.put(task, Boolean.TRUE) != null) continue;
            try {
                destroyTask(task);
            } catch (RuntimeException failure) {
                if (firstFailure == null) firstFailure = failure;
                else firstFailure.addSuppressed(failure);
            }
        }

        synchronized (taskLock) {
            tasks.clear();
            pendingTasks.clear();
            drawableTasks.clear();
        }
        for (Scene scene : new ArrayList<Scene>(scenes)) scene.clearInternal();
        scenes.clear();
        activeScene = null;
        ArrayList<Sound> loadedSounds = new ArrayList<Sound>(sounds);
        sounds.clear();
        for (Sound sound : loadedSounds) sound.disposeFromCore();
        clearInput();
        caller = null;
        updating = false;
        lastTaskId = 0;
        audioPlatform = null;
        audioPlatformError = null;
        fadeAmount = 0.0f;
        fadeStartAmount = 0.0f;
        fadeTargetAmount = 0.0f;
        fadeDuration = 0;
        fading = false;
        synchronized (Core.class) {
            if (instance == this) instance = null;
        }
        if (firstFailure != null) throw firstFailure;
    }
}
