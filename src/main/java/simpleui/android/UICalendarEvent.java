package simpleui.android;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.android.SimpleUI.*;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.text.TextWatcher;
import android.text.Editable;
import android.graphics.Rect;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.os.Build;

public class UICalendarEvent {
    public UICalendar owner;
    public String id;
    public String title;
    public UIDateTime start;
    public UIDateTime end;
    public int eventColor = 0xFF1876D2;
    public Object data;
    public boolean visible = true;

    public UICalendarEvent(UICalendar owner, String id, UIDateTime start,
        UIDateTime end, String title) {
        if (id == null || id.trim().length() == 0) {
            throw new IllegalArgumentException("UICalendarEvent id is required");
        }
        if (start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException("Invalid UICalendarEvent interval");
        }
        this.owner = owner;
        this.id = id.trim();
        this.start = start;
        this.end = end;
        this.title = title == null ? "" : title;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public UIDateTime getStart() { return start; }
    public UIDateTime getEnd() { return end; }
    public int getColor() { return eventColor; }
    public Object getData() { return data; }
    public boolean isVisible() { return visible; }

    public UICalendarEvent setTitle(String value) {
        title = value == null ? "" : value;
        touch();
        return this;
    }

    public UICalendarEvent setColor(int value) {
        eventColor = value;
        touch();
        return this;
    }

    public UICalendarEvent setData(Object value) {
        data = value;
        touch();
        return this;
    }

    public UICalendarEvent setVisible(boolean value) {
        visible = value;
        touch();
        return this;
    }

    public UICalendarEvent setInterval(UIDateTime newStart, UIDateTime newEnd) {
        if (newStart == null || newEnd == null || newStart.isAfter(newEnd)) {
            throw new IllegalArgumentException("Invalid UICalendarEvent interval");
        }
        start = newStart;
        end = newEnd;
        touch();
        return this;
    }

    public boolean occursOn(UIDate date) {
        return date != null && date.isAfterOrEqual(start.getDate()) &&
            date.isBeforeOrEqual(end.getDate());
    }

    public boolean hasStarted(UIDateTime now) { return now != null && !now.isBefore(start); }
    public boolean hasFinished(UIDateTime now) { return now != null && now.isAfter(end); }
    public boolean isActiveAt(UIDateTime now) {
        return now != null && !now.isBefore(start) && !now.isAfter(end);
    }
    public UITimeSpan getDuration() { return start.difference(end); }

    public void touch() {
        if (owner != null) owner.touchEvents();
    }
}
