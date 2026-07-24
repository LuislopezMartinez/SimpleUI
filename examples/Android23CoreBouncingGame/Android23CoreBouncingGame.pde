import simplecore.*;

Core taskCore;
PImage ballImage;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  taskCore = Core.start(this);
  taskCore.setTaskCanvasScale(width / 400.0);
  ballImage = createCircleImage(34, color(255, 190, 55));
  new BouncingBall(110, 180, 2.4, 2.0);
  new BouncingBall(290, 360, -2.0, 2.7);
}

void draw() {
  background(25, 31, 46);
  float scale = width / 400.0;
  pushMatrix();
  scale(scale);
  fill(230);
  textAlign(CENTER, TOP);
  textSize(18);
  text("Mini juego: tareas que rebotan", 200, 24);
  popMatrix();
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
    float designHeight = height / (width / 400.0);
    x += speedX;
    y += speedY;
    if (x < radius || x > 400 - radius) speedX *= -1;
    if (y < 75 + radius || y > designHeight - radius) speedY *= -1;
    x = constrain(x, radius, 400 - radius);
    y = constrain(y, 75 + radius, designHeight - radius);
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
