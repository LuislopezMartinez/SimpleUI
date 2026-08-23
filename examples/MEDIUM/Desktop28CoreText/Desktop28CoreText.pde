import simplecore.*;

Core taskCore;
PFont gameFont;

void settings() {
  size(560, 700, P2D);
}

void setup() {
  taskCore = Core.start(this);
  taskCore.setMode(400, 700, ViewportMode.FIT);
  gameFont = createFont("SansSerif", 24);
  new TextExample();
}

void draw() {
  background(25, 31, 46);
}

class TextExample extends Task {
  protected void frame() {
    float pulse = 140 + sin(liveFrames * 0.05) * 115;

    text(gameFont, 28, "Task.text()",
      CENTER, 200, 70, color(255, 205, 70), 255);

    text(gameFont, 20, "Alineado a la izquierda",
      LEFT, 30, 180, color(240), 255);
    text(gameFont, 20, "Centrado",
      CENTER, 200, 280, color(100, 210, 255), 255);
    text(gameFont, 20, "Alineado a la derecha",
      RIGHT, 370, 380, color(150, 255, 170), 255);

    text(gameFont, 22, "Alpha animado",
      CENTER, 200, 500, color(255, 130, 190), pulse);
    text(gameFont, 16, "Fotograma: " + liveFrames,
      CENTER, 200, 610, color(190), 255);
  }
}
