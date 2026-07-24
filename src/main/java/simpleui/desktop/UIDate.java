package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UIDate implements Comparable<UIDate> {
    public final int year;
    public final int month;
    public final int day;

    public UIDate(int year, int month, int day) {
        if (!isValidUIDate(year, month, day)) {
            throw new IllegalArgumentException("Invalid UIDate: " +
                year + "-" + month + "-" + day);
        }
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public UIDate(UIDate other) {
        this(other.year, other.month, other.day);
    }

    public int compareTo(UIDate other) {
        if (other == null) return 1;
        if (year != other.year) return year < other.year ? -1 : 1;
        if (month != other.month) return month < other.month ? -1 : 1;
        return day == other.day ? 0 : (day < other.day ? -1 : 1);
    }

    public boolean isBefore(UIDate other) { return compareTo(other) < 0; }
    public boolean isAfter(UIDate other) { return compareTo(other) > 0; }
    public boolean isEqual(UIDate other) { return compareTo(other) == 0; }
    public boolean isBeforeOrEqual(UIDate other) { return compareTo(other) <= 0; }
    public boolean isAfterOrEqual(UIDate other) { return compareTo(other) >= 0; }

    public long toUtcMillis() {
        return createUICalendar(year, month, day, 0, 0, 0, 0, "UTC").getTimeInMillis();
    }

    public long differenceInDays(UIDate other) {
        if (other == null) throw new IllegalArgumentException("UIDate is null");
        return (other.toUtcMillis() - toUtcMillis()) / UI_MILLIS_PER_DAY;
    }

    public long absoluteDifferenceInDays(UIDate other) {
        return absLong(differenceInDays(other));
    }

    public UIDate plusDays(int amount) {
        Calendar value = createUICalendar(year, month, day, 0, 0, 0, 0, "UTC");
        value.add(Calendar.DAY_OF_MONTH, amount);
        return new UIDate(value.get(Calendar.YEAR), value.get(Calendar.MONTH) + 1,
            value.get(Calendar.DAY_OF_MONTH));
    }

    public String toString() {
        return String.format(Locale.ROOT, "%04d-%02d-%02d", year, month, day);
    }
}
