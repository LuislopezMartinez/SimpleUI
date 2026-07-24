package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UILabel extends UIElement {
  public String textContent;
  public int fontSize;
  public int textAlignHorizontal = CENTER;
  public int textAlignVertical = CENTER;

  public UILabel(String id, int x, int y, int w, int h, String textContent, int fontSize) {
    super(id, x, y, w, h);
    this.textContent = textContent;
    this.fontSize = fontSize;
  }

  public void setText(String text) { this.textContent = text; }
  public void setTextAlignment(int horizontal, int vertical) { this.textAlignHorizontal = horizontal; this.textAlignVertical = vertical; }

  public void draw() {
    if (!isVisible) return;
    fill(currentTheme.textColor);
    textAlign(textAlignHorizontal, textAlignVertical);
    textSize(fontSize);
    text(textContent, x, y, width, height);
  }
}
