import simpleui.android.*;

UITextField nameField;
UIButton helloButton;
UILabel statusLabel;

void settings() {
  SimpleUI.setVideoMode(this, 400, 800, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18 UIScaleMode.RESPONSIVE);

  nameField = new UITextField("name", 30, 70, 340, 52, "Your name", 18);
  helloButton = new UIButton("hello", 30, 145, 180, 52, "Hello", 18);
  statusLabel = new UILabel("status", 30, 220, 340, 80, "SimpleUI Android", 18);
  SimpleUI.addUIElement(nameField);
  SimpleUI.addUIElement(helloButton);
  SimpleUI.addUIElement(statusLabel);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == helloButton && action.equals("clicked")) {
        statusLabel.setText("Hello " + nameField.getText());
      }
    }
  });
}
