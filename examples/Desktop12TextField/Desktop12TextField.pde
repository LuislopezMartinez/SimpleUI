import simpleui.desktop.*;

UITextField nameField;
UILabel greetingLabel;

void settings() {
  size(400, 300);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(400, 300);

  nameField = new UITextField("name", 60, 80, 280, 48, "Escribe tu nombre", 17);
  nameField.setMaxLen(24);
  greetingLabel = new UILabel("greeting", 45, 160, 310, 48, "Pulsa Enter al terminar", 16);
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
