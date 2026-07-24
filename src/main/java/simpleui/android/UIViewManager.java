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

public class UIViewManager {
    public String id;
    public UIView activeView = null;

    public UIViewManager(String id) {
        this.id = id == null ? "" : id;
        uiViewManagers.add(this);
    }

    public void prepare(UIView view) {
        if (view == null || view.initialized) return;
        if (view.manager != this) view.manager = this;
        view.initialize();
        view.initialized = true;
        view.enabledStates.clear();
        view.visibleStates.clear();
        for (int i = 0; i < view.controls.size(); i++) {
            UIElement element = view.controls.get(i);
            view.enabledStates.add(element.isEnabled);
            view.visibleStates.add(element.isVisible);
            element.setVisible(false);
            element.setEnabled(false);
        }
        view.visible = false;
    }

    public void show(UIView nextView) {
        if (nextView == null) return;
        if (nextView.manager != this) nextView.manager = this;
        if (activeView == nextView && nextView.visible) return;
        if (activeView != null) hideInternal(activeView, true);
        if (!nextView.initialized) prepare(nextView);
        for (int i = 0; i < nextView.controls.size(); i++) {
            UIElement element = nextView.controls.get(i);
            boolean controlVisible = i < nextView.visibleStates.size() ? nextView.visibleStates.get(i) : true;
            element.setVisible(controlVisible);
            boolean enabled = i < nextView.enabledStates.size() ? nextView.enabledStates.get(i) : true;
            element.setEnabled(enabled);
        }
        activeView = nextView;
        nextView.visible = true;
        scrollState.currentY = nextView.savedScrollY;
        scrollState.targetY = nextView.savedScrollY;
        gestureState.reset();
        closeAllDropdownsExcept(null);
        nextView.onShow();
    }

    public void hide(UIView view) {
        if (view == null || !view.initialized || !view.visible) return;
        hideInternal(view, activeView == view);
    }

    public void hideInternal(UIView view, boolean clearActive) {
        view.savedScrollY = scrollState.targetY;
        view.enabledStates.clear();
        view.visibleStates.clear();
        for (int i = 0; i < view.controls.size(); i++) {
            UIElement element = view.controls.get(i);
            view.enabledStates.add(element.isEnabled);
            view.visibleStates.add(element.isVisible);
            element.setVisible(false);
            element.setEnabled(false);
        }
        view.visible = false;
        view.onHide();
        if (clearActive && activeView == view) activeView = null;
    }

    public boolean routeEvent(UIElement element, String action, Object data) {
        if (activeView == null || !activeView.visible || !activeView.ownsControl(element)) return false;
        activeView.onUIEvent(element, action, data);
        return true;
    }

    public void update() {
        if (activeView != null && activeView.visible) activeView.update();
    }

    public void destroy(UIView view) {
        if (view == null || !view.initialized) return;
        if (view.visible) hideInternal(view, activeView == view);
        for (int i = view.controls.size() - 1; i >= 0; i--) removeUIElement(view.controls.get(i).id);
        view.controls.clear();
        view.enabledStates.clear();
        view.visibleStates.clear();
        view.initialized = false;
        view.onDestroy();
    }

    public UIView getActiveView() { return activeView; }
    public String getId() { return id; }
}
