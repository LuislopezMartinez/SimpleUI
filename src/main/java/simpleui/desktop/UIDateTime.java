package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UIDateTime implements Comparable<UIDateTime> {
    public final int year;
    public final int month;
    public final int day;
    public final int hour;
    public final int minute;
    public final int second;
    public final int millisecond;
    public final String timeZoneId;

    public UIDateTime(int year, int month, int day, int hour, int minute, int second) {
        this(year, month, day, hour, minute, second, 0, TimeZone.getDefault().getID());
    }

    public UIDateTime(int year, int month, int day, int hour, int minute, int second,
        int millisecond, String timeZoneId) {
        if (!isValidUIDate(year, month, day) || hour < 0 || hour > 23 ||
            minute < 0 || minute > 59 || second < 0 || second > 59 ||
            millisecond < 0 || millisecond > 999) {
            throw new IllegalArgumentException("Invalid UIDateTime");
        }
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millisecond = millisecond;
        this.timeZoneId = timeZoneId == null || timeZoneId.length() == 0 ?
            TimeZone.getDefault().getID() : timeZoneId;
        createUICalendar(year, month, day, hour, minute, second, millisecond, this.timeZoneId);
    }

    public UIDate getDate() { return new UIDate(year, month, day); }

    public long toEpochMilliseconds() {
        return createUICalendar(year, month, day, hour, minute, second,
            millisecond, timeZoneId).getTimeInMillis();
    }

    public int compareTo(UIDateTime other) {
        if (other == null) return 1;
        return Long.compare(toEpochMilliseconds(), other.toEpochMilliseconds());
    }

    public boolean isBefore(UIDateTime other) { return compareTo(other) < 0; }
    public boolean isAfter(UIDateTime other) { return compareTo(other) > 0; }
    public boolean isEqual(UIDateTime other) { return compareTo(other) == 0; }
    public boolean isBeforeOrEqual(UIDateTime other) { return compareTo(other) <= 0; }
    public boolean isAfterOrEqual(UIDateTime other) { return compareTo(other) >= 0; }

    public UITimeSpan difference(UIDateTime other) {
        if (other == null) throw new IllegalArgumentException("UIDateTime is null");
        return new UITimeSpan(other.toEpochMilliseconds() - toEpochMilliseconds());
    }

    public long differenceInMilliseconds(UIDateTime other) {
        return difference(other).getTotalMilliseconds();
    }

    public String toString() {
        return String.format(Locale.ROOT, "%04d-%02d-%02d %02d:%02d:%02d [%s]",
            year, month, day, hour, minute, second, timeZoneId);
    }
}
