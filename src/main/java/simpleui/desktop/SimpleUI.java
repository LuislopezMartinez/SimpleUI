/*
 * SimpleUI — designed and developed by Luis lopez martinez.
 * Copyright (c) 2026 Luis lopez martinez. Licensed under the MIT License.
 * SPDX-License-Identifier: MIT
 */
package simpleui.desktop;

import java.util.*;
import java.lang.reflect.*;
import java.util.concurrent.*;
import processing.core.*;
import processing.event.*;
import simplecore.Viewport;
import simplecore.ViewportMode;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;

public final class SimpleUI {
public static final String LIBRARY_AUTHOR = "Luis lopez martinez";
public static final String LIBRARY_LICENSE = "MIT";

    private static PApplet app;
    private static EventBridge eventBridge;
    private static boolean automaticEventHandling = true;
    private static KeyEventInterceptor keyEventInterceptor;
    private static WindowsResizeGuard windowsResizeGuard;

    private SimpleUI() {}

    public static void attach(PApplet host) {
        if (host == null) throw new IllegalArgumentException("SimpleUI requires a PApplet host");
        if (app != null && app != host) detach();
        app = host;
        Viewport.attach(host);
        syncHostState();
        installEventBridge();
        installWindowsResizeGuard();
    }

    public static void detach() {
        uninstallWindowsResizeGuard();
        uninstallEventBridge();
        app = null;
    }

    /**
     * Reports whether the automatic Windows/Java2D resize guard is active.
     * The guard installs itself from {@link #attach(PApplet)} and
     * {@link #initUI(PApplet, String, int)} when the host uses PSurfaceAWT.
     */
    public static boolean isWindowsResizeGuardActive() {
        return windowsResizeGuard != null;
    }

    private static void installWindowsResizeGuard() {
        if (app == null || windowsResizeGuard != null) return;
        WindowsResizeGuard candidate = WindowsResizeGuard.create(app);
        if (candidate != null) {
            windowsResizeGuard = candidate;
            candidate.install();
        }
    }

    private static void uninstallWindowsResizeGuard() {
        if (windowsResizeGuard == null) return;
        WindowsResizeGuard guard = windowsResizeGuard;
        windowsResizeGuard = null;
        guard.uninstall();
    }

    public static void setAutomaticEventHandling(boolean enabled) {
        automaticEventHandling = enabled;
        if (enabled) installEventBridge();
        else uninstallEventBridge();
    }

    public static boolean isAutomaticEventHandlingEnabled() { return automaticEventHandling; }
    public static boolean isEventBridgeInstalled() { return eventBridge != null; }

    public interface KeyEventInterceptor {
        boolean interceptKeyEvent(int action, char key, int keyCode);
    }

    public static void setKeyEventInterceptor(KeyEventInterceptor interceptor) {
        keyEventInterceptor = interceptor;
    }

    private static void installEventBridge() {
        if (!automaticEventHandling || app == null || eventBridge != null) return;
        eventBridge = new EventBridge();
        app.registerMethod("mouseEvent", eventBridge);
        app.registerMethod("keyEvent", eventBridge);
        app.registerMethod("dispose", eventBridge);
    }

    private static void uninstallEventBridge() {
        if (app == null || eventBridge == null) return;
        app.unregisterMethod("mouseEvent", eventBridge);
        app.unregisterMethod("keyEvent", eventBridge);
        app.unregisterMethod("dispose", eventBridge);
        eventBridge = null;
    }

    private static final class WindowsResizeGuard {
        private static final int SETTLE_DELAY_MS = 180;
        private static final int FRAME_ICONIFIED = 1;
        private static final String BUFFER_ERROR =
            "Buffers have not been created";

        private final PSurface surface;
        private final Object canvas;
        private final Object window;
        private final ScheduledExecutorService settleExecutor;
        private final Thread.UncaughtExceptionHandler previousExceptionHandler;
        private final Thread.UncaughtExceptionHandler exceptionHandler;
        private final Object componentListener;
        private final Object windowListener;
        private ScheduledFuture<?> settleTask;

        private volatile boolean recoveryDispatchPending;
        private boolean recoveryPending;
        private boolean closing;

        private WindowsResizeGuard(
            PSurface surface,
            Object canvas,
            Object window
        ) {
            this.surface = surface;
            this.canvas = canvas;
            this.window = window;
            previousExceptionHandler =
                Thread.getDefaultUncaughtExceptionHandler();

            settleExecutor = Executors.newSingleThreadScheduledExecutor();

            try {
                ClassLoader loader = SimpleUI.class.getClassLoader();
                Class<?> componentType = Class.forName("java.awt.event.ComponentListener", false, loader);
                componentListener = Proxy.newProxyInstance(loader, new Class<?>[]{componentType},
                    new ComponentInvocationHandler(this));

                Class<?> stateType = Class.forName("java.awt.event.WindowStateListener", false, loader);
                Class<?> windowType = Class.forName("java.awt.event.WindowListener", false, loader);
                windowListener = Proxy.newProxyInstance(loader, new Class<?>[]{stateType, windowType},
                    new WindowInvocationHandler(this));
            } catch (ReflectiveOperationException failure) {
                settleExecutor.shutdownNow();
                throw new IllegalStateException("Cannot initialize the Windows resize guard", failure);
            }

            exceptionHandler = new ResizeExceptionHandler(this);
        }

        static WindowsResizeGuard create(PApplet host) {
            String osName = System.getProperty("os.name", "");
            if (!osName.toLowerCase(Locale.ROOT).contains("windows")) {
                return null;
            }

            PSurface surface = host.getSurface();
            if (surface == null || !"processing.awt.PSurfaceAWT".equals(surface.getClass().getName())) return null;
            Object nativeSurface = surface.getNative();
            if (nativeSurface == null || !"processing.awt.PSurfaceAWT$SmoothCanvas".equals(nativeSurface.getClass().getName())) return null;
            try {
                Object frame = nativeSurface.getClass().getMethod("getFrame").invoke(nativeSurface);
                if (frame == null || !Class.forName("javax.swing.JFrame").isInstance(frame)) return null;
                return new WindowsResizeGuard(surface, nativeSurface, frame);
            } catch (ReflectiveOperationException | LinkageError failure) {
                return null;
            }
        }

        void install() {
            invokeListener(canvas, "addComponentListener", "java.awt.event.ComponentListener", componentListener);
            invokeListener(window, "addWindowStateListener", "java.awt.event.WindowStateListener", windowListener);
            invokeListener(window, "addWindowListener", "java.awt.event.WindowListener", windowListener);
            Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        }

