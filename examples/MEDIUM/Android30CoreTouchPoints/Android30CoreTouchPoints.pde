import simplecore.*;

Core core;
PFont font;

void settings() {
  fullScreen(P2D);
}

void setup() {
  orientation(PORTRAIT);
  core = Core.start(this);
  core.setMode(400, 700, ViewportMode.FIT);
  font = createFont("SansSerif", 18);
}

void draw() {
  background(24, 29, 40);
  pushMatrix();
  translate(Viewport.getOffsetX(), Viewport.getOffsetY());
  scale(Viewport.getScaleX(), Viewport.getScaleY());
  fill(235);
  textFont(font);
  textAlign(CENTER);
  text("Apoya varios dedos", 200, 35);

  for (TouchPoint point : core.points) {
    noStroke();
    fill(45, 210, 145, 150);
    ellipse(point.x, point.y, 70, 70);
    fill(255);
    text("ID " + point.id, point.x, point.y + 6);
  }
  popMatrix();
}
