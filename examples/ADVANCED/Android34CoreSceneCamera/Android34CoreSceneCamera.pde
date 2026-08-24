import simplecore.*;

Core core;
Scene level;
Player player;
PFont font;

void settings() {
  Core.setVideoMode(this, 800, 500, P2D);
}

void setup() {
  core = Core.start(this, ViewportMode.FIT);
  core.setAutomaticRenportMode.FIT);
  font = createFont("SansSerif", 18);

  level = core.createScene().activate();
  new World(createWorld(1800, 900));
  player = new Player(createDisc(70, color(250, 185, 45)), 250, 250);
  level.camera.setDeadZone(280, 170, 240, 160);
  level.camera.setBounds(0, 0, 1800, 900);
  level.camera.setTarget(player);
}

void draw() {
  background(18, 23, 34);
  core.renderTasks();
  fill(255);
  textFont(font);
  textAlign(CENTER);
  text("Arrastra el personaje; la cámara espera en la zona central", width / 2, 30);
}

class World extends Task {
  World(PImage image) {
    setGraph(image).setPosition(image.width / 2, image.height / 2).setZ(-100);
  }
}

class Player extends Task {
  Player(PImage image, float startX, float startY) {
    setGraph(image).setPosition(startX, startY).setZ(10);
  }

  protected void frame() {
    if (isTouched()) {
      x += point.deltaX;
      y += point.deltaY;
    }
    x = constrain(x, 35, 1765);
    y = constrain(y, 35, 865);
  }
}

PImage createWorld(int worldWidth, int worldHeight) {
  PGraphics world = createGraphics(worldWidth, worldHeight, P2D);
  world.beginDraw();
  world.background(34, 43, 60);
  world.stroke(62, 76, 98);
  for (int x = 0; x <= worldWidth; x += 100) world.line(x, 0, x, worldHeight);
  for (int y = 0; y <= worldHeight; y += 100) world.line(0, y, worldWidth, y);
  world.fill(90, 110, 140);
  world.textAlign(CENTER, CENTER);
  world.textSize(24);
  for (int x = 100; x < worldWidth; x += 200) {
    for (int y = 100; y < worldHeight; y += 200) world.text(x + "," + y, x, y);
  }
  world.endDraw();
  return world;
}

PImage createDisc(int size, int discColor) {
  PImage image = createImage(size, size, ARGB);
  image.loadPixels();
  float radius = size * 0.5;
  for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
    image.pixels[y * size + x] = dist(x, y, radius, radius) < radius
      ? discColor : color(0, 0);
  }
  image.updatePixels();
  return image;
}
