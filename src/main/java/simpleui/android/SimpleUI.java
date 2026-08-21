/*
 * SimpleUI — designed and developed by Luis lopez martinez.
 * Copyright (c) 2026 Luis lopez martinez. Licensed under the MIT License.
 * SPDX-License-Identifier: MIT
 */
package simpleui.android;

import java.util.*;
import processing.core.*;
import processing.event.*;
import simplecore.Viewport;
import simplecore.ViewportMode;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.text.TextWatcher;
import android.text.Editable;
import android.graphics.Rect;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.os.Build;

public final class SimpleUI {
public static final String LIBRARY_AUTHOR = "Luis lopez martinez";
public static final String LIBRARY_LICENSE = "MIT";

    private static PApplet app;
    private static EventBridge eventBridge;
    private static boolean automaticEventHandling = true;
    private static KeyEventInterceptor keyEventInterceptor;

    private SimpleUI() {}

    public static void attach(PApplet host) {
        if (host == null) throw new IllegalArgumentException("SimpleUI requires a PApplet host");
        if (app != null && app != host) detach();
        app = host;
        Viewport.attach(host);
        syncHostState();
        installEventBridge();
    }

    public static void detach() {
        uninstallEventBridge();
        app = null;
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


    public static android.app.Activity getActivity() {
        requireHost();
        return app.getActivity();
    }















// =============================================
// CONSTANTES GLOBALES
// =============================================
public static final float SCROLL_LERP_SPEED = 0.15f;
public static final float SCROLL_DEAD_ZONE = 10.0f;
public static final float ROW_HEIGHT_MULTIPLIER = 2.5f;
public static final float DROPDOWN_ARROW_SIZE = 15;
public static final float SLIDER_HANDLE_RADIUS_RATIO = 0.8f;
public static final int BORDER_RADIUS_SMALL = 4;
public static final int BORDER_RADIUS_MEDIUM = 8;
public static final int BORDER_RADIUS_LARGE = 10;
public static final int INPUT_OVERLAY_HEIGHT = 52;
public static final float KEYBOARD_FIELD_MARGIN = 20;
public static final int MODAL_WIDTH = 340;
public static final int MODAL_HEIGHT = 180;
public static final int MODAL_BUTTON_WIDTH = 120;
public static final int MODAL_BUTTON_HEIGHT = 42;

// =============================================
// SISTEMA DE EVENTOS MEJORADO
// =============================================

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




// =============================================
// GESTI??N DE ESTADOS Y CONFIGURACI??N
// =============================================






// =============================================
// GESTOR PRINCIPAL DE UI
// =============================================
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
public static float uiInputYOffset = 0;
public static boolean showInputOverlay = true;
public static EditText nativeInputField = null;
public static boolean nativeInputReady = false;
public static boolean nativeInputInternalChange = false;
public static volatile boolean androidKeyboardVisible = false;
public static volatile int androidVisibleBottom = 0;
public static boolean keyboardAvoidanceActive = false;
public static float scrollBeforeKeyboard = 0;
public static UITextInputBase keyboardAvoidanceField = null;

public static class NativeCursorEditText extends EditText {
    public NativeCursorEditText(Context context) { super(context); }

    protected void onSelectionChanged(int start, int end) {
        super.onSelectionChanged(start, end);
        if (nativeInputInternalChange || activeTextField == null || !activeTextField.isFocused()) return;
        activeTextField.setCursorPosition(start);
    }
}
public static boolean uiModalVisible = false;
public static String uiModalTitle = "";
public static String uiModalMessage = "";
public static String uiModalButtonLabel = "Aceptar";
public static String uiModalCancelLabel = "";
public static boolean uiModalHasCancel = false;
public static Runnable uiModalConfirmAction = null;
public static Runnable uiModalCancelAction = null;
public static String uiModalActionId = "";

public static void initUI(String fontName, int baseFontSize) {
    uiFont = createFont(fontName, baseFontSize, true);
    textFont(uiFont);
    setTheme(UIColorTheme.LIGHT);
    keepScreenAwake();
    ensureNativeTextInput();

    // Configurar manejador de eventos por defecto
    setUIEventHandler(new UIEventHandler() {
        @Override
            public void onUIEvent(UIElement element, String action, Object data) {
        }
    }
    );
}

public static void setTheme(UIColorTheme theme) {
    if (theme == UIColorTheme.DARK) {
        currentTheme = new UIConfig(
            color(25), // background
            color(45), // surface
            color(240), // text
            color(0, 140, 255), // accent
            color(70), // border
            color(120)      // placeholder
            );
    } else {
        currentTheme = new UIConfig(
            color(245), // background
            color(255), // surface
            color(30), // text
            color(0, 100, 220), // accent
            color(210), // border
            color(170)      // placeholder
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
    updateKeyboardAvoidance();
    for (int i = 0; i < uiViewManagers.size(); i++) uiViewManagers.get(i).update();
}

public static float anchoredContentBottomPixels() {
    float bottom = 0;
    for (UIElement e : uiElements) {
        if (!e.isAnchored() || !e.isVisible) continue;
        bottom = max(
            bottom,
            viewportOffsetY + (e.y + e.height) * uiScale
        );
    }
    return bottom;
}

public static float normalMinimumScroll() {
    float maxContentBottom = calculateMaxContentBottom();
    return min(0, -(maxContentBottom - height + 100));
}

public static void updateKeyboardAvoidance() {
    UITextInputBase field = activeTextField;
    if (activeUIModal != null && activeUIModal.isVisible() && activeUIModal.ownsControl(field)) {
        keyboardAvoidanceActive = false;
        keyboardAvoidanceField = null;
        return;
    }
    boolean shouldAvoid = androidKeyboardVisible && field != null && field.isFocused() && !field.isAnchored();

    if (!shouldAvoid) {
        if (keyboardAvoidanceActive) {
            scrollState.targetY = constrain(scrollBeforeKeyboard, normalMinimumScroll(), 0);
            keyboardAvoidanceActive = false;
            keyboardAvoidanceField = null;
        }
        return;
    }

    if (!keyboardAvoidanceActive) {
        scrollBeforeKeyboard = scrollState.targetY;
        keyboardAvoidanceActive = true;
    }
    keyboardAvoidanceField = field;

    float visibleBottom = androidVisibleBottom > 0 ? androidVisibleBottom : height;
    float desiredBottom = visibleBottom - KEYBOARD_FIELD_MARGIN * uiScale;
    float fieldTop = viewportOffsetY + field.y * uiScale;
    float fieldBottom =
        viewportOffsetY + (field.y + field.height) * uiScale;
    float desiredScroll = min(scrollBeforeKeyboard, desiredBottom - fieldBottom);

    // No permitir que el ajuste coloque el campo bajo la cabecera anclada.
    float minimumFieldTop = anchoredContentBottomPixels() + KEYBOARD_FIELD_MARGIN * uiScale;
    float mostNegativeAllowed = minimumFieldTop - fieldTop;
    desiredScroll = max(desiredScroll, mostNegativeAllowed);
    desiredScroll = min(0, desiredScroll);

    if (abs(scrollState.targetY - desiredScroll) > 0.5f) {
        scrollState.targetY = desiredScroll;
        scrollState.currentY = desiredScroll;
    }
}

public static void drawUIContent() {
    syncHostState();
    // CAPA SCROLL
    pushMatrix();
    translate(viewportOffsetX, viewportOffsetY + scrollState.currentY);
    scale(uiScale);

    // Capa 1: no-dropdown scrolleables
    for (UIElement e : uiElements) {
        if (!e.isAnchored() && !(e instanceof UIDropdown)) {
            e.draw();
        }
    }

    // Capa 2: dropdowns cerrados scrolleables
    for (UIElement e : uiElements) {
        if (!e.isAnchored() && e instanceof UIDropdown) {
            UIDropdown d = (UIDropdown)e;
            if (d != activeDropdown) {
                d.draw();
            }
        }
    }

    // Capa 3: dropdown activo scrolleable
    if (activeDropdown != null && !activeDropdown.isAnchored()) {
        activeDropdown.draw();
    }

    popMatrix();

    // CAPA ANCLADA (sin scroll)
    pushMatrix();
    translate(viewportOffsetX, viewportOffsetY);
    scale(uiScale);

    // Capa 4: no-dropdown anclados
    for (UIElement e : uiElements) {
        if (e.isAnchored() && !(e instanceof UIDropdown)) {
            e.draw();
        }
    }

    // Capa 5: dropdowns anclados cerrados
    for (UIElement e : uiElements) {
        if (e.isAnchored() && e instanceof UIDropdown) {
            UIDropdown d = (UIDropdown)e;
            if (d != activeDropdown) {
                d.draw();
            }
        }
    }

    // Capa 6: dropdown anclado activo
    if (activeDropdown != null && activeDropdown.isAnchored()) {
        activeDropdown.draw();
    }

    popMatrix();

    if (activeDropdown != null && !activeDropdown.isOpen()) {
        activeDropdown = null;
    }

}

public static void drawUIOverlays() {
    syncHostState();
    drawInputOverlay();
    drawModalOverlay();
    if (activeUIModal != null) activeUIModal.draw();
}

public static void drawUI() {
    syncHostState();
    drawUIContent();
    drawUIOverlays();
}

public static void drawInputOverlay() {
    if (!showInputOverlay) return;
    if (activeTextField == null) return;
    if (!activeTextField.isFocused()) return;

    pushStyle();
    noStroke();
    fill(currentTheme.backgroundColor, 245);
    rect(0, 0, width, INPUT_OVERLAY_HEIGHT);

    stroke(currentTheme.accentColor);
    strokeWeight(2);
    line(0, INPUT_OVERLAY_HEIGHT - 1, width, INPUT_OVERLAY_HEIGHT - 1);

    fill(currentTheme.textColor);
    textAlign(LEFT, CENTER);
    textSize(16);

    String content = activeTextField.getText();
    if (content == null || content.length() == 0) {
        fill(currentTheme.placeholderColor);
        content = "...";
    }
    text(content, 14, INPUT_OVERLAY_HEIGHT / 2);
    popStyle();
}

public static void showAlertModal(String title, String message, String buttonLabel) {
    showConfirmModal(title, message, buttonLabel, "", null, null);
}

public static void showConfirmModal(String title, String message, String confirmLabel, String cancelLabel, Runnable confirmAction, Runnable cancelAction) {
    if (activeUIModal != null) activeUIModal.hide();
    uiModalTitle = title == null ? "" : title;
    uiModalMessage = message == null ? "" : message;
    uiModalButtonLabel = confirmLabel == null || confirmLabel.length() == 0 ? "Aceptar" : confirmLabel;
    uiModalCancelLabel = cancelLabel == null ? "" : cancelLabel;
    uiModalHasCancel = uiModalCancelLabel.length() > 0;
    uiModalConfirmAction = confirmAction;
    uiModalCancelAction = cancelAction;
    uiModalActionId = "";
    uiModalVisible = true;
    if (activeTextField != null) {
        activeTextField.setFocused(false);
        activeTextField = null;
    }
    closeKeyboard();
    closeAllDropdownsExcept(null);
}

public static void showConfirmModal(String title, String message, String confirmLabel, String cancelLabel, String actionId) {
    showConfirmModal(title, message, confirmLabel, cancelLabel, null, null);
    uiModalActionId = actionId == null ? "" : actionId;
}

public static void hideAlertModal() {
    uiModalVisible = false;
    uiModalConfirmAction = null;
    uiModalCancelAction = null;
    uiModalCancelLabel = "";
    uiModalHasCancel = false;
    uiModalActionId = "";
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
    float buttonX = uiModalHasCancel
        ? modalX + MODAL_WIDTH - MODAL_BUTTON_WIDTH - 18
        : modalX + (MODAL_WIDTH - MODAL_BUTTON_WIDTH) * 0.5f;
    float buttonY = modalY + MODAL_HEIGHT - MODAL_BUTTON_HEIGHT - 18;
    float cancelButtonX = modalX + 18;

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

    if (uiModalHasCancel) {
        fill(currentTheme.surfaceColor);
        stroke(currentTheme.borderColor);
        rect(cancelButtonX, buttonY, MODAL_BUTTON_WIDTH, MODAL_BUTTON_HEIGHT, BORDER_RADIUS_MEDIUM);

        fill(currentTheme.textColor);
        textAlign(CENTER, CENTER);
        textSize(15);
        text(uiModalCancelLabel, cancelButtonX + MODAL_BUTTON_WIDTH * 0.5f, buttonY + MODAL_BUTTON_HEIGHT * 0.5f);
    }

    fill(currentTheme.accentColor);
    stroke(currentTheme.accentColor);
    rect(buttonX, buttonY, MODAL_BUTTON_WIDTH, MODAL_BUTTON_HEIGHT, BORDER_RADIUS_MEDIUM);

    fill(255);
    textAlign(CENTER, CENTER);
    textSize(15);
    text(uiModalButtonLabel, buttonX + MODAL_BUTTON_WIDTH * 0.5f, buttonY + MODAL_BUTTON_HEIGHT * 0.5f);
    popMatrix();
    popStyle();
}

public static boolean isPointInModalConfirmButton(float mx, float my) {
    float designWidth = getLogicalWidth();
    float designHeight = getLogicalHeight();
    float modalX = (designWidth - MODAL_WIDTH) * 0.5f;
    float modalY = (designHeight - MODAL_HEIGHT) * 0.5f;
    float buttonX = uiModalHasCancel
        ? modalX + MODAL_WIDTH - MODAL_BUTTON_WIDTH - 18
        : modalX + (MODAL_WIDTH - MODAL_BUTTON_WIDTH) * 0.5f;
    float buttonY = modalY + MODAL_HEIGHT - MODAL_BUTTON_HEIGHT - 18;
    return mx >= buttonX && mx <= buttonX + MODAL_BUTTON_WIDTH &&
        my >= buttonY && my <= buttonY + MODAL_BUTTON_HEIGHT;
}

public static boolean isPointInModalCancelButton(float mx, float my) {
    if (!uiModalHasCancel) return false;
    float designWidth = getLogicalWidth();
    float designHeight = getLogicalHeight();
    float modalX = (designWidth - MODAL_WIDTH) * 0.5f;
    float modalY = (designHeight - MODAL_HEIGHT) * 0.5f;
    float buttonX = modalX + 18;
    float buttonY = modalY + MODAL_HEIGHT - MODAL_BUTTON_HEIGHT - 18;
    return mx >= buttonX && mx <= buttonX + MODAL_BUTTON_WIDTH &&
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
    // Si hay uno activo y abierto, tiene prioridad absoluta de hit-test.
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

// =============================================
// MANEJO DE ENTRADA T??CTIL
// =============================================
public static boolean isPointInElement(float mx, float my, UIElement element) {
    return mx > element.x && mx < element.x + element.width &&
        my > element.y && my < element.y + element.height;
}

public static float getScaledMouseX() {
    return (mouseX - viewportOffsetX) / uiScale;
}

public static float getScaledMouseY() {
    return (mouseY - viewportOffsetY - uiInputYOffset) / uiScale;
}

public static boolean isGestureScrollableElement(UIElement e) {
    return e != null && e.hasScrollableOverflow();
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

public static UIElement findTopMostAnchoredInteractiveAt(float mx, float myAnchored) {
    for (int i = uiElements.size() - 1; i >= 0; i--) {
        UIElement e = uiElements.get(i);
        if (!e.isAnchored() || !e.isVisible || !e.isEnabled || e instanceof UILabel) continue;
        if (isPointInElement(mx, myAnchored, e)) return e;
    }
    return null;
}

public static UIElement findTopMostInteractiveElementAt(float mx, float myAnchored, float myScrolled) {
    UIDropdown hitDropdown = findTopMostDropdownAt(mx, myScrolled);
    UIDropdown hitDropdownAnchored = findTopMostDropdownAt(mx, myAnchored);
    if (hitDropdownAnchored != null && hitDropdownAnchored.isAnchored()) return hitDropdownAnchored;
    if (hitDropdown != null) return hitDropdown;

    // Los controles anclados se dibujan por encima del contenido desplazable y
    // por tanto deben ganar siempre el hit-test, independientemente del orden
    // en el que fueron añadidos.
    UIElement anchoredHit = findTopMostAnchoredInteractiveAt(mx, myAnchored);
    if (anchoredHit != null) return anchoredHit;

    for (int i = uiElements.size() - 1; i >= 0; i--) {
        UIElement e = uiElements.get(i);
        if (e.isAnchored() || !e.isVisible || !e.isEnabled || e instanceof UILabel) continue;
        if (isPointInElement(mx, myScrolled, e)) {
            return e;
        }
    }
    return null;
}

public static boolean elementHasScrollableOverflow(UIElement e) {
    return e != null && e.hasScrollableOverflow();
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
        // En móvil, un desplegable solo debe conservar un gesto claramente
        // horizontal. Los arrastres verticales (también los diagonales) deben
        // seguir desplazando la página aunque comiencen encima del control.
        if (gestureState.axis == UIGestureAxis.AXIS_HORIZONTAL) {
            gestureState.target = UIGestureTarget.GESTURE_ELEMENT_ACTION;
            gestureState.capturedElement = e;
        } else {
            gestureState.target = UIGestureTarget.GESTURE_GLOBAL_SCROLL;
        }
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

    if (gestureState.axis != UIGestureAxis.AXIS_HORIZONTAL &&
        isGestureScrollableElement(e) &&
        elementHasScrollableOverflow(e)) {
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
        if (!dropdown.containsMainBox(mx, localY) &&
            !dropdown.containsOpenMenu(mx, localY)) return;
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

    if (e instanceof UIList) {
        ((UIList)e).performTapAction(mx, localY);
        return;
    }

    if (e instanceof UITable) {
        ((UITable)e).performTapAction(mx, localY);
        return;
    }

    if (e instanceof UICheckbox) {
        e.mouseReleased();
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
        closeKeyboard();
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
        uiInputYOffset = 0;
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
    uiInputYOffset = 0;
    float myAnchored = getScaledMouseY();
    uiInputYOffset = scrollState.currentY;
    float my = getScaledMouseY();

    UITextInputBase hitText = findTopMostTextFieldAt(mx, my);
    if (activeTextField != null && activeTextField != hitText) {
        activeTextField.setFocused(false);
        activeTextField = null;
        closeKeyboard();
    }

    // La cabecera anclada está visualmente por encima. Resolverla antes de
    // dropdowns o controles desplazados evita que un elemento oculto reciba
    // el tap destinado a una pestaña.
    UIElement anchoredHit = findTopMostAnchoredInteractiveAt(mx, myAnchored);
    if (anchoredHit != null) {
        gestureState.pressedElement = anchoredHit;
        if (activeDropdown != null && activeDropdown != anchoredHit) {
            activeDropdown.setOpen(false);
            activeDropdown = null;
        }
        if (anchoredHit instanceof UIButton) ((UIButton)anchoredHit).setPressedVisual(true);
        return;
    }

    UIDropdown hitDropdown = findTopMostDropdownAt(mx, my);
    UIDropdown hitDropdownAnchored = findTopMostDropdownAt(mx, myAnchored);
    float dropdownHitY = my;
    if (hitDropdownAnchored != null && hitDropdownAnchored.isAnchored()) {
        hitDropdown = hitDropdownAnchored;
        dropdownHitY = myAnchored;
    }
    if (hitDropdown != null) {
        if (!hitDropdown.containsMainBox(mx, dropdownHitY) && !hitDropdown.containsOpenMenu(mx, dropdownHitY)) {
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
    if (activeUIModal != null && activeUIModal.isVisible()) {
        if (!gestureState.passedThreshold && passedGestureThreshold()) {
            gestureState.passedThreshold = true;
            gestureState.isTapCandidate = false;
            gestureState.axis = resolveGestureAxis(
                mouseX - gestureState.startX, mouseY - gestureState.startY);
            resolveGestureTarget();
        }
        if (gestureState.passedThreshold &&
            (gestureState.target == UIGestureTarget.GESTURE_ELEMENT_SCROLL ||
            gestureState.target == UIGestureTarget.GESTURE_ELEMENT_ACTION)) {
            dispatchDragToCapturedElement();
        }
        gestureState.lastX = mouseX;
        gestureState.lastY = mouseY;
        return;
    }
    if (uiModalVisible) {
        return;
    }

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

    // Calcular l??mite de scroll
    float maxContentBottom = calculateMaxContentBottom();
    float maxScrollUp = min(0, -(maxContentBottom - height + 100));

    scrollState.targetY = constrain(scrollState.targetY, maxScrollUp, 0);
}

public static float calculateMaxContentBottom() {
    float maxBottom = 0;
    for (UIElement e : uiElements) {
        if (!e.isVisible || e.isAnchored()) continue;
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
        uiInputYOffset = 0;
        float mx = getScaledMouseX();
        float my = getScaledMouseY();
        if (isPointInModalConfirmButton(mx, my)) {
            Runnable confirmAction = uiModalConfirmAction;
            String actionId = uiModalActionId;
            hideAlertModal();
            if (confirmAction != null) confirmAction.run();
            if (actionId.length() > 0) handleModalResult(actionId, true);
        } else if (isPointInModalCancelButton(mx, my)) {
            Runnable cancelAction = uiModalCancelAction;
            String actionId = uiModalActionId;
            hideAlertModal();
            if (cancelAction != null) cancelAction.run();
            if (actionId.length() > 0) handleModalResult(actionId, false);
        }
        cleanupCapturedGestureState();
        scrollState.reset();
        gestureState.reset();
        return;
    }

    float mx = getScaledMouseX();
    uiInputYOffset = 0;
    float myAnchored = getScaledMouseY();
    uiInputYOffset = scrollState.currentY;
    float my = getScaledMouseY();

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
    if (uiModalVisible) {
        return;
    }
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
    if (uiModalVisible) {
        return;
    }
    for (UIElement e : uiElements) {
        e.keyTyped();
    }
}

// =============================================
// CLASE BASE ABSTRACTA MEJORADA
// =============================================

// =============================================
// COMPONENTES DE UI MEJORADOS
// =============================================





// Geometría lógica reutilizable para consultar límites de controles.














// =============================================
// UTILIDADES DE TECLADO (ANDROID)
// =============================================
public static void ensureNativeTextInput() {
    if (nativeInputReady) return;

    getActivity().runOnUiThread(new Runnable() {
        public void run() {
            if (nativeInputReady) return;

            FrameLayout root = (FrameLayout)getActivity().findViewById(android.R.id.content);
            if (root == null) return;

            final View keyboardRoot = root;
            if (Build.VERSION.SDK_INT >= 30) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                keyboardRoot.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                        int fullHeight = getActivity().getResources().getDisplayMetrics().heightPixels;
                        int imeHeight = insets.getInsets(WindowInsets.Type.ime()).bottom;
                        androidKeyboardVisible = insets.isVisible(WindowInsets.Type.ime());
                        androidVisibleBottom = fullHeight - imeHeight;
                        return insets;
                    }
                });
                keyboardRoot.requestApplyInsets();
            }
            keyboardRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= 30) return;
                    Rect visibleFrame = new Rect();
                    keyboardRoot.getWindowVisibleDisplayFrame(visibleFrame);
                    int fullHeight = getActivity().getResources().getDisplayMetrics().heightPixels;
                    int hiddenHeight = Math.max(0, fullHeight - visibleFrame.bottom);
                    androidVisibleBottom = visibleFrame.bottom;
                    androidKeyboardVisible = hiddenHeight > fullHeight * 0.15f;
                }
            });

            nativeInputField = new NativeCursorEditText(getActivity());
            nativeInputField.setFocusable(true);
            nativeInputField.setFocusableInTouchMode(true);
            nativeInputField.setSingleLine(true);
            nativeInputField.setImeOptions(EditorInfo.IME_ACTION_DONE);
            nativeInputField.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            nativeInputField.setTextColor(android.graphics.Color.TRANSPARENT);
            nativeInputField.setCursorVisible(false);
            nativeInputField.setAlpha(0.01f);
            nativeInputField.setX(0);
            nativeInputField.setY(0);
            nativeInputField.setMinWidth(1);
            nativeInputField.setMinimumWidth(1);

            nativeInputField.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void afterTextChanged(Editable editable) {
                    if (nativeInputInternalChange) return;
                    if (activeTextField == null || !activeTextField.isFocused()) return;

                    String next = editable == null ? "" : editable.toString();
                    int nativeCursor = nativeInputField == null ? next.length() : nativeInputField.getSelectionStart();
                    activeTextField.setText(next);
                    activeTextField.setCursorPosition(nativeCursor);
                    triggerEvent(activeTextField, "changed", activeTextField.getText());
                }
            }
            );

            nativeInputField.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
                public boolean onEditorAction(android.widget.TextView v, int actionId, android.view.KeyEvent event) {
                    boolean isDone = actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_GO ||
                        actionId == EditorInfo.IME_ACTION_SEND;
                    boolean isEnterKey = event != null &&
                        event.getAction() == android.view.KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER;

                    if (isDone || isEnterKey) {
                        if (activeTextField != null) {
                            activeTextField.submitAndCloseKeyboard();
                        }
                        return true;
                    }
                    return false;
                }
            }
            );

            root.addView(nativeInputField, new FrameLayout.LayoutParams(2, 2));
            nativeInputReady = true;
        }
    }
    );
}

