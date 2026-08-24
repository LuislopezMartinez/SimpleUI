import simpleui.desktop.*;

UIButton greetButton;
UILabel messageLabel;

void settings() {
  SimpleUI.setVideoMode(this, 400, 260, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);

  greetButton = new UIButton("greet", 70, 70, 260, 52, "Saludar", 18);
  messageLabel = new UILabel("message", 40, 145, 320, 40, "Pulsa el boton", 17);
  messageLabel.setTextAlignment(CENTER, CENTER);

  SimpleUI.addUIElement(greetButton);
  SimpleUI.addUIElement(messageLabel);
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == greetButton && action.equals("clicked")) {
        messageLabel.setText("Hola, SimpleUI!");
      }
    }
  });
}
