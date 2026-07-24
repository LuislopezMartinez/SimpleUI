package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UICheckbox extends UIElement {
  public String label;
  public int fontSize;
  public boolean isChecked = false;

  public UICheckbox(String id, int x, int y, int size, String label, int fontSize) {
    super(id, x, y, size, size);
    this.label = label;
    this.fontSize = fontSize;
  }

  public boolean isChecked() { return isChecked; }
  public void setChecked(boolean checked) { isChecked = checked; }

  public void draw() {
    if (!isVisible) return;
    stroke(isChecked ? currentTheme.accentColor : currentTheme.borderColor);
    fill(currentTheme.surfaceColor);
    rect(x, y, height, height, BORDER_RADIUS_SMALL);
    if (isChecked) {
      fill(currentTheme.accentColor);
      noStroke();
      rect(x + 5, y + 5, height - 10, height - 10, 2);
    }
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
    if (mx > x && mx < x + height + 20 + labelWidth && my > y && my < y + height) {
      isChecked = !isChecked;
      triggerEvent(this, "changed", isChecked);
    }
  }

  public void performTapAction(float mx, float my) {
    if (!isEnabled) return;
    float labelWidth = textWidth(label);
    if (mx > x && mx < x + height + 20 + labelWidth && my > y && my < y + height) {
      isChecked = !isChecked;
      triggerEvent(this, "changed", isChecked);
    }
  }
}
