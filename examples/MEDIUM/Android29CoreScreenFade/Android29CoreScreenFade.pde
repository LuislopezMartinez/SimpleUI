import simplecore.*;

Core core;
FadeDemo demo;
PFont font;

void settings() {
  Core.setVideoMode(this, 400, 700, P2D);
}

void setup() {
  core = Core.start(this, ViewportMode.FIT);
  font = createFont("SansSerif", 20);
  demo = new FadeDemo();
}

void draw() {
  background(demo.scene == 0 ? color(35, 110, 190) : color(215, 95, 55));
}

class FadeDemo extends Task {
  int scene;
  boolean changing;
  boolean wasPressed;

  protected void frame() {
    boolean pressed = mouse.left;
    if (pressed && !wasPressed && !changing) {
      changing = true;
      core.fadeOff(color(12, 18, 30), 600);
    }
    wasPressed = pressed;

    if (changing && core.isFaded()) {
      scene = 1 - scene;
      core.fadeOn(600);
    }
    if (changing && !core.isFading() && !core.isFaded()) changing = false;

    text(font, 20, "Toca para cambiar de escena", CENTER,
      200, 340, color(255), 255);
  }
}
