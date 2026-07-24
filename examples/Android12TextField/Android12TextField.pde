import simpleui.android.*;

UITextField nameField;
UILabel greetingLabel;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  nameField = new UITextField("name", 60, 100, 280, 52, "Escribe tu nombre", 17);
  nameField.setMaxLen(24);
  greetingLabel = new UILabel("greeting", 45, 185, 310, 52, "Confirma al terminar", 16);
  greetingLabel.setTextAlignment(CENTER, CENTER);

  SimpleUI.addUIElement(nameField);
  SimpleUI.addUIElement(greetingLabel);
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == nameField && action.equals("submitted")) {
        greetingLabel.setText("Hola, " + data + "!");
      }
    }
  });
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
