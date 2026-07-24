package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public abstract class UIElement {
  public int x, y, width, height;
  public String id;
  public boolean isVisible = true;
  public boolean isEnabled = true;
  public boolean anchored = false;

  public UIElement(String id, int x, int y, int w, int h) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.width = w;
    this.height = h;
  }

  public abstract void draw();
  public void mousePressed() {}
  public void mouseReleased() {}
  public void mouseDragged() {}
  public void keyPressed() {}
  public void keyTyped() {}
  public void performTapAction(float mx, float my) {}
  public boolean hasScrollableOverflow() { return false; }
  public void setPosition(int x, int y) { this.x = x; this.y = y; }
  public void setSize(int w, int h) { this.width = w; this.height = h; }
  public void setVisible(boolean visible) { this.isVisible = visible; }
  public void setEnabled(boolean enabled) { this.isEnabled = enabled; }
  public void setAnchored(boolean anchored) { this.anchored = anchored; }
  public boolean isAnchored() { return anchored; }
  public boolean containsPoint(float px, float py) { return isPointInElement(px, py, this); }
}
