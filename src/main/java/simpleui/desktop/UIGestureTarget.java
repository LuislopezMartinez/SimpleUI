package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public enum UIGestureTarget {
  GESTURE_NONE,
  GESTURE_GLOBAL_SCROLL,
  GESTURE_ELEMENT_SCROLL,
  GESTURE_ELEMENT_ACTION
}