        void uninstall() {
            closing = true;
            cancelSettleTask();
            settleExecutor.shutdownNow();
            invokeListener(canvas, "removeComponentListener", "java.awt.event.ComponentListener", componentListener);
            invokeListener(window, "removeWindowStateListener", "java.awt.event.WindowStateListener", windowListener);
            invokeListener(window, "removeWindowListener", "java.awt.event.WindowListener", windowListener);
            if (
                Thread.getDefaultUncaughtExceptionHandler() ==
                exceptionHandler
            ) {
                Thread.setDefaultUncaughtExceptionHandler(
                    previousExceptionHandler
                );
            }
        }

        private void beginWindowTransition() {
            if (closing) return;
            pauseRendering();
            scheduleTransitionFinish();
        }

        private void pauseRendering() {
            if (closing) return;
            surface.pauseThread();
        }

        private void scheduleTransitionFinish() {
            if (closing) return;
            cancelSettleTask();
            settleTask = settleExecutor.schedule(new SettleDispatch(this),
                SETTLE_DELAY_MS, TimeUnit.MILLISECONDS);
        }

        private void finishWindowTransition() {
            if (closing) return;
            if ((intResult(window, "getExtendedState", 0) & FRAME_ICONIFIED) != 0) return;
            if (!booleanResult(canvas, "isDisplayable", false)) return;

            if (recoveryPending || surface.isStopped()) {
                surface.stopThread();
                surface.startThread();
            } else {
                surface.resumeThread();
            }
            recoveryPending = false;
            recoveryDispatchPending = false;
        }

        private void scheduleRenderRecovery() {
            if (recoveryDispatchPending) return;
            recoveryDispatchPending = true;
            dispatchToAwt(new RecoveryDispatch(this));
        }

        private static final class ComponentInvocationHandler implements InvocationHandler {
            private final WindowsResizeGuard guard;
            ComponentInvocationHandler(WindowsResizeGuard guard) { this.guard = guard; }
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                if ("componentResized".equals(name)) guard.beginWindowTransition();
                else if ("componentHidden".equals(name)) guard.pauseRendering();
                else if ("componentShown".equals(name)) guard.scheduleTransitionFinish();
                return null;
            }
        }

