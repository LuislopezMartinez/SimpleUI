import simplecore.*;

Core taskCore;
PImage ballImage;
PFont gameFont;

void settings() {
  Core.setVideoMode(this, 400, 700, P2D);
}

void setup() {
  taskCore = Core.start(this, ViewportMode.FIT);
  ballImage = createCircleImage(34, color(255, 190, 55));
  gameFont = createFont("SansSerif", 18);
  new GameText();
  new BouncingBall(110, 180, 2.4, 2.0);
  new BouncingBall(290, 360, -2.0, 2.7);
}

void draw() {
  background(25, 31, 46);
}

class GameText extends Task {
  protected void frame() {
    text(gameFont, 18, "Toca para acelerar",
      CENTER, 200, 35, color(230), 255);
  }
}

class BouncingBall extends Task {
  float speedX;
  float speedY;
  float radius = 17;

  BouncingBall(float startX, float startY, float vx, float vy) {
    setGraph(ballImage);
    setPosition(startX, startY);
    speedX = vx;
    speedY = vy;
  }

  protected void frame() {
    float boost = mouse.left ? 2.0 : 1.0;
    x += speedX * boost;
    y += speedY * boost;
    if (x < radius || x > 400 - radius) speedX *= -1;
    if (y < 75 + radius || y > 700 - radius) speedY *= -1;
    x = constrain(x, radius, 400 - radius);
    y = constrain(y, 75 + radius, 700 - radius);
  }
}

PImage createCircleImage(int diameter, int circleColor) {
  PImage image = createImage(diameter, diameter, ARGB);
  image.loadPixels();
  float radius = diameter / 2.0;
  for (int y = 0; y < diameter; y++) {
    for (int x = 0; x < diameter; x++) {
      float distance = dist(x, y, radius, radius);
      image.pixels[y * diameter + x] = distance <= radius ? circleColor : color(0, 0);
    }
  }
  image.updatePixels();
  return image;
}
