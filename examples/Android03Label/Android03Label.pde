import simpleui.android.*;

UILabel title;
int secondsShown = -1;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  title = new UILabel("clock", 40, 100, 320, 64, "", 24);
  title.setTextAlignment(CENTER, CENTER);
  SimpleUI.addUIElement(title);
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  int currentSecond = millis() / 1000;
  if (currentSecond != secondsShown) {
    secondsShown = currentSecond;
    title.setText("Segundos: " + secondsShown);
  }
  SimpleUI.updateAndDrawUI();
}