        private static final class WindowInvocationHandler implements InvocationHandler {
            private final WindowsResizeGuard guard;
            WindowInvocationHandler(WindowsResizeGuard guard) { this.guard = guard; }
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                if ("windowStateChanged".equals(name) && args != null && args.length > 0) {
                    int state = intResult(args[0], "getNewState", 0);
                    if ((state & FRAME_ICONIFIED) != 0) guard.pauseRendering();
                    else guard.beginWindowTransition();
                } else if ("windowClosing".equals(name)) {
                    guard.closing = true;
                    guard.cancelSettleTask();
                }
                return null;
            }
        }

        private static final class ResizeExceptionHandler implements Thread.UncaughtExceptionHandler {
            private final WindowsResizeGuard guard;
            ResizeExceptionHandler(WindowsResizeGuard guard) { this.guard = guard; }
            public void uncaughtException(Thread thread, Throwable error) {
                if (guard.isRecoverableBufferError(thread, error) &&
                    SimpleUI.windowsResizeGuard == guard) {
                    guard.scheduleRenderRecovery();
                    return;
                }
                guard.forwardException(thread, error);
            }
        }

        private static final class SettleDispatch implements Runnable {
            private final WindowsResizeGuard guard;
            SettleDispatch(WindowsResizeGuard guard) { this.guard = guard; }
            public void run() { dispatchToAwt(new FinishDispatch(guard)); }
        }

        private static final class FinishDispatch implements Runnable {
            private final WindowsResizeGuard guard;
            FinishDispatch(WindowsResizeGuard guard) { this.guard = guard; }
            public void run() { guard.finishWindowTransition(); }
        }

        private static final class RecoveryDispatch implements Runnable {
            private final WindowsResizeGuard guard;
            RecoveryDispatch(WindowsResizeGuard guard) { this.guard = guard; }
            public void run() {
                if (guard.closing) {
                    guard.recoveryDispatchPending = false;
                    return;
                }
                guard.recoveryPending = true;
                guard.surface.stopThread();
                guard.scheduleTransitionFinish();
            }
        }

        private void cancelSettleTask() {
            if (settleTask != null) {
                settleTask.cancel(false);
                settleTask = null;
            }
        }

        private static void dispatchToAwt(Runnable action) {
            try {
                Class<?> eventQueue = Class.forName("java.awt.EventQueue");
                eventQueue.getMethod("invokeLater", Runnable.class).invoke(null, action);
            } catch (ReflectiveOperationException | LinkageError failure) {
                action.run();
            }
        }

        private static void invokeListener(Object target, String methodName, String typeName, Object listener) {
            try {
                Class<?> type = Class.forName(typeName);
                target.getClass().getMethod(methodName, type).invoke(target, listener);
            } catch (ReflectiveOperationException | LinkageError failure) {
                // The guard is optional; unavailable Desktop integration must not affect the sketch.
            }
        }

        private static int intResult(Object target, String methodName, int fallback) {
            try {
                Object result = target.getClass().getMethod(methodName).invoke(target);
                return result instanceof Number ? ((Number)result).intValue() : fallback;
            } catch (ReflectiveOperationException | LinkageError failure) {
                return fallback;
            }
        }

        private static boolean booleanResult(Object target, String methodName, boolean fallback) {
            try {
                Object result = target.getClass().getMethod(methodName).invoke(target);
                return result instanceof Boolean ? ((Boolean)result).booleanValue() : fallback;
            } catch (ReflectiveOperationException | LinkageError failure) {
                return fallback;
            }
        }

        private boolean isRecoverableBufferError(
            Thread thread,
            Throwable error
        ) {
            if (
                thread == null ||
                !"Animation Thread".equals(thread.getName())
            ) return false;

            Throwable current = error;
            while (current != null) {
                if (
                    current instanceof IllegalStateException &&
                    BUFFER_ERROR.equals(current.getMessage())
                ) return true;
                current = current.getCause();
            }
            return false;
        }

        private void forwardException(Thread thread, Throwable error) {
            if (previousExceptionHandler != null) {
                previousExceptionHandler.uncaughtException(thread, error);
            } else {
                error.printStackTrace();
            }
        }
    }

    public static final class EventBridge {
        private EventBridge() {}

        public void mouseEvent(MouseEvent event) {
            if (!automaticEventHandling || event == null) return;
            syncHostState();
            mouseX = event.getX();
            mouseY = event.getY();
            switch (event.getAction()) {
                case MouseEvent.PRESS: handleUIMousePressed(); break;
                case MouseEvent.DRAG: handleUIMouseDragged(); break;
                case MouseEvent.RELEASE: handleUIMouseReleased(); break;
                case MouseEvent.WHEEL: handleUIMouseWheel(event.getCount()); break;
                default: break;
            }
        }

        public void keyEvent(KeyEvent event) {
            if (!automaticEventHandling || event == null) return;
            syncHostState();
            key = event.getKey();
            keyCode = event.getKeyCode();
            if (keyEventInterceptor != null &&
                keyEventInterceptor.interceptKeyEvent(event.getAction(), key, keyCode)) return;
            switch (event.getAction()) {
                case KeyEvent.PRESS: handleUIKeyPressed(); break;
                case KeyEvent.TYPE: handleUIKeyTyped(); break;
                default: break;
            }
        }

        public void dispose() { detach(); }
    }

    public static PApplet host() {
        requireHost();
        return app;
    }

    public static void initUI(PApplet host, String fontName, int baseFontSize) {
        attach(host);
        initUI(fontName, baseFontSize);
    }

    public static void syncHostState() {
        requireHost();
        width = app.width;
        height = app.height;
        mouseX = app.mouseX;
        mouseY = app.mouseY;
        pmouseX = app.pmouseX;
        pmouseY = app.pmouseY;
        key = app.key;
        keyCode = app.keyCode;
        mousePressed = app.mousePressed;
        updateViewport();
    }

    private static void requireHost() {
        if (app == null) throw new IllegalStateException("Call SimpleUI.attach(this) or initUI(this, ...) first");
    }

    public static int width, height, mouseX, mouseY, pmouseX, pmouseY, keyCode;
    public static boolean mousePressed;
    public static char key;

    public static void pushMatrix() { app.pushMatrix(); }
    public static void popMatrix() { app.popMatrix(); }
    public static void pushStyle() { app.pushStyle(); }
    public static void popStyle() { app.popStyle(); }
    public static void translate(float x, float y) { app.translate(x, y); }
    public static void scale(float value) { app.scale(value); }
    public static void stroke(int value) { app.stroke(value); }
    public static void stroke(int value, float alpha) { app.stroke(value, alpha); }
    public static void stroke(int r, int g, int b) { app.stroke(r, g, b); }
    public static void strokeWeight(float value) { app.strokeWeight(value); }
    public static void noStroke() { app.noStroke(); }
    public static void fill(int value) { app.fill(value); }
    public static void fill(int value, float alpha) { app.fill(value, alpha); }
    public static void fill(float gray) { app.fill(gray); }
    public static void fill(float r, float g, float b) { app.fill(r, g, b); }
    public static void fill(float r, float g, float b, float a) { app.fill(r, g, b, a); }
    public static void noFill() { app.noFill(); }
    public static void rect(float a, float b, float c, float d) { app.rect(a, b, c, d); }
    public static void rect(float a, float b, float c, float d, float r) { app.rect(a, b, c, d, r); }
    public static void rect(float a, float b, float c, float d, float tl, float tr, float br, float bl) { app.rect(a, b, c, d, tl, tr, br, bl); }
    public static void ellipse(float a, float b, float c, float d) { app.ellipse(a, b, c, d); }
    public static void line(float a, float b, float c, float d) { app.line(a, b, c, d); }
    public static void text(String value, float x, float y) { app.text(value, x, y); }
    public static void text(String value, float x, float y, float w, float h) { app.text(value, x, y, w, h); }
    public static void text(char value, float x, float y) { app.text(value, x, y); }
    public static void text(int value, float x, float y) { app.text(value, x, y); }
    public static void textAlign(int horizontal) { app.textAlign(horizontal); }
    public static void textAlign(int horizontal, int vertical) { app.textAlign(horizontal, vertical); }
    public static void textSize(float size) { app.textSize(size); }
    public static void textFont(PFont font) { app.textFont(font); }
    public static PFont createFont(String name, float size) { return app.createFont(name, size); }
    public static PFont createFont(String name, float size, boolean smooth) { return app.createFont(name, size, smooth); }
    public static float textWidth(String value) { return app.textWidth(value); }
    public static void imageMode(int mode) { app.imageMode(mode); }
    public static void image(PImage image, float a, float b, float c, float d) { app.image(image, a, b, c, d); }
    public static void tint(float gray, float alpha) { app.tint(gray, alpha); }
    public static void noTint() { app.noTint(); }
    public static void clip(float a, float b, float c, float d) { app.clip(a, b, c, d); }
    public static void noClip() { app.noClip(); }
    public static int color(float gray) { return app.color(gray); }
    public static int color(float r, float g, float b) { return app.color(r, g, b); }
    public static int color(float r, float g, float b, float a) { return app.color(r, g, b, a); }
    public static int lerpColor(int a, int b, float amount) { return app.lerpColor(a, b, amount); }
    public static float alpha(int value) { return app.alpha(value); }
    public static float red(int value) { return app.red(value); }
    public static float green(int value) { return app.green(value); }
    public static float blue(int value) { return app.blue(value); }
    public static int millis() { return app.millis(); }

    public interface ModalResultHandler {
        void onModalResult(String actionId, boolean confirmed);
    }
    private static ModalResultHandler modalResultHandler;
    public static void setModalResultHandler(ModalResultHandler handler) { modalResultHandler = handler; }
    public static void handleModalResult(String actionId, boolean confirmed) {
        if (modalResultHandler != null) modalResultHandler.onModalResult(actionId, confirmed);
    }

// Desktop-adapted SimpleUI extracted from the Android sketch.
// Keeps the same spirit and component names, but removes Android-only
// keyboard bridge and mobile-specific dependencies.

public static final float SCROLL_LERP_SPEED = 0.15f;
public static final float SCROLL_DEAD_ZONE = 10.0f;
public static final float ROW_HEIGHT_MULTIPLIER = 2.5f;
public static final float SLIDER_HANDLE_RADIUS_RATIO = 0.8f;
public static final int BORDER_RADIUS_SMALL = 4;
public static final int BORDER_RADIUS_MEDIUM = 8;
public static final int BORDER_RADIUS_LARGE = 10;
public static final int MODAL_WIDTH = 340;
public static final int MODAL_HEIGHT = 180;
public static final int MODAL_BUTTON_WIDTH = 120;
public static final int MODAL_BUTTON_HEIGHT = 42;
public static final int MODAL_BUTTON_GAP = 16;


