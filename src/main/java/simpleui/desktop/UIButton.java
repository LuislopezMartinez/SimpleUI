package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

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

  public void setLabel(String newLabel) { if (newLabel != null) label = newLabel; }

  public void setColors(int fillColor, int borderColor, int textColor) {
    useCustomColors = true;
    customFillColor = fillColor;
    customBorderColor = borderColor;
    customTextColor = textColor;
  }

  public void clearCustomColors() { useCustomColors = false; }

  public void setPressedVisual(boolean pressed) {
    isPressed = pressed;
  }

  public boolean isPressedState() { return isPressed; }

  public void draw() {
    if (!isVisible) return;
    pushStyle();
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
      borderC = color(210, 214, 220);
      fillC = color(235, 237, 240);
      textC = color(148, 154, 162);
    }
    stroke(borderC);
    fill(fillC);
    rect(x, y, width, height, BORDER_RADIUS_LARGE);
    fill(textC);
    textAlign(CENTER, CENTER);
    textSize(fontSize);
    text(label, x + width / 2, y + height / 2);
    popStyle();
  }

  public void mousePressed() {
  }

  public void performTapAction() {
    if (!isEnabled) return;
    isPressed = true;
    triggerEvent(this, "clicked", null);
  }

  public void mouseReleased() {
    isPressed = false;
  }
}
