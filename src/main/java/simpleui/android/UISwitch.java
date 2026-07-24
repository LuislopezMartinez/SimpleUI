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

public class UISwitch extends UIElement {
    public String label;
    public boolean checked;
    public int fontSize;

    public UISwitch(
        String id,
        int x,
        int y,
        int w,
        int h,
        String label,
        boolean initialValue,
        int fontSize
    ) {
        super(id, x, y, w, h);
        this.label = label == null ? "" : label;
        this.checked = initialValue;
        this.fontSize = fontSize;
    }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean value) { checked = value; }
    public void setLabel(String value) { label = value == null ? "" : value; }

    public void toggle() {
        if (!isEnabled) return;
        checked = !checked;
        triggerEvent(this, "changed", Boolean.valueOf(checked));
    }

    public void draw() {
        if (!isVisible) return;
        pushStyle();

        float trackWidth = min(48, max(34, width * 0.28f));
        float trackHeight = min(24, max(18, height * 0.62f));
        float trackX = x + width - trackWidth;
        float trackY = y + (height - trackHeight) * 0.5f;
        float knobDiameter = trackHeight - 6;
        float knobX = checked
            ? trackX + trackWidth - 3 - knobDiameter
            : trackX + 3;
        float knobY = trackY + 3;

        int textColor = isEnabled
            ? currentTheme.textColor
            : currentTheme.placeholderColor;
        int trackColor = checked && isEnabled
            ? currentTheme.accentColor
            : currentTheme.borderColor;

        fill(textColor);
        noStroke();
        textAlign(LEFT, CENTER);
        textSize(fontSize);
        text(label, x, y, max(0, width - trackWidth - 10), height);

        stroke(trackColor);
        strokeWeight(1.5f);
        fill(trackColor, checked && isEnabled ? 210 : 105);
        rect(trackX, trackY, trackWidth, trackHeight, trackHeight * 0.5f);

        noStroke();
        fill(isEnabled ? color(255) : currentTheme.placeholderColor);
        ellipse(
            knobX + knobDiameter * 0.5f,
            knobY + knobDiameter * 0.5f,
            knobDiameter,
            knobDiameter
        );
        popStyle();
    }

    public void performTapAction(float mx, float my) {
        if (!isEnabled || !containsPoint(mx, my)) return;
        toggle();
    }
}
