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

public class UIButton extends UIElement {
    public String label;
    public int fontSize;
    public boolean isPressed = false;
    public boolean useCustomColors = false;
    public int customFillColor = color(180);
    public int customBorderColor = color(120);
    public int customTextColor = color(255);

    public UIButton(String id, int x, int y, int w, int h, String label, int fontSize) {
        super(id, x, y, w, h);
        this.label = label;
        this.fontSize = fontSize;
    }

    public void setLabel(String newLabel) {
        if (newLabel != null) label = newLabel;
    }

    public void setColors(int fillColor, int borderColor, int textColor) {
        useCustomColors = true;
        customFillColor = fillColor;
        customBorderColor = borderColor;
        customTextColor = textColor;
    }

    public void clearCustomColors() {
        useCustomColors = false;
    }

    public void setPressedVisual(boolean pressed) {
        isPressed = pressed;
    }

    public void draw() {
        if (!isVisible) return;
        if (useCustomColors &&
            label != null && label.length() == 0 &&
            alpha(customFillColor) == 0 &&
            alpha(customBorderColor) == 0 &&
            alpha(customTextColor) == 0) {
            // Boton invisible (hit-area): no dibujar nada, solo capturar eventos.
            return;
        }

        int borderC;
        int fillC;
        int textC;

        if (useCustomColors) {
            borderC = customBorderColor;
            fillC = isPressed ? lerpColor(customFillColor, color(0), 0.25f) : customFillColor;
            textC = customTextColor;
        } else {
            borderC = currentTheme.accentColor;
            fillC = isPressed ? currentTheme.accentColor : currentTheme.surfaceColor;
            textC = isPressed ? 255 : currentTheme.accentColor;
        }

        if (!isEnabled) {
            borderC = color(185, 190, 197);
            fillC = color(205, 209, 214);
            textC = color(112, 118, 126);
        }

        stroke(borderC);
        fill(fillC);
        rect(x, y, width, height, BORDER_RADIUS_LARGE);

        fill(textC);
        textAlign(CENTER, CENTER);
        textSize(fontSize);
        text(label, x + width / 2, y + height / 2);

        if (!mousePressed) {
            isPressed = false;
        }
    }

    public void mousePressed() {
    }

    public void mouseReleased() {
        isPressed = false;
    }

    public void performTapAction() {
        if (!isEnabled) return;
        isPressed = true;
        triggerEvent(this, "clicked", null);
    }

    public boolean isPressedState() {
        return isPressed;
    }
}