public static UIEventHandler globalHandler = null;
public static ArrayList<UIViewManager> uiViewManagers = new ArrayList<UIViewManager>();
public static UIModal activeUIModal = null;

public static void setUIEventHandler(UIEventHandler handler) {
  globalHandler = handler;
}

public static void triggerEvent(UIElement element, String action, Object data) {
  if (activeUIModal != null && activeUIModal.isVisible() && activeUIModal.ownsControl(element)) {
    activeUIModal.onUIEvent(element, action, data);
    return;
  }
  for (int i = uiViewManagers.size() - 1; i >= 0; i--) {
    if (uiViewManagers.get(i).routeEvent(element, action, data)) return;
  }
  if (globalHandler != null) {
    globalHandler.onUIEvent(element, action, data);
  }
}










public static ArrayList<UIElement> uiElements = new ArrayList<UIElement>();
public static PFont uiFont;
public static float uiScale = 1.0f;
public static float designWidth = 0;
public static float designHeight = 0;
public static float logicalWidth = 0;
public static float logicalHeight = 0;
public static float viewportOffsetX = 0;
public static float viewportOffsetY = 0;
public static boolean modeConfigured = false;
public static UIScaleMode scaleMode = UIScaleMode.FIT;
public static UIConfig currentTheme;
public static UIScrollState scrollState = new UIScrollState();
public static UIGestureState gestureState = new UIGestureState();
public static UIDropdown activeDropdown = null;
public static UITextInputBase activeTextField = null;
public static boolean uiModalVisible = false;
public static String uiModalTitle = "";
public static String uiModalMessage = "";
public static String uiModalButtonLabel = "OK";
public static boolean uiModalIsConfirm = false;
public static String uiModalCancelLabel = "Cancel";
public static String uiModalActionId = "";
public static Runnable uiModalConfirmAction = null;
public static Runnable uiModalCancelAction = null;

public static void initUI(String fontName, int baseFontSize) {
  uiFont = createFont(fontName, baseFontSize, true);
  textFont(uiFont);
  setTheme(UIColorTheme.LIGHT);
  setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
    }
  });
}

public static void setTheme(UIColorTheme theme) {
  if (theme == UIColorTheme.DARK) {
    currentTheme = new UIConfig(
      color(25),
      color(45),
      color(240),
      color(0, 140, 255),
      color(70),
      color(120)
      );
  } else {
    currentTheme = new UIConfig(
      color(245),
      color(255),
      color(30),
      color(0, 100, 220),
      color(210),
      color(170)
      );
  }
}

public static void setMode(float width, float height) {
  setMode(width, height, UIScaleMode.FIT);
}

public static void setMode(
  float width,
  float height,
  UIScaleMode mode
) {
  if (width <= 0 || height <= 0) {
    throw new IllegalArgumentException("SimpleUI mode dimensions must be positive");
  }
  designWidth = width;
  designHeight = height;
  scaleMode = mode == null ? UIScaleMode.FIT : mode;
  modeConfigured = true;
  Viewport.setMode(width, height, ViewportMode.valueOf(scaleMode.name()));
  updateViewport();
}

public static void updateViewport() {
  Viewport.update();
  modeConfigured = Viewport.isConfigured();
  designWidth = Viewport.getDesignWidth();
  designHeight = Viewport.getDesignHeight();
  logicalWidth = Viewport.getLogicalWidth();
  logicalHeight = Viewport.getLogicalHeight();
  uiScale = Viewport.getScaleX();
  viewportOffsetX = Viewport.getOffsetX();
  viewportOffsetY = Viewport.getOffsetY();
  scaleMode = UIScaleMode.valueOf(Viewport.getMode().name());
}

public static float getLogicalWidth() {
  return modeConfigured ? logicalWidth : width / uiScale;
}

public static float getLogicalHeight() {
  return modeConfigured ? logicalHeight : height / uiScale;
}

public static float screenToDesignX(float screenX) {
  return Viewport.screenToDesignX(screenX);
}

public static float screenToDesignY(float screenY) {
  return Viewport.screenToDesignY(screenY);
}

public static float designToScreenX(float designX) {
  return Viewport.designToScreenX(designX);
}

public static float designToScreenY(float designY) {
  return Viewport.designToScreenY(designY);
}

public static void addUIElement(UIElement element) {
  uiElements.add(element);
}

public static UIElement getUIElement(String elementId) {
  for (UIElement e : uiElements) {
    if (e.id.equals(elementId)) return e;
  }
  return null;
}

public static boolean removeUIElement(String elementId) {
  for (int i = uiElements.size() - 1; i >= 0; i--) {
    if (uiElements.get(i).id.equals(elementId)) {
      uiElements.remove(i);
      return true;
    }
  }
  return false;
}

public static void clearUIElements() {
  uiElements.clear();
}

public static void updateUI() {
    syncHostState();
  scrollState.update();
  for (int i = 0; i < uiViewManagers.size(); i++) uiViewManagers.get(i).update();
}

public static void drawUI() {
    syncHostState();
  pushMatrix();
  translate(viewportOffsetX, viewportOffsetY + scrollState.currentY);
  scale(uiScale);

  for (UIElement e : uiElements) {
    if (!e.isAnchored() && !(e instanceof UIDropdown)) {
      e.draw();
    }
  }
  for (UIElement e : uiElements) {
    if (!e.isAnchored() && e instanceof UIDropdown) {
      UIDropdown d = (UIDropdown)e;
      if (d != activeDropdown) d.draw();
    }
  }
  if (activeDropdown != null && !activeDropdown.isAnchored()) {
    activeDropdown.draw();
  }
  popMatrix();

  pushMatrix();
  translate(viewportOffsetX, viewportOffsetY);
  scale(uiScale);
  for (UIElement e : uiElements) {
    if (e.isAnchored() && !(e instanceof UIDropdown)) {
      e.draw();
    }
  }
  for (UIElement e : uiElements) {
    if (e.isAnchored() && e instanceof UIDropdown) {
      UIDropdown d = (UIDropdown)e;
      if (d != activeDropdown) d.draw();
    }
  }
  if (activeDropdown != null && activeDropdown.isAnchored()) {
    activeDropdown.draw();
  }
  popMatrix();

  if (activeDropdown != null && !activeDropdown.isOpen()) {
    activeDropdown = null;
  }

  drawModalOverlay();
  if (activeUIModal != null) activeUIModal.draw();
}

