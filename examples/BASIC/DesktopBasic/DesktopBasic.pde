import simpleui.desktop.*;

UIButton helloButton;
UILabel statusLabel;

void settings() {
  size(640, 420, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);

  helloButton = new UIButton("hello", 40, 50, 180, 48, "Hello", 18);
  statusLabel = new UILabel("status", 40, 120, 360, 40, "Press the button", 18);
  SimpleUI.addUIElement(helloButton);
  SimpleUI.addUIElement(statusLabel);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == helloButton && action.equals("clicked")) {
        statusLabel.setText("SimpleUI Desktop is working");
      }
    }
  });
}
