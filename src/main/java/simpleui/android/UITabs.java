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

public class UITabs extends UIElement {
    public String[] tabs;
    public int selectedIndex = 0;
    public int fontSize;

    public UITabs(String id, int x, int y, int w, int h, String[] tabs, int fontSize) {
        super(id, x, y, w, h);
        this.tabs = tabs;
        this.fontSize = fontSize;
    }

    public int getSelectedIndex() { return selectedIndex; }
    public String getSelectedLabel() { return tabs[selectedIndex]; }

    public void setSelectedIndex(int idx) {
        if (idx >= 0 && idx < tabs.length) selectedIndex = idx;
    }

    public void setTabs(String[] nextTabs) {
        if (nextTabs == null || nextTabs.length == 0) return;
        tabs = nextTabs.clone();
        selectedIndex = constrain(selectedIndex, 0, tabs.length - 1);
    }

    public int getTabCount() { return tabs.length; }

    public UIRect getTabBounds(int index) {
        if (index < 0 || index >= tabs.length) return null;
        float tabW = width / (float)tabs.length;
        return new UIRect(x + index * tabW, y, tabW, height);
    }

    public void draw() {
        if (!isVisible) return;
        pushStyle();
        float tabW = width / (float)tabs.length;
        stroke(currentTheme.borderColor);
        strokeWeight(2);
        for (int i = 0; i < tabs.length; i++) {
            float tx = x + i * tabW;
            fill(i == selectedIndex ? currentTheme.accentColor : currentTheme.surfaceColor);
            rect(tx, y, tabW, height, BORDER_RADIUS_MEDIUM);
            noStroke();
            fill(i == selectedIndex ? color(255, 255, 255) : currentTheme.textColor);
            textAlign(CENTER, CENTER);
            textSize(fontSize);
            text(tabs[i], tx + tabW / 2, y + height / 2);
        }
        popStyle();
    }

    public void mousePressed() {
    }

    public void performTapAction(float mx, float my) {
        if (!isEnabled) return;
        if (!containsPoint(mx, my)) return;
        float tabW = width / (float)tabs.length;
        int idx = (int)((mx - x) / tabW);
        if (idx >= 0 && idx < tabs.length && idx != selectedIndex) {
            selectedIndex = idx;
            triggerEvent(this, "tabChanged", tabs[selectedIndex]);
        }
    }
}
