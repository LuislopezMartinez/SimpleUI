import simpleui.android.*;

UILabel title;
int secondsShown = -1;

void settings() {
  fullScreen(P2D);
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.RESPONSIVE);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  title = new UILabel("clock", 40, 100, 320, 64, "", 24);
  title.setTextAlignment(CENTER, CENTER);
  SimpleUI.addUIElement(title);
}

void draw() {
  int currentSecond = millis() / 1000;
  if (currentSecond != secondsShown) {
    secondsShown = currentSecond;
    title.setText("Segundos: " + secondsShown);
  }
}
