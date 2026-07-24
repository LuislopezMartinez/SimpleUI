package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public abstract class UITextInputBase extends UIElement {
  public UITextInputBase(String id, int x, int y, int w, int h) {
    super(id, x, y, w, h);
  }

  public abstract String getText();
  public abstract String getOverlayHint();
  public abstract void setText(String t);
  public abstract void setFocused(boolean f);
  public abstract boolean isFocused();
  public abstract void submitAndCloseKeyboard();
  public abstract boolean isUsingNativeKeyboardBridge();
}
