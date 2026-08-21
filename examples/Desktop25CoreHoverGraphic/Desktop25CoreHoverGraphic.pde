import simplecore.*;

Core taskCore;
InteractiveDiamond diamond;
PImage diamondImage;
PFont gameFont;

void settings() {
  size(560, 700);
}

void setup() {
  taskCore = Core.start(this);
  taskCore.setMode(400, 700, ViewportMode.FIT);
  diamondImage = createDiamondImage(90, color(255, 205, 55));
  gameFont = createFont("SansSerif", 18);
  diamond = new InteractiveDiamond(200, 350);
}

void draw() {
  background(25, 31, 46);
}

class InteractiveDiamond extends Task {
  float currentScale = 1;

  InteractiveDiamond(float startX, float startY) {
    setGraph(diamondImage);
    setPosition(startX, startY);
  }

  protected void frame() {
    boolean pointerIsNear = dist(mouse.x, mouse.y, x, y) < 65;
    text(gameFont, 18, "Pasa el raton sobre el grafico",
      CENTER, 200, 35, color(235), 255);
    float targetScale = pointerIsNear ? 1.45 : 1.0;
    currentScale = lerp(currentScale, targetScale, 0.14);
    scale(currentScale);
    angle += pointerIsNear ? 2.0 : 0.3;
  }
}

PImage createDiamondImage(int size, int diamondColor) {
  PImage image = createImage(size, size, ARGB);
  image.loadPixels();
  float center = size / 2.0;
  for (int y = 0; y < size; y++) {
    for (int x = 0; x < size; x++) {
      boolean inside = abs(x - center) + abs(y - center) < center;
      image.pixels[y * size + x] = inside ? diamondColor : color(0, 0);
    }
  }
  image.updatePixels();
  return image;
}
