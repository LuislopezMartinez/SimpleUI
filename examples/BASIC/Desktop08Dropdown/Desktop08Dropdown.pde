import simpleui.desktop.*;

UIDropdown colorMenu;
UILabel resultLabel;

void settings() {
  size(400, 320, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);

  String[] colors = { "Rojo", "Verde", "Azul" };
  colorMenu = new UIDropdown("colors", 70, 75, 260, 48, colors, 17);
  resultLabel = new UILabel("result", 50, 220, 300, 40, "Color: Rojo", 17);
  resultLabel.setTextAlignment(CENTER, CENTER);
  SimpleUI.addUIElement(colorMenu);
  SimpleUI.addUIElement(resultLabel);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == colorMenu && action.equals("changed")) {
        resultLabel.setText("Color: " + data);
      }
    }
  });
}
