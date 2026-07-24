package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UITimeSpan {
    public final long totalMilliseconds;

    public UITimeSpan(long totalMilliseconds) {
        this.totalMilliseconds = totalMilliseconds;
    }

    public long getTotalMilliseconds() { return totalMilliseconds; }
    public long getTotalSeconds() { return totalMilliseconds / UI_MILLIS_PER_SECOND; }
    public long getTotalMinutes() { return totalMilliseconds / UI_MILLIS_PER_MINUTE; }
    public double getTotalHours() { return totalMilliseconds / (double)UI_MILLIS_PER_HOUR; }
    public double getTotalDays() { return totalMilliseconds / (double)UI_MILLIS_PER_DAY; }
    public boolean isNegative() { return totalMilliseconds < 0; }
    public UITimeSpan absolute() { return new UITimeSpan(absLong(totalMilliseconds)); }

    public long getDaysPart() { return absLong(totalMilliseconds) / UI_MILLIS_PER_DAY; }
    public int getHoursPart() {
        return (int)((absLong(totalMilliseconds) % UI_MILLIS_PER_DAY) / UI_MILLIS_PER_HOUR);
    }
    public int getMinutesPart() {
        return (int)((absLong(totalMilliseconds) % UI_MILLIS_PER_HOUR) / UI_MILLIS_PER_MINUTE);
    }
    public int getSecondsPart() {
        return (int)((absLong(totalMilliseconds) % UI_MILLIS_PER_MINUTE) / UI_MILLIS_PER_SECOND);
    }
}
