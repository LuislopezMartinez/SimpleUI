package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UIProgressBar extends UIElement {
  public float progressValue = 0.0f;
  public int trackColor = color(222, 227, 233);
  public int fillColor = color(24, 118, 210);

  public UIProgressBar(String id, int x, int y, int w, int h) {
    super(id, x, y, w, h);
  }

  public void setProgress(float value) { progressValue = constrain(value, 0.0f, 1.0f); }
  public float getProgress() { return progressValue; }
  public void setColors(int track, int fill) { trackColor = track; fillColor = fill; }

  public void draw() {
    if (!isVisible) return;
    pushStyle();
    noStroke();
    fill(trackColor);
    rect(x, y, width, height, height * 0.5f);
    float filledWidth = width * progressValue;
    if (filledWidth > 0.0f) {
      fill(fillColor);
      rect(x, y, filledWidth, height, height * 0.5f);
    }
    popStyle();
  }
}
