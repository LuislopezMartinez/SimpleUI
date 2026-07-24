import simpleui.desktop.*;

UICalendar calendar;
UILabel selectionLabel;

void settings() {
  size(720, 520);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(720, 520);
  calendar = new UICalendar("calendar", 40, 40, 430, 360);
  selectionLabel = new UILabel("selection", 40, 425, 620, 40, "Select a date", 18);
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

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