public static void showAlertModal(String title, String message, String buttonLabel) {
  if (activeUIModal != null) activeUIModal.hide();
  uiModalTitle = title == null ? "" : title;
  uiModalMessage = message == null ? "" : message;
  uiModalButtonLabel = buttonLabel == null || buttonLabel.length() == 0 ? "OK" : buttonLabel;
  uiModalCancelLabel = "Cancel";
  uiModalActionId = "";
  uiModalConfirmAction = null;
  uiModalCancelAction = null;
  uiModalIsConfirm = false;
  uiModalVisible = true;
  if (activeTextField != null) {
    activeTextField.setFocused(false);
    activeTextField = null;
  }
  closeAllDropdownsExcept(null);
}

public static void showConfirmModal(String title, String message, String confirmLabel, String cancelLabel, String actionId) {
  if (activeUIModal != null) activeUIModal.hide();
  uiModalTitle = title == null ? "" : title;
  uiModalMessage = message == null ? "" : message;
  uiModalButtonLabel = confirmLabel == null || confirmLabel.length() == 0 ? "OK" : confirmLabel;
  uiModalCancelLabel = cancelLabel == null || cancelLabel.length() == 0 ? "Cancel" : cancelLabel;
  uiModalActionId = actionId == null ? "" : actionId;
  uiModalConfirmAction = null;
  uiModalCancelAction = null;
  uiModalIsConfirm = true;
  uiModalVisible = true;
  if (activeTextField != null) {
    activeTextField.setFocused(false);
    activeTextField = null;
  }
  closeAllDropdownsExcept(null);
}

public static void showConfirmModal(String title, String message, String confirmLabel, String cancelLabel, Runnable confirmAction, Runnable cancelAction) {
  showConfirmModal(title, message, confirmLabel, cancelLabel, "");
  uiModalConfirmAction = confirmAction;
  uiModalCancelAction = cancelAction;
}

public static void hideAlertModal() {
  uiModalVisible = false;
  uiModalIsConfirm = false;
  uiModalActionId = "";
  uiModalConfirmAction = null;
  uiModalCancelAction = null;
}

public static boolean isAlertModalVisible() {
  return uiModalVisible;
}

public static boolean isAnyModalVisible() {
  return uiModalVisible || (activeUIModal != null && activeUIModal.isVisible());
}

public static void drawModalOverlay() {
  if (!uiModalVisible) return;

  float designWidth = getLogicalWidth();
  float designHeight = getLogicalHeight();
  float modalX = (designWidth - MODAL_WIDTH) * 0.5f;
  float modalY = (designHeight - MODAL_HEIGHT) * 0.5f;
  float buttonX = modalX + (MODAL_WIDTH - MODAL_BUTTON_WIDTH) * 0.5f;
  float buttonY = modalY + MODAL_HEIGHT - MODAL_BUTTON_HEIGHT - 18;
  float confirmButtonX = modalX + (MODAL_WIDTH - (MODAL_BUTTON_WIDTH * 2 + MODAL_BUTTON_GAP)) * 0.5f + MODAL_BUTTON_WIDTH + MODAL_BUTTON_GAP;
  float cancelButtonX = modalX + (MODAL_WIDTH - (MODAL_BUTTON_WIDTH * 2 + MODAL_BUTTON_GAP)) * 0.5f;

  pushStyle();
  pushMatrix();
  translate(viewportOffsetX, viewportOffsetY);
  scale(uiScale);
  noStroke();
  fill(0, 150);
  rect(0, 0, designWidth, designHeight);

  stroke(currentTheme.borderColor);
  strokeWeight(2);
  fill(currentTheme.surfaceColor);
  rect(modalX, modalY, MODAL_WIDTH, MODAL_HEIGHT, BORDER_RADIUS_LARGE);

  fill(currentTheme.textColor);
  textAlign(LEFT, TOP);
  textSize(18);
  text(uiModalTitle, modalX + 18, modalY + 16, MODAL_WIDTH - 36, 26);

  textSize(15);
  text(uiModalMessage, modalX + 18, modalY + 54, MODAL_WIDTH - 36, 54);

  textAlign(CENTER, CENTER);
  textSize(15);
  if (uiModalIsConfirm) {
    fill(currentTheme.surfaceColor);
    stroke(currentTheme.borderColor);
    rect(cancelButtonX, buttonY, MODAL_BUTTON_WIDTH, MODAL_BUTTON_HEIGHT, BORDER_RADIUS_MEDIUM);
    fill(currentTheme.textColor);
    text(uiModalCancelLabel, cancelButtonX + MODAL_BUTTON_WIDTH * 0.5f, buttonY + MODAL_BUTTON_HEIGHT * 0.5f);

    fill(currentTheme.accentColor);
    stroke(currentTheme.accentColor);
    rect(confirmButtonX, buttonY, MODAL_BUTTON_WIDTH, MODAL_BUTTON_HEIGHT, BORDER_RADIUS_MEDIUM);
    fill(255);
    text(uiModalButtonLabel, confirmButtonX + MODAL_BUTTON_WIDTH * 0.5f, buttonY + MODAL_BUTTON_HEIGHT * 0.5f);
  } else {
    fill(currentTheme.accentColor);
    stroke(currentTheme.accentColor);
    rect(buttonX, buttonY, MODAL_BUTTON_WIDTH, MODAL_BUTTON_HEIGHT, BORDER_RADIUS_MEDIUM);
    fill(255);
    text(uiModalButtonLabel, buttonX + MODAL_BUTTON_WIDTH * 0.5f, buttonY + MODAL_BUTTON_HEIGHT * 0.5f);
  }
  popMatrix();
  popStyle();
}

public static boolean isPointInModalButton(float mx, float my) {
  float designWidth = getLogicalWidth();
  float designHeight = getLogicalHeight();
  float modalX = (designWidth - MODAL_WIDTH) * 0.5f;
  float modalY = (designHeight - MODAL_HEIGHT) * 0.5f;
  float buttonX = modalX + (MODAL_WIDTH - MODAL_BUTTON_WIDTH) * 0.5f;
  float buttonY = modalY + MODAL_HEIGHT - MODAL_BUTTON_HEIGHT - 18;
  return mx >= buttonX && mx <= buttonX + MODAL_BUTTON_WIDTH &&
    my >= buttonY && my <= buttonY + MODAL_BUTTON_HEIGHT;
}

