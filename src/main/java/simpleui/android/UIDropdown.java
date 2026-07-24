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

public class UIDropdown extends UIElement {
    public String[] options;
    public int selectedIndex = 0;
    public int fontSize;
    public boolean isOpen = false;
    public int pressedMenuIndex = -1;
    public int flashMenuIndex = -1;
    public int flashUntilMs = 0;

    public UIDropdown(String id, int x, int y, int w, int h, String[] options, int fontSize) {
        super(id, x, y, w, h);
        this.options = options;
        this.fontSize = fontSize;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean nextOpen) {
        isOpen = nextOpen;
        if (!isOpen) {
            pressedMenuIndex = -1;
        }
    }

    public boolean containsMainBox(float mx, float my) {
        return mx > x && mx < x + width && my > y && my < y + height;
    }

    public boolean containsOpenMenu(float mx, float my) {
        if (!isOpen) return false;
        return mx > x && mx < x + width &&
            my > y + height && my < y + height + (options.length * height);
    }

    public String getSelectedValue() {
        return options[selectedIndex];
    }

    public String[] getOptions() {
        String[] copy = new String[options.length];
        arrayCopy(options, copy);
        return copy;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setOptions(String[] newOptions) {
        if (newOptions == null || newOptions.length == 0) {
            options = new String[] { "Sin opciones" };
            selectedIndex = 0;
            isOpen = false;
            return;
        }

        options = new String[newOptions.length];
        arrayCopy(newOptions, options);
        selectedIndex = constrain(selectedIndex, 0, options.length - 1);
        isOpen = false;
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < options.length) {
            selectedIndex = index;
        }
    }

    public boolean handleClickAt(float mx, float my) {
        if (containsMainBox(mx, my)) {
            isOpen = !isOpen;
            return true;
        }
        if (isOpen && containsOpenMenu(mx, my)) {
            int newIndex = floor((my - (y + height)) / height);
            if (newIndex >= 0 && newIndex < options.length) {
                selectedIndex = newIndex;
                flashMenuIndex = newIndex;
                flashUntilMs = millis() + 180;
                triggerEvent(this, "changed", options[selectedIndex]);
            }
            pressedMenuIndex = -1;
            isOpen = false;
            return true;
        }
        if (isOpen) {
            pressedMenuIndex = -1;
            isOpen = false;
            return true;
        }
        return false;
    }

    public void draw() {
        if (!isVisible) return;
        int now = millis();
        if (flashUntilMs > 0 && now >= flashUntilMs) {
            flashUntilMs = 0;
            flashMenuIndex = -1;
        }

        float pointerX = getScaledMouseX();
        float pointerY = isAnchored()
            ? (mouseY - viewportOffsetY) / uiScale
            : (mouseY - viewportOffsetY - scrollState.currentY) / uiScale;
        if (isOpen && gestureState.pressedElement == this && gestureState.isTapCandidate && containsOpenMenu(pointerX, pointerY)) {
            pressedMenuIndex = floor((pointerY - (y + height)) / height);
            if (pressedMenuIndex < 0 || pressedMenuIndex >= options.length) {
                pressedMenuIndex = -1;
            }
        } else if (gestureState.pressedElement != this) {
            pressedMenuIndex = -1;
        }

        // Dibujar caja principal
        stroke(isOpen ? currentTheme.accentColor : currentTheme.borderColor);
        fill(currentTheme.surfaceColor);
        strokeWeight(2);
        rect(x, y, width, height, BORDER_RADIUS_MEDIUM);

        // Texto seleccionado
        fill(currentTheme.textColor);
        textAlign(LEFT, CENTER);
        textSize(fontSize);
        text(options[selectedIndex], x + DROPDOWN_ARROW_SIZE, y + height / 2);

        // Flecha
        textAlign(RIGHT, CENTER);
        text(isOpen ? "???" : "???", x + width - DROPDOWN_ARROW_SIZE, y + height / 2);

        // Men?? desplegable
        if (isOpen) {
            for (int i = 0; i < options.length; i++) {
                int itemY = y + height + (i * height);
                if (i == pressedMenuIndex) {
                    fill(lerpColor(currentTheme.surfaceColor, currentTheme.accentColor, 0.22f));
                } else if (i == flashMenuIndex && flashUntilMs > now) {
                    fill(lerpColor(currentTheme.surfaceColor, currentTheme.accentColor, 0.14f));
                } else {
                    fill(currentTheme.surfaceColor);
                }
                stroke(currentTheme.borderColor);
                rect(x, itemY, width, height);

                fill(i == selectedIndex ? currentTheme.accentColor : currentTheme.textColor);
                textAlign(LEFT, CENTER);
                text(options[i], x + DROPDOWN_ARROW_SIZE, itemY + height / 2);
            }

            noFill();
            stroke(currentTheme.accentColor);
            rect(x, y + height, width, options.length * height, 0, 0, BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM);
        }
    }

    public void mouseReleased() {
        if (!isEnabled) return;
        handleClickAt(getScaledMouseX(), getScaledMouseY());
    }
}
