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

public class UICheckbox extends UIElement {
    public String label;
    public int fontSize;
    public boolean isChecked = false;

    public UICheckbox(String id, int x, int y, int size, String label, int fontSize) {
        super(id, x, y, size, size);
        this.label = label;
        this.fontSize = fontSize;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void draw() {
        if (!isVisible) return;

        // Caja del checkbox
        stroke(isChecked ? currentTheme.accentColor : currentTheme.borderColor);
        fill(currentTheme.surfaceColor);
        rect(x, y, height, height, BORDER_RADIUS_SMALL);

        // Marca de verificaci??n
        if (isChecked) {
            fill(currentTheme.accentColor);
            noStroke();
            rect(x + 5, y + 5, height - 10, height - 10, 2);
        }

        // Etiqueta
        fill(currentTheme.textColor);
        textAlign(LEFT, CENTER);
        textSize(fontSize);
        text(label, x + height + 15, y + height / 2);
    }

    public void mouseReleased() {
        if (!isEnabled) return;

        float mx = getScaledMouseX();
        float my = getScaledMouseY();

        float labelWidth = textWidth(label);
        if (mx > x && mx < x + height + 20 + labelWidth &&
            my > y && my < y + height) {
            isChecked = !isChecked;
            triggerEvent(this, "changed", isChecked);
        }
    }

    public void performTapAction(float mx, float my) {
        if (!isEnabled) return;
        float labelWidth = textWidth(label);
        if (mx > x && mx < x + height + 20 + labelWidth &&
            my > y && my < y + height) {
            isChecked = !isChecked;
            triggerEvent(this, "changed", isChecked);
        }
    }
}
