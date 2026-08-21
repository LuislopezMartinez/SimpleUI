import simplecore.*;

Core taskCore;
PImage playerImage;
PFont gameFont;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  taskCore = Core.start(this);
  taskCore.setMode(400, 700, ViewportMode.FIT);
  playerImage = createPlayerImage(44, color(70, 180, 255));
  gameFont = createFont("SansSerif", 18);
  new Player();
}

void draw() {
  background(25, 31, 46);
}

class Player extends Task {
  Player() {
    setGraph(playerImage);
    setPosition(200, 350);
  }

  protected void frame() {
    if (key(_LEFT) || key(_A)) x -= 4;
    if (key(_RIGHT) || key(_D)) x += 4;
    if (key(_UP) || key(_W)) y -= 4;
    if (key(_DOWN) || key(_S)) y += 4;

    x = constrain(x, 22, 378);
    y = constrain(y, 110, 678);

    text(gameFont, 18, "Teclado fisico: flechas o WASD",
      CENTER, 200, 35, color(240), 255);
    text(gameFont, 15, "Prueba dos teclas a la vez",
      CENTER, 200, 62, color(160, 205, 255), 255);
  }
}

PImage createPlayerImage(int size, int playerColor) {
  PImage image = createImage(size, size, ARGB);
  image.loadPixels();
  float center = size / 2.0;
  for (int py = 0; py < size; py++) {
    for (int px = 0; px < size; px++) {
      float distance = dist(px, py, center, center);
      image.pixels[py * size + px] = distance <= center
        ? playerColor : color(0, 0);
    }
  }
  image.updatePixels();
  return image;
}
