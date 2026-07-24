package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UIConfig {
  public int backgroundColor;
  public int surfaceColor;
  public int textColor;
  public int accentColor;
  public int borderColor;
  public int placeholderColor;

  public UIConfig(int bg, int surface, int text, int accent, int border, int placeholder) {
    backgroundColor = bg;
    surfaceColor = surface;
    textColor = text;
    accentColor = accent;
    borderColor = border;
    placeholderColor = placeholder;
  }
}
