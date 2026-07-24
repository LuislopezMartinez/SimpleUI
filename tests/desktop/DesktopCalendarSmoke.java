import simpleui.desktop.*;

public class DesktopCalendarSmoke {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(SimpleUI.isUILeapYear(2000), "2000 must be leap year");
        check(!SimpleUI.isUILeapYear(1900), "1900 must not be leap year");
        check(SimpleUI.getUIDaysInMonth(2024, 2) == 29, "February 2024 must have 29 days");

        UIDate leapDay = new UIDate(2024, 2, 29);
        UIDate nextDay = leapDay.plusDays(1);
        check(nextDay.year == 2024 && nextDay.month == 3 && nextDay.day == 1,
            "plusDays must cross month boundaries");
        check(leapDay.differenceInDays(nextDay) == 1, "Date difference must be one day");

        UIDateTime start = new UIDateTime(2026, 7, 22, 10, 0, 0, 0, "UTC");
        UIDateTime end = new UIDateTime(2026, 7, 22, 11, 30, 0, 0, "UTC");
        check(start.difference(end).getTotalMinutes() == 90, "Duration must be 90 minutes");

        final int[] selectedEvents = {0};
        UICalendar calendar = new UICalendar("calendar", 0, 0, 350, 260,
            new UIDate(2026, 7, 1));
        SimpleUI.setUIEventHandler(new UIEventHandler() {
            public void onUIEvent(UIElement element, String action, Object data) {
                if (element == calendar && "dateSelected".equals(action) &&
                    data instanceof UICalendarSelection) selectedEvents[0]++;
            }
        });

        UICalendarEvent event = calendar.addEvent("event-1", new UIDate(2026, 7, 22), "Test");
        calendar.selectDate(2026, 7, 22);
        check(selectedEvents[0] == 1, "dateSelected must be emitted once");
        check(calendar.getEventCountForDate(2026, 7, 22) == 1, "Calendar event must be indexed");
        check(calendar.getSelectedDate().isEqual(new UIDate(2026, 7, 22)), "Selected date mismatch");
        event.setVisible(false);
        check(calendar.getEventCountForDate(2026, 7, 22) == 0, "Hidden event must not be counted");
        calendar.showNextMonth();
        check(calendar.getDisplayedMonth() == 8, "Month navigation failed");

        System.out.println("Desktop calendar model and events passed.");
    }
}
