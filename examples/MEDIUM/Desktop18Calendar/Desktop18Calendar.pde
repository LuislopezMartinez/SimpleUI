import simpleui.desktop.*;

UICalendar courseCalendar;
UILabel selectedDateLabel;

void settings() {
  SimpleUI.setVideoMode(this, 560, 620, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 17, UIScaleMode.FIT);

  UIDate today = SimpleUI.getUIToday();
  courseCalendar = new UICalendar("calendar", 40, 40, 480, 450, today);
  courseCalendar.addEvent("class", today, "Clase de Processing");
  courseCalendar.addEvent("project", today.plusDays(3), "Entrega del proyecto");
  selectedDateLabel = new UILabel("selected", 40, 520, 480, 44, "Selecciona un dia", 16);
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
