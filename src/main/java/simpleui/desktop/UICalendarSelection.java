package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UICalendarSelection {
    public final UIDate date;
    public final ArrayList<UICalendarEvent> events;

    public UICalendarSelection(UIDate date, ArrayList<UICalendarEvent> events) {
        this.date = new UIDate(date);
        this.events = new ArrayList<UICalendarEvent>(events);
    }
}