public static void syncNativeInputFromActiveTextField() {
    if (!nativeInputReady || nativeInputField == null || activeTextField == null) return;

    final String targetText = activeTextField.getText();
    final int targetCursor = activeTextField.getCursorPosition();
    getActivity().runOnUiThread(new Runnable() {
        public void run() {
            if (nativeInputField == null) return;
            nativeInputInternalChange = true;
            try {
                // setInputType() can notify the TextWatcher while switching between
                // a single-line field and a text area. Keep the bridge muted until
                // the native editor contains the new active field's own value.
                configureNativeInputForActiveField();
                String currentText = nativeInputField.getText() == null ? "" : nativeInputField.getText().toString();
                if (!currentText.equals(targetText)) nativeInputField.setText(targetText);
                nativeInputField.setSelection(constrain(targetCursor, 0, nativeInputField.getText().length()));
            } finally {
                nativeInputInternalChange = false;
            }
        }
    }
    );
}

public static void syncNativeSelectionFromActiveTextField() {
    if (!nativeInputReady || nativeInputField == null || activeTextField == null) return;
    final int targetCursor = activeTextField.getCursorPosition();
    getActivity().runOnUiThread(new Runnable() {
        public void run() {
            if (nativeInputField == null) return;
            nativeInputInternalChange = true;
            try {
                nativeInputField.setSelection(constrain(targetCursor, 0, nativeInputField.getText().length()));
            } finally {
                nativeInputInternalChange = false;
            }
        }
    });
}

