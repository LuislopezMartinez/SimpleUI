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
  Core.setVideoMode(this, 400, 700, P2D);
}

void setup() {
  taskCore = Core.start(this, ViewportMode.FIT);
  gameFont = createFont("SansSerif", 22);
  new CounterTask();
}

void draw() {
  background(28);
}
