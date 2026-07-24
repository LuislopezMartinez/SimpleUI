import simpleui.desktop.*;

UILabel title;
int secondsShown = -1;

void settings() {
  size(400, 240);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(400, 240);

  title = new UILabel("clock", 40, 80, 320, 60, "", 24);
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
