import simplecore.*;

Core taskCore;
PFont gameFont;

class CounterTask extends Task {
  protected void frame() {
    text(gameFont, 22, "Task frame " + liveFrames,
      CENTER, 200, 350, color(240), 255);
  }
}

void settings() {
  size(560, 700, P2D);
}

void setup() {
  taskCore = Core.start(this);
  taskCore.setMode(400, 700, ViewportMode.FIT);
  gameFont = createFont("SansSerif", 22);
  new CounterTask();
}

void draw() {
  background(28);
}
