package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UISlider extends UIElement {
  public float minValue;
  public float maxValue;
  public float currentValue;
  public boolean isDragging = false;
  public boolean wasDragging = false;
  public int fontSize;

  public UISlider(String id, int x, int y, int w, int h, float minValue, float maxValue, float initialValue, int fontSize) {
    super(id, x, y, w, h);
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.currentValue = constrain(initialValue, minValue, maxValue);
    this.fontSize = fontSize;
  }

  public float getValue() { return currentValue; }
  public void setValue(float value) { currentValue = constrain(value, minValue, maxValue); }

  public void draw() {
    if (!isVisible) return;
    pushStyle();
    stroke(currentTheme.borderColor);
    strokeWeight(2);
    line(x, y + height / 2, x + width, y + height / 2);
    float ratio = map(currentValue, minValue, maxValue, 0, 1);
    float handleX = x + ratio * width;
    fill(currentTheme.accentColor);
    stroke(currentTheme.accentColor);
    ellipse(handleX, y + height / 2, height * SLIDER_HANDLE_RADIUS_RATIO, height * SLIDER_HANDLE_RADIUS_RATIO);
    if (isEnabled) {
      fill(currentTheme.textColor);
      textAlign(CENTER, CENTER);
      textSize(fontSize);
      text(nf(currentValue, 0, 2), x + width / 2, y - fontSize);
    }
    popStyle();
  }

  public void update() {
    if (isEnabled && isDragging) updateFromMouse(getScaledMouseX());
  }

  public void mousePressed() {
  }

  public void beginGestureDrag() {
    if (!isEnabled) return;
    isDragging = true;
    wasDragging = true;
    updateFromMouse(getScaledMouseX());
  }

  public void mouseDragged() {
    if (!isEnabled || !isDragging) return;
    updateFromMouse(getScaledMouseX());
  }

  public void mouseReleased() {
    if (isDragging) {
      isDragging = false;
      triggerEvent(this, "released", currentValue);
    }
    wasDragging = false;
  }

  public void forceRelease() {
    if (isDragging) {
      isDragging = false;
      triggerEvent(this, "released", currentValue);
    }
    wasDragging = false;
  }

  public void updateFromMouse(float mx) {
    float oldValue = currentValue;
    float ratio = constrain((mx - x) / width, 0, 1);
    currentValue = lerp(minValue, maxValue, ratio);
    if (abs(oldValue - currentValue) > 0.001f) triggerEvent(this, "changed", currentValue);
  }
}
