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

public class UIList extends UIElement {
    public volatile ArrayList<String> items = new ArrayList<String>();
    public int fontSize;
    public String title = "";
    public boolean showTitle = false;
    public float internalScrollY = 0;
    public float rowHeight;
    public int selectedIndex = -1;
    public boolean useCustomColors = false;
    public int customFillColor = color(255);
    public int customBorderColor = color(210);
    public int customTitleFillColor = color(0, 100, 220);
    public int customTitleTextColor = color(255);
    public int customRowTextColor = color(30);

    public UIList(String id, int x, int y, int w, int h, int fontSize) {
        super(id, x, y, w, h);
        this.fontSize = fontSize;
        this.rowHeight = fontSize * ROW_HEIGHT_MULTIPLIER;
    }

    public void setRowHeightMultiplier(float multiplier) {
        rowHeight = max(fontSize * 1.1f, fontSize * multiplier);
        internalScrollY = constrain(internalScrollY, minScroll(), 0);
    }

    public void setRowHeight(float nextRowHeight) {
        rowHeight = max(fontSize + 4, nextRowHeight);
        internalScrollY = constrain(internalScrollY, minScroll(), 0);
    }

    public void setTitle(String nextTitle) {
        title = nextTitle == null ? "" : nextTitle;
        showTitle = title.length() > 0;
    }

    public void setColors(int fillColor, int borderColor, int titleFillColor, int titleTextColor, int rowTextColor) {
        useCustomColors = true;
        customFillColor = fillColor;
        customBorderColor = borderColor;
        customTitleFillColor = titleFillColor;
        customTitleTextColor = titleTextColor;
        customRowTextColor = rowTextColor;
    }

    public void clearCustomColors() {
        useCustomColors = false;
    }

    public void addItem(String item) {
        ArrayList<String> nextItems = new ArrayList<String>(items);
        nextItems.add(item == null ? "" : item);
        items = nextItems;
    }

    public void setItems(ArrayList<String> nextItems) {
        ArrayList<String> replacement = new ArrayList<String>();
        if (nextItems != null) replacement.addAll(nextItems);
        items = replacement;
        selectedIndex = -1;
        internalScrollY = constrain(internalScrollY, minScroll(), 0);
    }

    public void clearItems() {
        items = new ArrayList<String>();
        selectedIndex = -1;
        internalScrollY = 0;
    }

    public int getItemCount() {
        return items.size();
    }

    public String getItem(int index) {
        if (index < 0 || index >= items.size()) return "";
        return items.get(index);
    }

    public String getSelectedItem() {
        if (selectedIndex < 0 || selectedIndex >= items.size()) return null;
        return items.get(selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index >= 0 && index < items.size() ? index : -1;
    }

    public void scrollToBottom() {
        internalScrollY = minScroll();
    }

    @Override
    public boolean hasScrollableOverflow() {
        return minScroll() < 0;
    }

    public float headerHeight() {
        return showTitle ? rowHeight : 0;
    }

    public float minScroll() {
        float visibleHeight = height - headerHeight();
        float contentHeight = items.size() * rowHeight;
        return min(0, visibleHeight - contentHeight);
    }

    public String truncateToWidth(String value, float availableWidth) {
        String source = value == null ? "" : value;
        if (availableWidth <= 0) return "";
        if (textWidth(source) <= availableWidth) return source;

        String suffix = "...";
        float suffixWidth = textWidth(suffix);
        if (suffixWidth > availableWidth) return "";

        int low = 0;
        int high = source.length();
        while (low < high) {
            int middle = (low + high + 1) / 2;
            if (textWidth(source.substring(0, middle)) + suffixWidth <= availableWidth) low = middle;
            else high = middle - 1;
        }
        return source.substring(0, low) + suffix;
    }

    public void draw() {
        if (!isVisible) return;
        ArrayList<String> drawItems = items;

        pushStyle();
        int fillColor = useCustomColors ? customFillColor : currentTheme.surfaceColor;
        int borderColor = useCustomColors ? customBorderColor : currentTheme.borderColor;
        int titleFillColor = useCustomColors ? customTitleFillColor : currentTheme.accentColor;
        int titleTextColor = useCustomColors ? customTitleTextColor : color(255);
        int rowTextColor = useCustomColors ? customRowTextColor : currentTheme.textColor;

        fill(fillColor);
        stroke(borderColor);
        strokeWeight(2);
        rect(x, y, width, height, BORDER_RADIUS_MEDIUM);

        float contentTop = y;
        if (showTitle) {
            noStroke();
            fill(titleFillColor);
            rect(x, y, width, rowHeight, BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, 0, 0);
            fill(titleTextColor);
            textAlign(LEFT, CENTER);
            textSize(fontSize);
            text(truncateToWidth(title, width - 24), x + 12, y + rowHeight * 0.5f);
            contentTop += rowHeight;
        }

        for (int i = 0; i < drawItems.size(); i++) {
            float rowY = contentTop + internalScrollY + i * rowHeight;
            float rowBottom = rowY + rowHeight;
            boolean rowInsideContent = rowY >= contentTop && rowBottom <= y + height;
            if (!rowInsideContent) continue;

            if (i == selectedIndex) {
                noStroke();
                fill(currentTheme.accentColor, 28);
                rect(x + 1, rowY, width - 2, rowHeight);
            }

            fill(rowTextColor);
            textAlign(LEFT, CENTER);
            textSize(fontSize);
            text(truncateToWidth(drawItems.get(i), width - 24), x + 12, rowY + rowHeight * 0.5f);

            if (i < drawItems.size() - 1 && rowBottom >= contentTop && rowBottom <= y + height) {
                stroke(borderColor, 80);
                line(x + 1, rowBottom, x + width - 1, rowBottom);
            }
        }
        noFill();
        stroke(borderColor);
        strokeWeight(2);
        rect(x, y, width, height, BORDER_RADIUS_MEDIUM);
        popStyle();
    }

    public void mousePressed() {
    }

    public void mouseDragged() {
        if (!isEnabled) return;

        float mx = getScaledMouseX();
        float my = getScaledMouseY();
        float contentTop = y + headerHeight();
        if (!containsPoint(mx, my) || my < contentTop) return;

        internalScrollY += (mouseY - pmouseY) / uiScale;
        internalScrollY = constrain(internalScrollY, minScroll(), 0);
    }

    @Override
    public void performTapAction(float mx, float my) {
        if (!isEnabled) return;

        float contentTop = y + headerHeight();
        if (!containsPoint(mx, my) || my < contentTop) return;

        int index = floor((my - contentTop - internalScrollY) / rowHeight);
        ArrayList<String> tapItems = items;
        if (index >= 0 && index < tapItems.size()) {
            selectedIndex = index;
            triggerEvent(this, "itemSelected", tapItems.get(index));
        }
    }
}
