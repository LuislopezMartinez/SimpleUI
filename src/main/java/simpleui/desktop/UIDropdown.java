package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UIDropdown extends UIElement {
  public String[] options;
  public int selectedIndex = 0;
  public int fontSize;
  public boolean isOpen = false;

  public UIDropdown(String id, int x, int y, int w, int h, String[] options, int fontSize) {
    super(id, x, y, w, h);
    this.options = options;
    this.fontSize = fontSize;
  }

  public boolean isOpen() { return isOpen; }
  public void setOpen(boolean nextOpen) { isOpen = nextOpen; }
  public boolean containsMainBox(float mx, float my) { return mx > x && mx < x + width && my > y && my < y + height; }
  public boolean containsOpenMenu(float mx, float my) { return isOpen && mx > x && mx < x + width && my > y + height && my < y + height + (options.length * height); }
  public String getSelectedValue() { return options[selectedIndex]; }
  public String[] getOptions() { return options.clone(); }
  public int getSelectedIndex() { return selectedIndex; }
  public void setOptions(String[] newOptions) {
    options = newOptions == null || newOptions.length == 0 ? new String[] { "Sin opciones" } : newOptions.clone();
    selectedIndex = constrain(selectedIndex, 0, options.length - 1);
    isOpen = false;
  }
  public void setSelectedIndex(int index) { if (index >= 0 && index < options.length) selectedIndex = index; }

  public boolean handleClickAt(float mx, float my) {
    if (containsMainBox(mx, my)) {
      isOpen = !isOpen;
      return true;
    }
    if (isOpen && containsOpenMenu(mx, my)) {
      int idx = floor((my - (y + height)) / height);
      if (idx >= 0 && idx < options.length) {
        selectedIndex = idx;
        triggerEvent(this, "changed", options[selectedIndex]);
      }
      isOpen = false;
      return true;
    }
    if (isOpen) {
      isOpen = false;
      return true;
    }
    return false;
  }

  public void draw() {
    if (!isVisible) return;
    pushStyle();
    float hoverMx = getScaledMouseX();
    float hoverMy = isAnchored()
      ? (mouseY - viewportOffsetY) / uiScaleY
      : (mouseY - viewportOffsetY - scrollState.currentY) / uiScaleY;
    int hoveredIndex = -1;
    if (isOpen && containsOpenMenu(hoverMx, hoverMy)) {
      hoveredIndex = floor((hoverMy - (y + height)) / height);
      if (hoveredIndex < 0 || hoveredIndex >= options.length) hoveredIndex = -1;
    }
    stroke(currentTheme.borderColor);
    strokeWeight(2);
    fill(currentTheme.surfaceColor);
    rect(x, y, width, height, BORDER_RADIUS_MEDIUM);
    fill(currentTheme.textColor);
    textAlign(LEFT, CENTER);
    textSize(fontSize);
    text(options.length == 0 ? "" : options[selectedIndex], x + 8, y + height * 0.5f);
    textAlign(CENTER, CENTER);
    text("v", x + width - 16, y + height * 0.5f);
    if (isOpen) {
      for (int i = 0; i < options.length; i++) {
        float rowY = y + height + i * height;
        stroke(currentTheme.borderColor);
        if (i == selectedIndex) {
          fill(currentTheme.accentColor);
        } else if (i == hoveredIndex) {
          fill(lerpColor(currentTheme.surfaceColor, currentTheme.accentColor, 0.18f));
        } else {
          fill(currentTheme.surfaceColor);
        }
        rect(x, rowY, width, height, 0);
        fill(i == selectedIndex ? color(255) : currentTheme.textColor);
        textAlign(LEFT, CENTER);
        text(options[i], x + 8, rowY + height * 0.5f);
      }
    }
    popStyle();
  }

  public void mouseReleased() {
    if (!isEnabled) return;
  }
}
