import simplecore.*;

Core core;
PImage blueDisc;
PImage orangeDisc;
PFont font;

void settings() {
  Core.setVideoMode(this, 400, 700, P2D);
}

void setup() {
  core = Core.start(this, ViewportMode.FIT);
  font = createFont("SansSerif", 18);
  blueDisc = createDisc(90, color(35, 135, 235));
  orangeDisc = createDisc(90, color(245, 135, 45));
  new DraggableDisc(blueDisc, 120, 350);
  new DraggableDisc(orangeDisc, 280, 350);
}

void draw() {
  background(24, 29, 40);
  fill(235);
  textFont(font);
  textAlign(CENTER);
  text("Mueve los dos discos con dos dedos", 200, 35);
}

class DraggableDisc extends Task {
  TouchPoint draggedBy;
  float grabOffsetX;
  float grabOffsetY;

  DraggableDisc(PImage image, float startX, float startY) {
    setGraph(image).setPosition(startX, startY);
  }

  protected void frame() {
    if (isTouched()) {
      if (draggedBy != point) {
        draggedBy = point;
        grabOffsetX = x - point.x;
        grabOffsetY = y - point.y;
      }
      x = point.x + grabOffsetX;
      y = point.y + grabOffsetY;
    } else {
      draggedBy = null;
    }
  }
}

PImage createDisc(int size, int discColor) {
  PImage image = createImage(size, size, ARGB);
  image.loadPixels();
  float radius = size * 0.5;
  for (int y = 0; y < size; y++) {
    for (int x = 0; x < size; x++) {
      image.pixels[y * size + x] = dist(x, y, radius, radius) < radius
        ? discColor : color(0, 0);
    }
  }
  image.updatePixels();
  return image;
}