public static boolean isPointInModalConfirmButton(float mx, float my) {
  float designWidth = getLogicalWidth();
  float designHeight = getLogicalHeight();
  float modalX = (designWidth - MODAL_WIDTH) * 0.5f;
  float modalY = (designHeight - MODAL_HEIGHT) * 0.5f;
  float buttonY = modalY + MODAL_HEIGHT - MODAL_BUTTON_HEIGHT - 18;
  float confirmButtonX = modalX + (MODAL_WIDTH - (MODAL_BUTTON_WIDTH * 2 + MODAL_BUTTON_GAP)) * 0.5f + MODAL_BUTTON_WIDTH + MODAL_BUTTON_GAP;
  return mx >= confirmButtonX && mx <= confirmButtonX + MODAL_BUTTON_WIDTH &&
    my >= buttonY && my <= buttonY + MODAL_BUTTON_HEIGHT;
}

public static boolean isPointInModalCancelButton(float mx, float my) {
  float designWidth = getLogicalWidth();
  float designHeight = getLogicalHeight();
  float modalX = (designWidth - MODAL_WIDTH) * 0.5f;
  float modalY = (designHeight - MODAL_HEIGHT) * 0.5f;
  float buttonY = modalY + MODAL_HEIGHT - MODAL_BUTTON_HEIGHT - 18;
  float cancelButtonX = modalX + (MODAL_WIDTH - (MODAL_BUTTON_WIDTH * 2 + MODAL_BUTTON_GAP)) * 0.5f;
  return mx >= cancelButtonX && mx <= cancelButtonX + MODAL_BUTTON_WIDTH &&
    my >= buttonY && my <= buttonY + MODAL_BUTTON_HEIGHT;
}

public static void updateAndDrawUI() {
  updateUI();
  drawUI();
}

public static void closeAllDropdownsExcept(UIDropdown keep) {
  for (UIElement e : uiElements) {
    if (e instanceof UIDropdown) {
      UIDropdown d = (UIDropdown)e;
      if (d != keep) d.setOpen(false);
    }
  }
  activeDropdown = keep;
}

public static UIDropdown findTopMostDropdownAt(float mx, float my) {
  if (activeDropdown != null && activeDropdown.isVisible && activeDropdown.isEnabled && activeDropdown.isOpen()) {
    if (activeDropdown.containsMainBox(mx, my) || activeDropdown.containsOpenMenu(mx, my)) {
      return activeDropdown;
    }
  }

  for (int i = uiElements.size() - 1; i >= 0; i--) {
    UIElement e = uiElements.get(i);
    if (e instanceof UIDropdown && e.isVisible && e.isEnabled) {
      UIDropdown d = (UIDropdown)e;
      if (d.containsMainBox(mx, my) || d.containsOpenMenu(mx, my)) {
        return d;
      }
    }
  }
  return null;
}

public static UITextInputBase findTopMostTextFieldAt(float mx, float my) {
  for (int i = uiElements.size() - 1; i >= 0; i--) {
    UIElement e = uiElements.get(i);
    if (e instanceof UITextInputBase && e.isVisible && e.isEnabled) {
      UITextInputBase t = (UITextInputBase)e;
      if (t.containsPoint(mx, my)) return t;
    }
  }
  return null;
}

public static boolean isPointInElement(float mx, float my, UIElement element) {
  return mx > element.x && mx < element.x + element.width &&
    my > element.y && my < element.y + element.height;
}

public static float getScaledMouseX() {
  return (mouseX - viewportOffsetX) / uiScale;
}

public static float getScaledMouseY() {
  return (mouseY - viewportOffsetY) / uiScale;
}

public static boolean passedGestureThreshold() {
  return dist(gestureState.startX, gestureState.startY, mouseX, mouseY) > SCROLL_DEAD_ZONE;
}

public static UIGestureAxis resolveGestureAxis(float dx, float dy) {
  float adx = abs(dx);
  float ady = abs(dy);
  if (adx > ady * 1.25f) return UIGestureAxis.AXIS_HORIZONTAL;
  if (ady > adx * 1.25f) return UIGestureAxis.AXIS_VERTICAL;
  return UIGestureAxis.AXIS_UNDECIDED;
}

public static UIElement findTopMostInteractiveElementAt(float mx, float myAnchored, float myScrolled) {
  UIDropdown hitDropdown = findTopMostDropdownAt(mx, myScrolled);
  UIDropdown hitDropdownAnchored = findTopMostDropdownAt(mx, myAnchored);
  if (hitDropdownAnchored != null && hitDropdownAnchored.isAnchored()) return hitDropdownAnchored;
  if (hitDropdown != null) return hitDropdown;

  for (int i = uiElements.size() - 1; i >= 0; i--) {
    UIElement e = uiElements.get(i);
    if (!e.isVisible || !e.isEnabled || e instanceof UILabel) continue;
    float localY = e.isAnchored() ? myAnchored : myScrolled;
    if (isPointInElement(mx, localY, e)) {
      return e;
    }
  }
  return null;
}

public static void handleUIMouseWheel(float count) {
  if (activeUIModal != null && activeUIModal.isVisible()) return;
  if (uiModalVisible) return;

  float mx = getScaledMouseX();
  float myAnchored = (mouseY - viewportOffsetY) / uiScale;
  float myScrolled =
    (mouseY - viewportOffsetY - scrollState.currentY) / uiScale;
  UIElement hit = findTopMostInteractiveElementAt(mx, myAnchored, myScrolled);
  if (!(hit instanceof UITable) || !hit.hasScrollableOverflow()) return;

  UITable table = (UITable)hit;
  float visibleHeight = table.height - table.rowHeight;
  float contentHeight = table.rows.size() * table.rowHeight;
  float minimumScroll = min(0, visibleHeight - contentHeight);
  table.internalScrollY = constrain(
    table.internalScrollY - count * table.rowHeight * 3,
    minimumScroll,
    0
  );
}

