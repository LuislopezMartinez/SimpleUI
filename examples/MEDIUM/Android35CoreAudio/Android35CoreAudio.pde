import simplecore.*;

Core core;
Sound music;
Sound effect;
AudioDemo demo;
PFont font;

void settings() {
  fullScreen(P2D);
}

void setup() {
  orientation(PORTRAIT);
  core = Core.start(this);
  core.setMode(400, 700, ViewportMode.FIT);
  font = createFont("SansSerif", 18);
  music = core.loadSound("2013_15.ogg");
  effect = core.loadSound("error.wav");
  demo = new AudioDemo();
}

void draw() {
  background(24, 29, 40);
}

class AudioDemo extends Task {
  boolean wasPressed;
  int step;
  String status = "Toca: reproducir OGG";

  protected void frame() {
    boolean pressed = mouse.left;
    if (pressed && !wasPressed) {
      if (step == 0) {
        music.setVolume(0.65).setLoop(true).play();
        status = "OGG en loop. Toca: reproducir WAV";
      } else if (step == 1) {
        effect.play();
        status = "WAV reproducido. Toca: detener";
      } else {
        music.stop();
        effect.stop();
        status = "Sonidos detenidos. Toca: reproducir OGG";
      }
      step = (step + 1) % 3;
    }
    wasPressed = pressed;
    text(font, 18, status, CENTER, 200, 330, color(245), 255);
    text(font, 14, "MP3, OGG y WAV usan la misma API", CENTER,
      200, 365, color(90, 180, 255), 255);
  }
}
