import simpleui.android.*;

UICalendar calendar;
UILabel selectionLabel;

void settings() {
  SimpleUI.setVideoMode(this, 400, 800, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.RESPONSIVE);
  calendar = new UICalendar("calendar", 20, 60, 360, 300);
  selectionLabel = new UILabel("selection", 20, 385, 360, 70, "Select a date", 18);
  calendar.addEvent("demo", SimpleUI.getUIToday(), "Today").setColor(color(24, 118, 210));
  SimpleUI.addUIElement(calendar);
  SimpleUI.addUIElement(selectionLabel);
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == calendar && action.equals("dateSelected")) {
        UICalendarSelection selection = (UICalendarSelection)data;
        selectionLabel.setText("Selected: " + selection.date +
          " · Events: " + selection.events.size());
      }
    }
  });
}
