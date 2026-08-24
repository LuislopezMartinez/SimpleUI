import simplecore.*;

Core taskCore;
String taskState = "Preparando tarea...";

void settings() {
  Core.setVideoMode(this, 540, 320, P2D);
}

void setup() {
  taskCore = Core.start(this, ViewportMode.FIT);
  new CountdownTask(5);
}

void draw() {
  background(28, 32, 42);
  fill(240);
  textAlign(CENTER, CENTER);
  textSize(22);
  text(taskState, width / 2, height / 2);
}

class CountdownTask extends Task {
  int totalSeconds;
  int secondsRemaining;

  CountdownTask(int seconds) {
    totalSeconds = seconds;
    secondsRemaining = seconds;
  }

  protected void initialize() {
    taskState = "Tarea creada - id " + id;
  }

  protected void frame() {
    secondsRemaining = max(0, totalSeconds - liveFrames / 60);
    taskState = "Cuenta atras: " + secondsRemaining;
    if (liveFrames >= totalSeconds * 60) kill();
  }

  protected void onDestroy() {
    taskState = "Tarea terminada y eliminada";
  }
}
