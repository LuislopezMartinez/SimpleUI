package simpleui.android;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.android.SimpleUI.*;
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

public abstract class UIView {
    public String id;
    public UIViewManager manager;
    public ArrayList<UIElement> controls = new ArrayList<UIElement>();
    public ArrayList<Boolean> enabledStates = new ArrayList<Boolean>();
    public ArrayList<Boolean> visibleStates = new ArrayList<Boolean>();
    public boolean initialized = false;
    public boolean visible = false;
    public float savedScrollY = 0;

    public UIView(String id, UIViewManager manager) {
        this.id = id == null ? "" : id;
        this.manager = manager;
    }

    public abstract void initialize();
    public void onUIEvent(UIElement element, String action, Object data) {}
    public void update() {}
    public void onShow() {}
    public void onHide() {}
    public void onDestroy() {}

    public final void addControl(UIElement element) {
        if (element == null || controls.contains(element)) return;
        controls.add(element);
        enabledStates.add(element.isEnabled);
        visibleStates.add(element.isVisible);
        if (!uiElements.contains(element)) addUIElement(element);
    }

    public final boolean ownsControl(UIElement element) {
        return element != null && controls.contains(element);
    }

    public final void show() {
        if (manager == null) throw new RuntimeException("UIView has no UIViewManager: " + id);
        manager.show(this);
    }

    public final void hide() {
        if (manager != null) manager.hide(this);
    }

    public final void destroy() {
        if (manager != null) manager.destroy(this);
    }

    public final boolean isVisible() { return visible; }
    public final boolean isInitialized() { return initialized; }
    public final String getId() { return id; }
}
