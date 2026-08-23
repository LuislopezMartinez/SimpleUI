import simpleui.desktop.*;
import simplecore.*;

Core taskCore;
UIButton celebrateButton;
UILabel helpLabel;
PImage particleImage;

void settings() {
  size(500, 420, P2D);
}

void setup() {
  taskCore = Core.start(this);
  particleImage = createCircleImage(16, color(255));

  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);
  celebrateButton = new UIButton("celebrate", 130, 285, 240, 56, "Crear particulas", 18);
  helpLabel = new UILabel("help", 70, 45, 360, 45, "Cada particula es una Task", 18);
  helpLabel.setTextAlignment(CENTER, CENTER);
  SimpleUI.addUIElement(celebrateButton);
  SimpleUI.addUIElement(helpLabel);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == celebrateButton && action.equals("clicked")) createBurst();
    }
  });
}

void createBurst() {
  for (int i = 0; i < 24; i++) {
    new Particle(250, 270, random(-3.5, 3.5), random(-5.5, -1.5));
  }
}

void draw() {
  background(25, 31, 46);
}

class Particle extends Task {
  float speedX;
  float speedY;

  Particle(float startX, float startY, float vx, float vy) {
    setGraph(particleImage);
    setPosition(startX, startY);
    tint(color(random(120, 255), random(120, 255), random(120, 255)));
    speedX = vx;
    speedY = vy;
  }

  protected void frame() {
    x += speedX;
    y += speedY;
    speedY += 0.12;
    setAlpha(255 - liveFrames * 4);
    if (alpha <= 0) kill();
  }
}

PImage createCircleImage(int diameter, int circleColor) {
  PImage image = createImage(diameter, diameter, ARGB);
  image.loadPixels();
  float radius = diameter / 2.0;
  for (int y = 0; y < diameter; y++) {
    for (int x = 0; x < diameter; x++) {
      image.pixels[y * diameter + x] = dist(x, y, radius, radius) <= radius
        ? circleColor : color(0, 0);
    }
  }
  image.updatePixels();
  return image;
}
