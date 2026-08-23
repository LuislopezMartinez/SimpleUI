import simpleui.android.*;

UICalendar courseCalendar;
UILabel selectedDateLabel;

void settings() {
  fullScreen(P2D);
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 17, UIScaleMode.RESPONSIVE);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  UIDate today = SimpleUI.getUIToday();
  courseCalendar = new UICalendar("calendar", 20, 45, 360, 410, today);
  courseCalendar.addEvent("class", today, "Clase de Processing");
  courseCalendar.addEvent("project", today.plusDays(3), "Entrega del proyecto");
  selectedDateLabel = new UILabel("selected", 20, 485, 360, 48, "Selecciona un dia", 16);
  selectedDateLabel.setTextAlignment(CENTER, CENTER);

  SimpleUI.addUIElement(courseCalendar);
  SimpleUI.addUIElement(selectedDateLabel);
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == courseCalendar && action.equals("dateSelected")) {
        UIDate date = courseCalendar.getSelectedDate();
        selectedDateLabel.setText("Fecha: " + date.day + "/" + date.month + "/" + date.year);
      }
    }
  });
}
