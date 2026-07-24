package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UIIndicator extends UIElement {
  public int indicatorColor = color(0);
  public boolean useCustomBorderColor = false;
  public int customBorderColor = color(0);

  public UIIndicator(String id, int x, int y, int size) {
    super(id, x, y, size, size);
  }

  public int getIndicatorColor() { return indicatorColor; }

  public void setIndicatorColor(int nextColor) { indicatorColor = nextColor; }
  public void setBorderColor(int nextBorderColor) { useCustomBorderColor = true; customBorderColor = nextBorderColor; }
  public void clearBorderColor() { useCustomBorderColor = false; }

  public void draw() {
    if (!isVisible) return;
    int borderColor = useCustomBorderColor ? customBorderColor : currentTheme.borderColor;
    stroke(borderColor);
    strokeWeight(2);
    fill(currentTheme.surfaceColor);
    rect(x, y, width, height, BORDER_RADIUS_SMALL);
    noStroke();
    fill(indicatorColor);
    rect(x + 4, y + 4, width - 8, height - 8, BORDER_RADIUS_SMALL);
  }
}
