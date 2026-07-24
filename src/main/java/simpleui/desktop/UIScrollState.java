package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UIScrollState {
  public float currentY = 0;
  public float targetY = 0;

  public void update() {
    currentY = lerp(currentY, targetY, SCROLL_LERP_SPEED);
  }

  public void reset() {
  }
}
