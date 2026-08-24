import simplecore.*;

Core core;
PFont font;
TimerDemo demo;

void settings() {
  Core.setVideoMode(this, 400, 700, P2D);
}

void setup() {
  core = Core.start(this, ViewportMode.FIT);
  font = createFont("SansSerif", 20);
  demo = new TimerDemo();
}

void draw() {
  background(24, 29, 40);
}

class TimerDemo extends Task {
  int seconds;
  int pulseColor = color(45, 210, 145);

  protected void frame() {
    if (timer("second", 1000)) seconds++;
    if (timer("color", 500)) {
      pulseColor = pulseColor == color(45, 210, 145)
        ? color(40, 130, 235) : color(45, 210, 145);
    }

    text(font, 20, "Segundos: " + seconds, CENTER,
      200, 325, color(245), 255);
    text(font, 15, "Dos timers independientes en el mismo Task", CENTER,
      200, 365, pulseColor, 255);
  }
}
