import simplecore.*;

Core core;
PFont font;
ArrayList<DrawSegment> drawing = new ArrayList<DrawSegment>();
ArrayList<TrackedPoint> trackedPoints = new ArrayList<TrackedPoint>();

void settings() {
  Core.setVideoMode(this, 640, 480, P2D);
}

void setup() {
  core = Core.start(this, ViewportMode.FIT);
  font = createFont("SansSerif", 18);
}

void draw() {
  for (TrackedPoint tracked : trackedPoints) tracked.active = false;
  for (TouchPoint point : core.points) {
    TrackedPoint tracked = findTrackedPoint(point.id);
    if (tracked == null) {
      tracked = new TrackedPoint(point.id, point.x, point.y);
      trackedPoints.add(tracked);
    } else if (tracked.x != point.x || tracked.y != point.y) {
      drawing.add(new DrawSegment(tracked.x, tracked.y, point.x, point.y));
      tracked.x = point.x;
      tracked.y = point.y;
    }
    tracked.active = true;
  }
  removeReleasedPoints();

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
  rect(0, 0, 640, 48);
  fill(35);
  textFont(font);
  textAlign(CENTER);
  text("Dibuja manteniendo pulsado el ratón", 320, 30);
  popMatrix();
}

TrackedPoint findTrackedPoint(int id) {
  for (TrackedPoint tracked : trackedPoints) {
    if (tracked.id == id) return tracked;
  }
  return null;
}

void removeReleasedPoints() {
  for (int i = trackedPoints.size() - 1; i >= 0; i--) {
    if (!trackedPoints.get(i).active) trackedPoints.remove(i);
  }
}

class TrackedPoint {
  int id;
  float x, y;
  boolean active;

  TrackedPoint(int id, float x, float y) {
    this.id = id;
    this.x = x;
    this.y = y;
  }
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
