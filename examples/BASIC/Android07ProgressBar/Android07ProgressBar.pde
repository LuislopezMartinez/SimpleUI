import simpleui.android.*;

UIProgressBar progressBar;
UIButton addButton;
float progress = 0;

void settings() {
  fullScreen(P2D);
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.RESPONSIVE);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  progressBar = new UIProgressBar("progress", 50, 100, 300, 36);
  addButton = new UIButton("add", 90, 175, 220, 56, "Sumar 10 %", 18);
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
