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

public class UIPanel extends UIElement {
    public String title = "";
    public int titleFontSize = 14;
    public boolean showTitle = false;
    public boolean useCustomColors = false;
    public int customFillColor = color(255);
    public int customBorderColor = color(210);
    public int customTitleColor = color(30);

    public UIPanel(String id, int x, int y, int w, int h) {
        super(id, x, y, w, h);
    }

    public void setTitle(String nextTitle, int nextFontSize) {
        title = nextTitle == null ? "" : nextTitle;
        titleFontSize = max(8, nextFontSize);
        showTitle = title.length() > 0;
    }

    public void clearTitle() {
        title = "";
        showTitle = false;
    }

    public void setColors(int fillColor, int borderColor, int titleColor) {
        useCustomColors = true;
        customFillColor = fillColor;
        customBorderColor = borderColor;
        customTitleColor = titleColor;
    }

    public void clearCustomColors() {
        useCustomColors = false;
    }

    public void draw() {
        if (!isVisible) return;

        pushStyle();
        int fillColor = useCustomColors ? customFillColor : currentTheme.surfaceColor;
        int borderColor = useCustomColors ? customBorderColor : currentTheme.borderColor;
        int titleColor = useCustomColors ? customTitleColor : currentTheme.textColor;

        stroke(borderColor);
        strokeWeight(2);
        fill(fillColor);
        rect(x, y, width, height, BORDER_RADIUS_LARGE);

        if (showTitle) {
            fill(titleColor);
            textAlign(LEFT, TOP);
            textSize(titleFontSize);
            text(title, x + 12, y + 10, width - 24, titleFontSize * 2);
        }
        popStyle();
    }
}
