import simplecore.*;

Core taskCore;
InteractiveDiamond diamond;
PImage diamondImage;
float designScale;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  designScale = width / 400.0;
  taskCore = Core.start(this);
  taskCore.setTaskCanvasScale(designScale);
  diamondImage = createDiamondImage(90, color(255, 205, 55));
  diamond = new InteractiveDiamond(200, 280);
}

void draw() {
  background(25, 31, 46);
  pushMatrix();
  scale(designScale);
  fill(235);
  textAlign(CENTER, TOP);
  textSize(18);
  text("Manten el dedo sobre el grafico", 200, 30);
  popMatrix();
}

class InteractiveDiamond extends Task {
  InteractiveDiamond(float startX, float startY) {
    setGraph(diamondImage);
    setPosition(startX, startY);
  }

  protected void frame() {
    float touchX = mouseX / designScale;
    float touchY = mouseY / designScale;
    boolean fingerIsNear = mousePressed && dist(touchX, touchY, x, y) < 70;
    float targetScale = fingerIsNear ? 1.45 : 1.0;
    scale = lerp(scale, targetScale, 0.14);
    angle += fingerIsNear ? 2.0 : 0.3;
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
