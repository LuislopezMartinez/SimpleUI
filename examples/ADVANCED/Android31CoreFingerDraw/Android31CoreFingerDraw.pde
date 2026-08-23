import simplecore.*;

Core core;
PFont font;
ArrayList<DrawSegment> drawing = new ArrayList<DrawSegment>();

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
  for (TouchPoint point : core.points) {
    if (point.deltaX != 0 || point.deltaY != 0) {
      drawing.add(new DrawSegment(
        point.previousX, point.previousY, point.x, point.y
      ));
    }
  }

  background(250);
  pushMatrix();
  translate(Viewport.getOffsetX(), Viewport.getOffsetY());
  scale(Viewport.getScaleX(), Viewport.getScaleY());
  stroke(20, 115, 220);
  strokeWeight(8);
  strokeCap(ROUND);
  for (DrawSegment segment : drawing) {
    line(segment.x1, segment.y1, segment.x2, segment.y2);
  }

  noStroke();
  fill(255, 235);
  rect(0, 0, 400, 48);
  fill(35);
  textFont(font);
  textAlign(CENTER);
  text("Dibuja con varios dedos", 200, 30);
  popMatrix();
}

class DrawSegment {
  float x1, y1, x2, y2;

  DrawSegment(float x1, float y1, float x2, float y2) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  }
}
