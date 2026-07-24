package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UIRect {
  public float x, y, width, height;

  public UIRect(float x, float y, float width, float height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public float centerX() { return x + width * 0.5f; }
  public float centerY() { return y + height * 0.5f; }
}
