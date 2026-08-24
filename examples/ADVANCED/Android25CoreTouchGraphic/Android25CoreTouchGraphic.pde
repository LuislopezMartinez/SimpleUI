import simplecore.*;

Core taskCore;
InteractiveDiamond diamond;
PImage diamondImage;
PFont gameFont;

void settings() {
  Core.setVideoMode(this, 400, 700, P2D);
}

void setup() {
  taskCore = Core.start(this, ViewportMode.FIT);
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
    boolean touched = isTouched();
    if (touched) {
      x += point.deltaX;
      y += point.deltaY;
    }
    text(gameFont, 18, "Pulsa el grafico para detectar el contacto",
      CENTER, 200, 35, color(235), 255);
    float targetScale = touched ? 1.45 : 1.0;
    currentScale = lerp(currentScale, targetScale, 0.14);
    scale(currentScale);
    angle += touched ? 2.0 : 0.3;
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
