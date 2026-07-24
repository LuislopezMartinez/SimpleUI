import simplecore.*;

Core taskCore;
PImage ballImage;

void settings() {
  size(640, 400);
}

void setup() {
  taskCore = Core.start(this);
  ballImage = createCircleImage(40, color(255, 190, 55));
  new BouncingBall(180, 120, 3.2, 2.4);
  new BouncingBall(420, 260, -2.5, 3.0);
}

void draw() {
  background(25, 31, 46);
  fill(230);
  textAlign(CENTER, TOP);
  textSize(18);
  text("Mini juego: tareas que rebotan", width / 2, 20);
}

class BouncingBall extends Task {
  float speedX;
  float speedY;
  float radius = 20;

  BouncingBall(float startX, float startY, float vx, float vy) {
    setGraph(ballImage);
    setPosition(startX, startY);
    speedX = vx;
    speedY = vy;
  }

  protected void frame() {
    x += speedX;
    y += speedY;
    if (x < radius || x > width - radius) speedX *= -1;
    if (y < 70 + radius || y > height - radius) speedY *= -1;
    x = constrain(x, radius, width - radius);
    y = constrain(y, 70 + radius, height - radius);
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