public static void resolveGestureTarget() {
  UIElement e = gestureState.pressedElement;
  gestureState.capturedElement = null;
  gestureState.target = UIGestureTarget.GESTURE_NONE;

  if (e == null) {
    gestureState.target = UIGestureTarget.GESTURE_GLOBAL_SCROLL;
    return;
  }

  if (e instanceof UIDropdown) {
    gestureState.target = UIGestureTarget.GESTURE_ELEMENT_ACTION;
    gestureState.capturedElement = e;
    return;
  }

  if (e instanceof UISlider) {
    if (gestureState.axis == UIGestureAxis.AXIS_HORIZONTAL) {
      gestureState.target = UIGestureTarget.GESTURE_ELEMENT_ACTION;
      gestureState.capturedElement = e;
      ((UISlider)e).beginGestureDrag();
    } else {
      gestureState.target = UIGestureTarget.GESTURE_GLOBAL_SCROLL;
    }
    return;
  }

  if (gestureState.axis != UIGestureAxis.AXIS_HORIZONTAL && e.hasScrollableOverflow()) {
    gestureState.target = UIGestureTarget.GESTURE_ELEMENT_SCROLL;
    gestureState.capturedElement = e;
    return;
  }

  gestureState.target = UIGestureTarget.GESTURE_GLOBAL_SCROLL;
}

public static void dispatchDragToCapturedElement() {
  if (gestureState.capturedElement == null) return;
  gestureState.capturedElement.mouseDragged();
}

public static void performTapAction(float mx, float myAnchored, float myScrolled) {
  UIElement e = gestureState.pressedElement;
  if (e == null || !e.isVisible || !e.isEnabled) return;

  float localY = e.isAnchored() ? myAnchored : myScrolled;

  if (e instanceof UIDropdown) {
    UIDropdown dropdown = (UIDropdown)e;
    if (!dropdown.containsMainBox(mx, localY) && !dropdown.containsOpenMenu(mx, localY)) return;
    closeAllDropdownsExcept(dropdown);
    dropdown.handleClickAt(mx, localY);
    if (dropdown.isOpen()) {
      activeDropdown = dropdown;
    } else if (activeDropdown == dropdown) {
      activeDropdown = null;
    }
    return;
  }

  if (!isPointInElement(mx, localY, e)) return;

  if (e instanceof UIButton) {
    ((UIButton)e).performTapAction();
    return;
  }

  if (e instanceof UITabs) {
    ((UITabs)e).performTapAction(mx, localY);
    return;
  }

  if (e instanceof UITextField) {
    ((UITextField)e).performTapAction(mx, localY);
    return;
  }

  if (e instanceof UITextArea) {
    ((UITextArea)e).performTapAction(mx, localY);
    return;
  }

  if (e instanceof UINumberField) {
    ((UINumberField)e).performTapAction();
    return;
  }

  e.performTapAction(mx, localY);
}

public static void cleanupCapturedGestureState() {
  if (gestureState.pressedElement instanceof UIButton) {
    ((UIButton)gestureState.pressedElement).setPressedVisual(false);
  }

  if (gestureState.capturedElement instanceof UISlider) {
    ((UISlider)gestureState.capturedElement).mouseReleased();
  }
}

public static void handleUIModalMousePressed() {
  activeUIModal.layout();
  gestureState.reset();
  gestureState.startX = mouseX;
  gestureState.startY = mouseY;
  gestureState.lastX = mouseX;
  gestureState.lastY = mouseY;
  gestureState.isTapCandidate = true;
  float mx = (mouseX - viewportOffsetX) / uiScale;
  float my = (mouseY - viewportOffsetY) / uiScale;
  UIElement hit = activeUIModal.findTopMostControlAt(mx, my);
  if (activeTextField != null && activeTextField != hit) {
    activeTextField.setFocused(false);
    activeTextField = null;
  }
  gestureState.pressedElement = hit;
  if (hit instanceof UIButton) ((UIButton)hit).setPressedVisual(true);
}

public static void handleUIModalMouseReleased() {
  activeUIModal.layout();
  float mx = (mouseX - viewportOffsetX) / uiScale;
  float my = (mouseY - viewportOffsetY) / uiScale;
  boolean isTap = dist(gestureState.startX, gestureState.startY, mouseX, mouseY) <= SCROLL_DEAD_ZONE;
  if (isTap && gestureState.pressedElement != null) {
    performTapAction(mx, my, my);
  } else if (isTap && activeUIModal.dismissOnOutsideTap && !activeUIModal.containsPoint(mx, my)) {
    activeUIModal.hide();
  }
  cleanupCapturedGestureState();
  gestureState.reset();
}

public static void handleUIMousePressed() {
    syncHostState();
  if (activeUIModal != null && activeUIModal.isVisible()) {
    handleUIModalMousePressed();
    return;
  }
  if (uiModalVisible) {
    scrollState.reset();
    gestureState.reset();
    return;
  }

  scrollState.reset();
  gestureState.reset();
  gestureState.startX = mouseX;
  gestureState.startY = mouseY;
  gestureState.lastX = mouseX;
  gestureState.lastY = mouseY;
  gestureState.isTapCandidate = true;

  float mx = getScaledMouseX();
  float myAnchored = (mouseY - viewportOffsetY) / uiScale;
  float my = (mouseY - viewportOffsetY - scrollState.currentY) / uiScale;

  UITextInputBase hitText = findTopMostTextFieldAt(mx, my);
  if (activeTextField != null && activeTextField != hitText) {
    activeTextField.setFocused(false);
    activeTextField = null;
  }

  UIDropdown hitDropdown = findTopMostDropdownAt(mx, my);
  UIDropdown hitDropdownAnchored = findTopMostDropdownAt(mx, myAnchored);
  if (hitDropdownAnchored != null && hitDropdownAnchored.isAnchored()) {
    hitDropdown = hitDropdownAnchored;
    my = myAnchored;
  }
  if (hitDropdown != null) {
    if (!hitDropdown.containsMainBox(mx, my) && !hitDropdown.containsOpenMenu(mx, my)) {
      return;
    }
    gestureState.pressedElement = hitDropdown;
    if (activeDropdown == null || activeDropdown != hitDropdown) {
      closeAllDropdownsExcept(hitDropdown);
    }
    return;
  } else if (activeDropdown != null && activeDropdown.isOpen()) {
    activeDropdown.setOpen(false);
    activeDropdown = null;
  }

  gestureState.pressedElement = findTopMostInteractiveElementAt(mx, myAnchored, my);
  if (gestureState.pressedElement instanceof UIButton) {
    ((UIButton)gestureState.pressedElement).setPressedVisual(true);
  }
}

