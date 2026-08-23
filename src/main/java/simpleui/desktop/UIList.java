package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UIList extends UIElement {
  public ArrayList<String> items = new ArrayList<String>();
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

  public void setRowHeight(float nextRowHeight) {
    rowHeight = max(fontSize + 4, nextRowHeight);
    internalScrollY = constrain(internalScrollY, minScroll(), 0);
  }

  public void setRowHeightMultiplier(float multiplier) {
    setRowHeight(fontSize * multiplier);
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

  public void clearCustomColors() { useCustomColors = false; }

  public void addItem(String item) {
    items.add(item == null ? "" : item);
  }

  public void setItems(ArrayList<String> nextItems) {
    items = new ArrayList<String>();
    if (nextItems != null) items.addAll(nextItems);
    selectedIndex = -1;
    internalScrollY = constrain(internalScrollY, minScroll(), 0);
  }

  public int getItemCount() {
    return items.size();
  }

  public String getItem(int index) {
    if (index < 0 || index >= items.size()) return "";
    return items.get(index);
  }

  public String getSelectedItem() {
    return selectedIndex >= 0 && selectedIndex < items.size() ? items.get(selectedIndex) : null;
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public void setSelectedIndex(int index) {
    selectedIndex = (index >= 0 && index < items.size()) ? index : -1;
  }

  public void clearItems() {
    items.clear();
    selectedIndex = -1;
    internalScrollY = 0;
  }

  public void scrollToBottom() {
    internalScrollY = minScroll();
  }

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
    if (textWidth(suffix) > availableWidth) return "";
    int low = 0;
    int high = source.length();
    while (low < high) {
      int middle = (low + high + 1) / 2;
      if (textWidth(source.substring(0, middle)) + textWidth(suffix) <= availableWidth) low = middle;
      else high = middle - 1;
    }
    return source.substring(0, low) + suffix;
  }

  public void draw() {
    if (!isVisible) return;
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
      text(title, x + 12, y + rowHeight * 0.5f);
      contentTop += rowHeight;
    }

    for (int i = 0; i < items.size(); i++) {
      float rowY = contentTop + internalScrollY + i * rowHeight;
      float rowBottom = rowY + rowHeight;
      if (rowBottom <= contentTop || rowY >= y + height) continue;
      if (i == selectedIndex) {
        noStroke();
        fill(currentTheme.accentColor, 28);
        float drawY = max(rowY, contentTop);
        float drawH = min(rowBottom, y + height) - drawY;
        if (drawH > 0) rect(x + 1, drawY, width - 2, drawH);
      }
      if (rowY >= contentTop - rowHeight * 0.5f && rowY <= y + height - rowHeight * 0.5f) {
        float textY = constrain(
          rowY + rowHeight * 0.5f,
          contentTop + rowHeight * 0.5f,
          y + height - rowHeight * 0.5f
        );
        fill(rowTextColor);
        textAlign(LEFT, CENTER);
        textSize(fontSize);
        text(truncateToWidth(items.get(i), width - 24), x + 12, textY);
      }
      if (i < items.size() - 1 && rowBottom >= contentTop && rowBottom <= y + height) {
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
    internalScrollY += (mouseY - pmouseY) / uiScaleY;
    internalScrollY = constrain(internalScrollY, minScroll(), 0);
  }

  public void performTapAction(float mx, float my) {
    if (!isEnabled) return;
    float contentTop = y + headerHeight();
    if (!containsPoint(mx, my) || my < contentTop) return;
    int index = floor((my - contentTop - internalScrollY) / rowHeight);
    if (index >= 0 && index < items.size()) {
      selectedIndex = index;
      triggerEvent(this, "itemSelected", items.get(index));
    }
  }
}
