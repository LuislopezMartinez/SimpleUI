import simpleui.android.*;

UIButton greetButton;
UILabel messageLabel;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  greetButton = new UIButton("greet", 70, 90, 260, 56, "Saludar", 18);
  messageLabel = new UILabel("message", 40, 170, 320, 44, "Toca el boton", 17);
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

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
