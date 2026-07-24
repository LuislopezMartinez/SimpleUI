package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UITable extends UIElement {
  public String[] headers;
  public float[] columnWidths;
  public ArrayList<String[]> rows = new ArrayList<String[]>();
  public int fontSize;
  public float internalScrollY = 0;
  public float rowHeight;
  public int selectedIndex = -1;

  public UITable(String id, int x, int y, int w, int h, String[] headers, float[] columnWidths, int fontSize) {
    super(id, x, y, w, h);
    this.headers = headers;
    this.columnWidths = columnWidths;
    this.fontSize = fontSize;
    this.rowHeight = fontSize * ROW_HEIGHT_MULTIPLIER;
  }

  public void addRow(String[] data) {
    if (data.length == headers.length) rows.add(data);
  }

  public void clearRows() {
    rows.clear();
    selectedIndex = -1;
    internalScrollY = 0;
  }

  public int getRowCount() { return rows.size(); }
  public int getSelectedIndex() { return selectedIndex; }
  public String[] getSelectedRow() {
    return selectedIndex >= 0 && selectedIndex < rows.size() ? rows.get(selectedIndex) : null;
  }
  public void setSelectedIndex(int index) {
    selectedIndex = index >= 0 && index < rows.size() ? index : -1;
  }

  public boolean hasScrollableOverflow() {
    return rows.size() * rowHeight > (height - rowHeight);
  }

  public float minimumScroll() {
    return min(0, (height - rowHeight) - rows.size() * rowHeight);
  }

  public float scrollbarWidth() { return 6; }
  public float scrollbarTrackX() { return x + width - 11; }
  public float scrollbarTrackTop() { return y + rowHeight + 5; }
  public float scrollbarTrackHeight() { return max(0, height - rowHeight - 10); }
  public float scrollbarThumbHeight() {
    if (!hasScrollableOverflow()) return scrollbarTrackHeight();
    float contentHeight = rows.size() * rowHeight;
    float visibleHeight = height - rowHeight;
    return constrain(
      scrollbarTrackHeight() * visibleHeight / contentHeight,
      24,
      scrollbarTrackHeight()
    );
  }
  public float scrollbarThumbY() {
    float travel = scrollbarTrackHeight() - scrollbarThumbHeight();
    float minimum = minimumScroll();
    float progress = minimum == 0 ? 0 : constrain(internalScrollY / minimum, 0, 1);
    return scrollbarTrackTop() + travel * progress;
  }
  public boolean isPointInScrollbar(float mx, float my) {
    return hasScrollableOverflow() &&
      mx >= scrollbarTrackX() - 3 &&
      mx <= scrollbarTrackX() + scrollbarWidth() + 3 &&
      my >= scrollbarTrackTop() &&
      my <= scrollbarTrackTop() + scrollbarTrackHeight();
  }

  public void scrollToBottom() {
    float contentHeight = rows.size() * rowHeight;
    float visibleHeight = height - rowHeight;
    internalScrollY = min(0, visibleHeight - contentHeight);
  }

  public void draw() {
    if (!isVisible) return;
    pushStyle();
    fill(currentTheme.surfaceColor);
    stroke(currentTheme.borderColor);
    strokeWeight(1);
    rect(x, y + rowHeight, width, height - rowHeight, 0, 0, BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM);

    textSize(fontSize);
    textAlign(CENTER, CENTER);
    float visibleTop = y + rowHeight;
    float visibleBottom = y + height;

    for (int i = 0; i < rows.size(); i++) {
      float rowY = y + rowHeight + (i * rowHeight) + internalScrollY;
      float rowBottom = rowY + rowHeight;
      if (rowBottom < visibleTop || rowY > visibleBottom) continue;

      if (i == selectedIndex) {
        fill(currentTheme.accentColor, 40);
        noStroke();
        float drawY = max(rowY, visibleTop);
        float drawHeight = min(rowBottom, visibleBottom) - drawY;
        if (drawHeight > 0) rect(x + 1, drawY, width - 2, drawHeight);
      }

      String[] rowData = rows.get(i);
      float cellX = x;
      float textY = max(rowY + rowHeight * 0.48f, visibleTop + rowHeight * 0.48f);
      textY = min(textY, visibleBottom - rowHeight * 0.52f);
      for (int j = 0; j < rowData.length; j++) {
        float colW = width * columnWidths[j];
        if (rowY >= visibleTop - rowHeight * 0.5f && rowY <= visibleBottom - rowHeight * 0.5f) {
          fill(tableCellTextColor(rowData[j], i == selectedIndex));
          text(rowData[j], cellX + colW / 2, textY);
        }
        cellX += colW;
      }
      if (i < rows.size() - 1 && rowBottom >= visibleTop && rowBottom <= visibleBottom) {
        stroke(currentTheme.borderColor, 80);
        line(x, rowBottom, x + width, rowBottom);
      }
    }

    fill(currentTheme.accentColor);
    noStroke();
    rect(x, y, width, rowHeight, BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, 0, 0);
    float headerX = x;
    fill(255);
    for (int i = 0; i < headers.length; i++) {
      float colW = width * columnWidths[i];
      text(headers[i], headerX + colW / 2, y + rowHeight * 0.48f);
      headerX += colW;
    }

    if (hasScrollableOverflow()) {
      float trackX = scrollbarTrackX();
      float trackTop = scrollbarTrackTop();
      float trackHeight = scrollbarTrackHeight();
      float thumbY = scrollbarThumbY();
      float thumbHeight = scrollbarThumbHeight();
      float mx = getScaledMouseX();
      float my = getScaledMouseY();
      boolean hovered = isPointInScrollbar(mx, my);
      noStroke();
      fill(currentTheme.borderColor, 105);
      rect(trackX, trackTop, scrollbarWidth(), trackHeight, 3);
      fill(currentTheme.accentColor, hovered ? 230 : 165);
      rect(trackX, thumbY, scrollbarWidth(), thumbHeight, 3);
    }

    noFill();
    stroke(currentTheme.borderColor);
    strokeWeight(2);
    rect(x, y, width, height, BORDER_RADIUS_MEDIUM);
    popStyle();
  }

  public int tableCellTextColor(String value, boolean selected) {
    if (selected) return currentTheme.accentColor;
    if ("ACTIVE".equals(value)) return color(32, 132, 72);
    if ("BANNED".equals(value)) return color(195, 46, 46);
    if ("PENDING".equals(value)) return color(190, 116, 18);
    if ("DISABLED".equals(value)) return color(105, 112, 122);
    return currentTheme.textColor;
  }

  public void mousePressed() {
  }

  public void mouseDragged() {
    if (!isEnabled) return;
    float mx = getScaledMouseX();
    float my = getScaledMouseY();
    if (containsPoint(mx, my) && my > y + rowHeight && my < y + height) {
      if (isPointInScrollbar(mx, my)) {
        float travel = scrollbarTrackHeight() - scrollbarThumbHeight();
        float progress = travel <= 0
          ? 0
          : constrain(
            (my - scrollbarTrackTop() - scrollbarThumbHeight() * 0.5f) / travel,
            0,
            1
          );
        internalScrollY = minimumScroll() * progress;
      } else {
        internalScrollY += (mouseY - pmouseY) / uiScale;
        internalScrollY = constrain(internalScrollY, minimumScroll(), 0);
      }
    }
  }

  public void performTapAction(float mx, float my) {
    if (!isEnabled) return;
    if (containsPoint(mx, my) && my > y + rowHeight && my < y + height) {
      if (isPointInScrollbar(mx, my)) {
        float thumbTop = scrollbarThumbY();
        float thumbBottom = thumbTop + scrollbarThumbHeight();
        float page = height - rowHeight;
        if (my < thumbTop) internalScrollY += page;
        else if (my > thumbBottom) internalScrollY -= page;
        internalScrollY = constrain(internalScrollY, minimumScroll(), 0);
        return;
      }
      int index = floor((my - (y + rowHeight) - internalScrollY) / rowHeight);
      if (index >= 0 && index < rows.size()) {
        selectedIndex = index;
        triggerEvent(this, "rowSelected", rows.get(index));
      }
    }
  }
}