public static void handleUIMouseDragged() {
    syncHostState();
  if (activeUIModal != null && activeUIModal.isVisible()) return;
  if (uiModalVisible) return;

  if (!gestureState.passedThreshold && passedGestureThreshold()) {
    gestureState.passedThreshold = true;
    gestureState.isTapCandidate = false;
    gestureState.axis = resolveGestureAxis(mouseX - gestureState.startX, mouseY - gestureState.startY);
    resolveGestureTarget();
  }

  if (!gestureState.passedThreshold) {
    return;
  }

  if (gestureState.target == UIGestureTarget.GESTURE_GLOBAL_SCROLL) {
    handleGlobalScroll();
  } else if (gestureState.target == UIGestureTarget.GESTURE_ELEMENT_SCROLL ||
    gestureState.target == UIGestureTarget.GESTURE_ELEMENT_ACTION) {
    dispatchDragToCapturedElement();
  }

  gestureState.lastX = mouseX;
  gestureState.lastY = mouseY;
}

public static void handleGlobalScroll() {
  scrollState.targetY += (mouseY - pmouseY);
  float maxContentBottom = calculateMaxContentBottom();
  float maxScrollUp = min(0, -(maxContentBottom - height + 100));
  scrollState.targetY = constrain(scrollState.targetY, maxScrollUp, 0);
}

public static float calculateMaxContentBottom() {
  float maxBottom = 0;
  for (UIElement e : uiElements) {
    float elementBottom = (e.y + e.height) * uiScale;
    if (elementBottom > maxBottom) {
      maxBottom = elementBottom;
    }
  }
  return maxBottom;
}

public static void handleUIMouseReleased() {
    syncHostState();
  if (activeUIModal != null && activeUIModal.isVisible()) {
    handleUIModalMouseReleased();
    return;
  }
  if (uiModalVisible) {
    float mx = getScaledMouseX();
    float my = getScaledMouseY();
    if (uiModalIsConfirm) {
      if (isPointInModalConfirmButton(mx, my)) {
        String actionId = uiModalActionId;
        Runnable action = uiModalConfirmAction;
        hideAlertModal();
        if (action != null) action.run();
        if (actionId.length() > 0) handleModalResult(actionId, true);
      } else if (isPointInModalCancelButton(mx, my)) {
        String actionId = uiModalActionId;
        Runnable action = uiModalCancelAction;
        hideAlertModal();
        if (action != null) action.run();
        if (actionId.length() > 0) handleModalResult(actionId, false);
      }
    } else if (isPointInModalButton(mx, my)) {
      hideAlertModal();
    }
    cleanupCapturedGestureState();
    scrollState.reset();
    gestureState.reset();
    return;
  }

  float mx = getScaledMouseX();
  float myAnchored = (mouseY - viewportOffsetY) / uiScale;
  float my = (mouseY - viewportOffsetY - scrollState.currentY) / uiScale;

  if (gestureState.isTapCandidate && !gestureState.passedThreshold) {
    performTapAction(mx, myAnchored, my);
  }

  cleanupCapturedGestureState();
  scrollState.reset();
  gestureState.reset();
}

public static void handleUIKeyPressed() {
    syncHostState();
  if (activeUIModal != null && activeUIModal.isVisible()) {
    for (UIElement element : activeUIModal.controls) element.keyPressed();
    return;
  }
  if (uiModalVisible) return;
  for (UIElement e : uiElements) {
    e.keyPressed();
  }
}

public static void handleUIKeyTyped() {
    syncHostState();
  if (activeUIModal != null && activeUIModal.isVisible()) {
    for (UIElement element : activeUIModal.controls) element.keyTyped();
    return;
  }
  if (uiModalVisible) return;
  for (UIElement e : uiElements) {
    e.keyTyped();
  }
}




// Geometría lógica reutilizable para que otros controles y Task visuales puedan
// seguir un elemento de SimpleUI sin repetir sus cálculos internos.


















public static void setUIAlpha(float alpha) {
}

public static void enableAllElements() {
  for (UIElement e : uiElements) e.setEnabled(true);
}

public static void disableAllElements() {
  for (UIElement e : uiElements) e.setEnabled(false);
}

public static boolean isAnyDropdownOpen() {
  for (UIElement e : uiElements) {
    if (e instanceof UIDropdown && ((UIDropdown)e).isOpen()) return true;
  }
  return false;
}

public static void cleanupUIStates() {
  for (UIElement e : uiElements) {
    if (e instanceof UISlider) {
      ((UISlider)e).forceRelease();
    } else if (e instanceof UIButton) {
      ((UIButton)e).mouseReleased();
    }
  }
}


/*
 * SIMPLE UI CALENDAR
 * ------------------
 * Control de calendario compartido por Android y desktop. Incluye fechas
 * gregorianas validadas, fecha/hora con zona configurable, intervalos,
 * comparadores, eventos y selección de días.
 */







public static final long UI_MILLIS_PER_SECOND = 1000L;
public static final long UI_MILLIS_PER_MINUTE = 60L * UI_MILLIS_PER_SECOND;
public static final long UI_MILLIS_PER_HOUR = 60L * UI_MILLIS_PER_MINUTE;
public static final long UI_MILLIS_PER_DAY = 24L * UI_MILLIS_PER_HOUR;

public static boolean isUILeapYear(int year) {
    return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
}

public static int getUIDaysInMonth(int year, int month) {
    if (month < 1 || month > 12) return 0;
    int[] days = { 31, isUILeapYear(year) ? 29 : 28, 31, 30, 31, 30,
        31, 31, 30, 31, 30, 31 };
    return days[month - 1];
}

public static boolean isValidUIDate(int year, int month, int day) {
    return year >= 1 && month >= 1 && month <= 12 &&
        day >= 1 && day <= getUIDaysInMonth(year, month);
}

public static Calendar createUICalendar(int year, int month, int day, int hour, int minute,
    int second, int millisecond, String timeZoneId) {
    TimeZone zone = TimeZone.getTimeZone(
        timeZoneId == null || timeZoneId.length() == 0 ?
        TimeZone.getDefault().getID() : timeZoneId
        );
    Calendar value = new GregorianCalendar(zone);
    value.setLenient(false);
    value.clear();
    value.set(Calendar.YEAR, year);
    value.set(Calendar.MONTH, month - 1);
    value.set(Calendar.DAY_OF_MONTH, day);
    value.set(Calendar.HOUR_OF_DAY, hour);
    value.set(Calendar.MINUTE, minute);
    value.set(Calendar.SECOND, second);
    value.set(Calendar.MILLISECOND, millisecond);
    value.getTimeInMillis();
    return value;
}



public static long absLong(long value) {
    return value == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(value);
}








public static UIDate getUIToday() {
    Calendar today = Calendar.getInstance();
    return new UIDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1,
        today.get(Calendar.DAY_OF_MONTH));
}
}
