import simplecore.*;

Core taskCore;
String taskStatus = "Waiting";

class CounterTask extends Task {
  protected void initialize() {
    taskStatus = "Task initialized · id=" + id;
  }

  protected void frame() {
    taskStatus = "Task frame " + liveFrames;
    if (liveFrames >= 300) kill();
  }

  protected void onDestroy() {
    taskStatus = "Task destroyed";
  }
}

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  taskCore = Core.start(this);
  new CounterTask();
}

void draw() {
  background(28);
  fill(240);
  textAlign(CENTER, CENTER);
  textSize(22);
  text(taskStatus, width * 0.5, height * 0.5);
}
