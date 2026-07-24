package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UICalendar extends UIElement {
    final String[] MONTH_NAMES = { "ENERO", "FEBRERO", "MARZO", "ABRIL",
        "MAYO", "JUNIO", "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE",
        "NOVIEMBRE", "DICIEMBRE" };
    public final String[] WEEKDAY_NAMES = { "LU", "MA", "MI", "JU", "VI", "S\u00C1", "DO" };

    public int displayedYear;
    public int displayedMonth;
    public UIDate selectedDate;
    public ArrayList<UICalendarEvent> events = new ArrayList<UICalendarEvent>();
    public int viewRevision = 0;
    public int eventsRevision = 0;

    public UICalendar(String id, int x, int y, int w, int h) {
        this(id, x, y, w, h, getUIToday());
    }

    public UICalendar(String id, int x, int y, int w, int h, UIDate initialDate) {
        super(id, x, y, w, h);
        UIDate date = initialDate == null ? getUIToday() : initialDate;
        displayedYear = date.year;
        displayedMonth = date.month;
    }

    public void draw() {
        if (!isVisible) return;
        float headerHeight = constrain(height * 0.17f, 38.0f, 48.0f);
        float weekdayHeight = 24;
        float gridTop = y + headerHeight + weekdayHeight;
        float cellWidth = width / 7.0f;
        float cellHeight = (height - headerHeight - weekdayHeight) / 6.0f;

        pushStyle();
        stroke(currentTheme.borderColor);
        strokeWeight(1.5f);
        fill(currentTheme.surfaceColor);
        rect(x, y, width, height, BORDER_RADIUS_MEDIUM);

        noStroke();
        fill(lerpColor(currentTheme.surfaceColor, currentTheme.accentColor, 0.055f));
        rect(x, y, width, headerHeight, BORDER_RADIUS_MEDIUM);

        fill(currentTheme.textColor);
        textAlign(CENTER, CENTER);
        textSize(17);
        text(MONTH_NAMES[displayedMonth - 1] + " " + displayedYear,
            x + width * 0.5f, y + headerHeight * 0.5f);

        textSize(22);
        text("<<", x + 20, y + headerHeight * 0.5f);
        text("<", x + 54, y + headerHeight * 0.5f);
        text(">", x + width - 54, y + headerHeight * 0.5f);
        text(">>", x + width - 20, y + headerHeight * 0.5f);

        textSize(10);
        for (int column = 0; column < 7; column++) {
            fill(currentTheme.placeholderColor);
            text(WEEKDAY_NAMES[column], x + (column + 0.5f) * cellWidth,
                y + headerHeight + weekdayHeight * 0.5f);
        }

        UIDate firstVisible = getFirstVisibleDate();
        UIDate today = getUIToday();
        for (int index = 0; index < 42; index++) {
            UIDate date = firstVisible.plusDays(index);
            int row = index / 7;
            int column = index % 7;
            float cellX = x + column * cellWidth;
            float cellY = gridTop + row * cellHeight;
            boolean inMonth = date.year == displayedYear && date.month == displayedMonth;
            boolean selected = selectedDate != null && selectedDate.isEqual(date);
            boolean isToday = today.isEqual(date);

            if (selected) {
                noStroke();
                fill(currentTheme.accentColor);
                ellipse(cellX + cellWidth * 0.5f, cellY + cellHeight * 0.42f,
                    min(cellWidth, cellHeight) * 0.68f, min(cellWidth, cellHeight) * 0.68f);
            } else if (isToday) {
                noFill();
                stroke(currentTheme.accentColor);
                strokeWeight(1.5f);
                ellipse(cellX + cellWidth * 0.5f, cellY + cellHeight * 0.42f,
                    min(cellWidth, cellHeight) * 0.68f, min(cellWidth, cellHeight) * 0.68f);
            }

            noStroke();
            fill(selected ? color(255) :
                (inMonth ? currentTheme.textColor : currentTheme.placeholderColor));
            textAlign(CENTER, CENTER);
            textSize(12);
            text(date.day, cellX + cellWidth * 0.5f, cellY + cellHeight * 0.42f);

            int eventCount = getEventCountForDate(date);
            int dots = min(3, eventCount);
            for (int marker = 0; marker < dots; marker++) {
                UICalendarEvent event = getVisibleEventForDate(date, marker);
                float markerX = cellX + cellWidth * 0.5f + (marker - (dots - 1) * 0.5f) * 7;
                fill(event == null ? currentTheme.accentColor : event.getColor());
                ellipse(markerX, cellY + cellHeight - 7, 4.5f, 4.5f);
            }
            if (eventCount > 3) {
                fill(currentTheme.placeholderColor);
                textAlign(RIGHT, TOP);
                textSize(7);
                text("+" + (eventCount - 3), cellX + cellWidth - 3, cellY + 2);
            }
        }
        popStyle();
    }

    public void performTapAction(float mx, float my) {
        if (!isEnabled || !containsPoint(mx, my)) return;
        float headerHeight = constrain(height * 0.17f, 38.0f, 48.0f);
        if (my <= y + headerHeight) {
            float localX = mx - x;
            if (localX < 36) showPreviousYear();
            else if (localX < 72) showPreviousMonth();
            else if (localX >= width - 36) showNextYear();
            else if (localX >= width - 72) showNextMonth();
            return;
        }

        float weekdayHeight = 24;
        float gridTop = y + headerHeight + weekdayHeight;
        if (my < gridTop) return;
        float cellWidth = width / 7.0f;
        float cellHeight = (height - headerHeight - weekdayHeight) / 6.0f;
        int column = floor((mx - x) / cellWidth);
        int row = floor((my - gridTop) / cellHeight);
        if (column < 0 || column > 6 || row < 0 || row > 5) return;
        selectDate(getFirstVisibleDate().plusDays(row * 7 + column));
    }

    public void setDisplayedMonth(int year, int month) {
        if (year < 1 || month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid displayed month");
        }
        applyDisplayedMonth(year, month);
    }

    public void showPreviousMonth() { navigateMonths(-1); }
    public void showNextMonth() { navigateMonths(1); }
    public void showPreviousYear() { applyDisplayedMonth(displayedYear - 1, displayedMonth); }
    public void showNextYear() { applyDisplayedMonth(displayedYear + 1, displayedMonth); }

    public void showToday() {
        UIDate today = getUIToday();
        applyDisplayedMonth(today.year, today.month);
        selectDate(today);
    }

    public int getDisplayedYear() { return displayedYear; }
    public int getDisplayedMonth() { return displayedMonth; }
    public UIDate getDisplayedDate() { return new UIDate(displayedYear, displayedMonth, 1); }
    public int getViewRevision() { return viewRevision; }
    public int getEventsRevision() { return eventsRevision; }

    public void selectDate(int year, int month, int day) {
        selectDate(new UIDate(year, month, day));
    }

    public void selectDate(UIDate date) {
        if (date == null) return;
        if (date.year != displayedYear || date.month != displayedMonth) {
            applyDisplayedMonth(date.year, date.month);
        }
        selectedDate = new UIDate(date);
        viewRevision++;
        triggerEvent(this, "dateSelected",
            new UICalendarSelection(selectedDate, getEventsForDate(selectedDate)));
    }

    public void setSelectedDate(int year, int month, int day) {
        selectDate(year, month, day);
    }

    public void setSelectedDate(UIDate date) {
        selectDate(date);
    }

    public void clearSelection() {
        if (selectedDate == null) return;
        selectedDate = null;
        viewRevision++;
        triggerEvent(this, "selectionCleared", null);
    }

    public boolean hasSelectedDate() { return selectedDate != null; }
    public UIDate getSelectedDate() { return selectedDate == null ? null : new UIDate(selectedDate); }
    public int getSelectedYear() { return selectedDate == null ? 0 : selectedDate.year; }
    public int getSelectedMonth() { return selectedDate == null ? 0 : selectedDate.month; }
    public int getSelectedDay() { return selectedDate == null ? 0 : selectedDate.day; }
    public boolean isSelected(UIDate date) {
        return selectedDate != null && date != null && selectedDate.isEqual(date);
    }
    public boolean isToday(UIDate date) { return date != null && getUIToday().isEqual(date); }

    public boolean isDateVisible(UIDate date) {
        if (date == null) return false;
        UIDate first = getFirstVisibleDate();
        UIDate last = first.plusDays(41);
        return date.isAfterOrEqual(first) && date.isBeforeOrEqual(last);
    }

    public UICalendarEvent addEvent(String id, int year, int month, int day, String title) {
        return addEvent(id, new UIDate(year, month, day), title);
    }

    public UICalendarEvent addEvent(String id, UIDate date, String title) {
        return addEvent(id, date, date, title);
    }

    public UICalendarEvent addEvent(String id, UIDate startDate, UIDate endDate, String title) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Calendar event dates are required");
        }
        String zone = TimeZone.getDefault().getID();
        UIDateTime start = new UIDateTime(startDate.year, startDate.month, startDate.day,
            0, 0, 0, 0, zone);
        UIDateTime end = new UIDateTime(endDate.year, endDate.month, endDate.day,
            23, 59, 59, 999, zone);
        return addEvent(id, start, end, title);
    }

    public UICalendarEvent addEvent(String id, UIDateTime start, UIDateTime end, String title) {
        if (hasEvent(id)) throw new IllegalArgumentException("Duplicate calendar event id: " + id);
        UICalendarEvent event = new UICalendarEvent(this, id, start, end, title);
        events.add(event);
        touchEvents();
        return event;
    }

    public boolean hasEvent(String id) { return getEvent(id) != null; }

    public UICalendarEvent getEvent(String id) {
        if (id == null) return null;
        for (UICalendarEvent event : events) {
            if (event.getId().equals(id)) return event;
        }
        return null;
    }

    public boolean removeEvent(String id) {
        UICalendarEvent event = getEvent(id);
        if (event == null) return false;
        events.remove(event);
        touchEvents();
        return true;
    }

    public void clearEvents() {
        if (events.size() == 0) return;
        events.clear();
        touchEvents();
    }

    public ArrayList<UICalendarEvent> getEventsForDate(int year, int month, int day) {
        return getEventsForDate(new UIDate(year, month, day));
    }

    public ArrayList<UICalendarEvent> getEventsForDate(UIDate date) {
        ArrayList<UICalendarEvent> result = new ArrayList<UICalendarEvent>();
        for (UICalendarEvent event : events) {
            if (event.isVisible() && event.occursOn(date)) result.add(event);
        }
        Collections.sort(result, new UICalendarEventDateComparator());
        return result;
    }

    public int getEventCountForDate(UIDate date) {
        int count = 0;
        for (UICalendarEvent event : events) {
            if (event.isVisible() && event.occursOn(date)) count++;
        }
        return count;
    }

    public int getEventCountForDate(int year, int month, int day) {
        return getEventCountForDate(new UIDate(year, month, day));
    }

    public boolean hasEventsForDate(UIDate date) { return getEventCountForDate(date) > 0; }
    public boolean hasEventsForDate(int year, int month, int day) {
        return hasEventsForDate(new UIDate(year, month, day));
    }

    public ArrayList<UICalendarEvent> getEventsForDisplayedMonth() {
        UIDate first = new UIDate(displayedYear, displayedMonth, 1);
        UIDate last = new UIDate(displayedYear, displayedMonth,
            getUIDaysInMonth(displayedYear, displayedMonth));
        ArrayList<UICalendarEvent> result = new ArrayList<UICalendarEvent>();
        for (UICalendarEvent event : events) {
            if (!event.isVisible()) continue;
            UIDate eventStart = event.getStart().getDate();
            UIDate eventEnd = event.getEnd().getDate();
            if (!eventEnd.isBefore(first) && !eventStart.isAfter(last)) result.add(event);
        }
        Collections.sort(result, new UICalendarEventDateComparator());
        return result;
    }

    public UICalendarEvent getVisibleEventForDate(UIDate date, int requestedIndex) {
        int currentIndex = 0;
        for (UICalendarEvent event : events) {
            if (!event.isVisible() || !event.occursOn(date)) continue;
            if (currentIndex == requestedIndex) return event;
            currentIndex++;
        }
        return null;
    }

    public UIDate getFirstVisibleDate() {
        UIDate first = new UIDate(displayedYear, displayedMonth, 1);
        Calendar value = createUICalendar(first.year, first.month, first.day,
            0, 0, 0, 0, "UTC");
        int mondayIndex = (value.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        return first.plusDays(-mondayIndex);
    }

    public void navigateMonths(int amount) {
        Calendar value = createUICalendar(displayedYear, displayedMonth, 1,
            0, 0, 0, 0, "UTC");
        value.add(Calendar.MONTH, amount);
        applyDisplayedMonth(value.get(Calendar.YEAR), value.get(Calendar.MONTH) + 1);
    }

    public void applyDisplayedMonth(int year, int month) {
        if (year < 1 || (year == displayedYear && month == displayedMonth)) return;
        int previousYear = displayedYear;
        int previousMonth = displayedMonth;
        displayedYear = year;
        displayedMonth = month;
        viewRevision++;
        UIDate displayed = new UIDate(year, month, 1);
        if (previousYear != year) triggerEvent(this, "yearChanged", displayed);
        if (previousMonth != month) triggerEvent(this, "monthChanged", displayed);
        triggerEvent(this, "viewChanged", displayed);
    }

    public void touchEvents() { eventsRevision++; }
}
