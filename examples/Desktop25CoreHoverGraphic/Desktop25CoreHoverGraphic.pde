import simplecore.*;

Core taskCore;
InteractiveDiamond diamond;
PImage diamondImage;

void settings() {
  size(560, 380);
}

void setup() {
  taskCore = Core.start(this);
  diamondImage = createDiamondImage(90, color(255, 205, 55));
  diamond = new InteractiveDiamond(width / 2, height / 2);
}

void draw() {
  background(25, 31, 46);
  fill(235);
  textAlign(CENTER, TOP);
  textSize(18);
  text("Pasa el raton sobre el grafico", width / 2, 28);
}

class InteractiveDiamond extends Task {
  InteractiveDiamond(float startX, float startY) {
    setGraph(diamondImage);
    setPosition(startX, startY);
  }

  protected void frame() {
    boolean pointerIsNear = dist(mouseX, mouseY, x, y) < 65;
    float targetScale = pointerIsNear ? 1.45 : 1.0;
    scale = lerp(scale, targetScale, 0.14);
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
