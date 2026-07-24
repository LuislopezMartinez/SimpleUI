package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class ChatAreaMessage {
  public int side = LEFT;
  public String text = "";
  public String footer = "";
  public boolean readReceipt = false;
}