public static void openKeyboard() {
    ensureNativeTextInput();
    getActivity().runOnUiThread(new Runnable() {
        public void run() {
            if (nativeInputField != null && activeTextField != null) {
                nativeInputInternalChange = true;
                try {
                    // Reconfiguring EditText may emit a text callback containing
                    // the previous control's value. Do not let it leak into the
                    // field that has just received focus.
                    configureNativeInputForActiveField();
                    nativeInputField.setText(activeTextField.getText());
                    nativeInputField.setSelection(constrain(activeTextField.getCursorPosition(), 0, nativeInputField.getText().length()));
                } finally {
                    nativeInputInternalChange = false;
                }
                nativeInputField.requestFocus();
            }
            requestNativeKeyboard(0);
        }
    }
    );
}

private static void requestNativeKeyboard(final int attempt) {
    if (nativeInputField == null || activeTextField == null) return;
    long delay = attempt == 0 ? 0L : 140L;
    nativeInputField.postDelayed(new Runnable() {
        public void run() {
            if (nativeInputField == null || activeTextField == null || !activeTextField.isFocused()) return;
            nativeInputField.requestFocus();
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null) return;
            imm.restartInput(nativeInputField);
            imm.showSoftInput(nativeInputField, 0);
            if (Build.VERSION.SDK_INT >= 30 && getActivity().getWindow().getInsetsController() != null) {
                getActivity().getWindow().getInsetsController().show(WindowInsets.Type.ime());
            }
            if (attempt < 2 && !androidKeyboardVisible) requestNativeKeyboard(attempt + 1);
        }
    }, delay);
}

