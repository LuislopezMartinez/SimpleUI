package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UICalendarEventDateComparator implements Comparator<UICalendarEvent> {
    public int compare(UICalendarEvent a, UICalendarEvent b) {
        if (a == b) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        int startResult = a.getStart().compareTo(b.getStart());
        if (startResult != 0) return startResult;
        int endResult = a.getEnd().compareTo(b.getEnd());
        if (endResult != 0) return endResult;
        return a.getId().compareToIgnoreCase(b.getId());
    }
}
