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

public class UIIndicator extends UIElement {
    public int indicatorColor = color(0);
    public boolean useCustomBorderColor = false;
    public int customBorderColor = color(0);

    public UIIndicator(String id, int x, int y, int size) {
        super(id, x, y, size, size);
    }

    public int getIndicatorColor() {
        return indicatorColor;
    }

    public void setIndicatorColor(int nextColor) {
        indicatorColor = nextColor;
    }

    public void setBorderColor(int nextBorderColor) {
        useCustomBorderColor = true;
        customBorderColor = nextBorderColor;
    }

    public void clearBorderColor() {
        useCustomBorderColor = false;
    }

    public void draw() {
        if (!isVisible) return;

        int borderColor = useCustomBorderColor ? customBorderColor : currentTheme.borderColor;

        stroke(borderColor);
        strokeWeight(2);
        fill(currentTheme.surfaceColor);
        rect(x, y, width, height, BORDER_RADIUS_SMALL);

        noStroke();
        fill(indicatorColor);
        rect(x + 4, y + 4, width - 8, height - 8, BORDER_RADIUS_SMALL);
    }
}