public static void configureNativeInputForActiveField() {
    if (nativeInputField == null || activeTextField == null) return;

    boolean isTextArea = activeTextField instanceof UITextArea;
    boolean isNumberField = activeTextField instanceof UINumberField;
    nativeInputField.setSingleLine(!isTextArea);
    nativeInputField.setHorizontallyScrolling(!isTextArea);
    nativeInputField.setMinLines(isTextArea ? 3 : 1);
    nativeInputField.setMaxLines(isTextArea ? 6 : 1);

    if (isNumberField) {
        UINumberField numericField = (UINumberField)activeTextField;
        int inputType = InputType.TYPE_CLASS_NUMBER;
        if (numericField.allowsDecimal()) {
            inputType |= InputType.TYPE_NUMBER_FLAG_DECIMAL;
        }
        if (numericField.allowsNegative()) {
            inputType |= InputType.TYPE_NUMBER_FLAG_SIGNED;
        }
        nativeInputField.setInputType(inputType);
        nativeInputField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        return;
    }

    nativeInputField.setInputType(isTextArea
        ? (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        : InputType.TYPE_CLASS_TEXT);
    nativeInputField.setImeOptions(isTextArea ? EditorInfo.IME_FLAG_NO_ENTER_ACTION : EditorInfo.IME_ACTION_DONE);
}

public static void closeKeyboard() {
    getActivity().runOnUiThread(new Runnable() {
        public void run() {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null) return;

            if (nativeInputField != null) {
                nativeInputField.clearFocus();
                imm.hideSoftInputFromWindow(nativeInputField.getWindowToken(), 0);
                return;
            }

            View decorView = getActivity().getWindow() != null ? getActivity().getWindow().getDecorView() : null;
            if (decorView != null) {
                imm.hideSoftInputFromWindow(decorView.getWindowToken(), 0);
            }
        }
    }
    );
}

public static void keepScreenAwake() {
    getActivity().runOnUiThread(new Runnable() {
        public void run() {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
    );
}

// =============================================
// FUNCIONES DE UTILIDAD ADICIONALES
// =============================================
public static void setUIAlpha(float alpha) {
    for (UIElement e : uiElements) {
        // Implementar transparencia si es necesario
    }
}

public static void enableAllElements() {
    for (UIElement e : uiElements) {
        e.setEnabled(true);
    }
}

public static void disableAllElements() {
    for (UIElement e : uiElements) {
        e.setEnabled(false);
    }
}

public static boolean isAnyDropdownOpen() {
    for (UIElement e : uiElements) {
        if (e instanceof UIDropdown && ((UIDropdown)e).isOpen()) {
            return true;
        }
    }
    return false;
}

// =============================================
// FUNCI??N PARA LIMPIAR ESTADOS COLGADOS
// =============================================
public static void cleanupUIStates() {
    // Forzar limpieza de estados en todos los elementos
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
