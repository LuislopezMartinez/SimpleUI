import simpleui.desktop.*;

UIProgressBar progressBar;
UIButton addButton;
float progress = 0;

void settings() {
  size(400, 290);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(400, 290);

  progressBar = new UIProgressBar("progress", 50, 80, 300, 32);
  addButton = new UIButton("add", 90, 155, 220, 52, "Sumar 10 %", 18);
  SimpleUI.addUIElement(progressBar);
  SimpleUI.addUIElement(addButton);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == addButton && action.equals("clicked")) {
        progress = (progress + 0.1) % 1.1;
        progressBar.setProgress(progress);
      }
    }
  });
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
