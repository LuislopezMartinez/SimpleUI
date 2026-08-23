import simplecore.*;

Core core;
FadeDemo demo;
PFont font;

void settings() {
  size(640, 480, P2D);
}

void setup() {
  core = Core.start(this);
  core.setMode(640, 480, ViewportMode.FIT);
  font = createFont("SansSerif", 22);
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

    text(font, 22, "Pulsa para cambiar de escena", CENTER,
      320, 230, color(255), 255);
  }
}
