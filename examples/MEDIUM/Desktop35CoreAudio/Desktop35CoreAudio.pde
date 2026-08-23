import simplecore.*;

Core core;
Sound music;
Sound effect;
AudioDemo demo;
PFont font;

void settings() {
  size(640, 480, P2D);
}

void setup() {
  core = Core.start(this);
  core.setMode(640, 480, ViewportMode.FIT);
  font = createFont("SansSerif", 20);
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
  String status = "Pulsa: reproducir OGG";

  protected void frame() {
    boolean pressed = mouse.left;
    if (pressed && !wasPressed) {
      if (step == 0) {
        music.setVolume(0.65).setLoop(true).play();
        status = "OGG en loop. Pulsa: reproducir WAV";
      } else if (step == 1) {
        effect.play();
        status = "WAV reproducido. Pulsa: detener";
      } else {
        music.stop();
        effect.stop();
        status = "Sonidos detenidos. Pulsa: reproducir OGG";
      }
      step = (step + 1) % 3;
    }
    wasPressed = pressed;
    text(font, 20, status, CENTER, 320, 220, color(245), 255);
    text(font, 15, "MP3, OGG y WAV usan la misma API", CENTER,
      320, 255, color(90, 180, 255), 255);
  }
}
