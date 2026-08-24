import simpleui.android.*;

UILabel title;
int secondsShown = -1;

void settings() {
  SimpleUI.setVideoMode(this, 400, 800, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18,IScaleMode.RESPONSIVE);

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
