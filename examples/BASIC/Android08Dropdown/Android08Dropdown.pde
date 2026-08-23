import simpleui.android.*;

UIDropdown colorMenu;
UILabel resultLabel;

void settings() {
  fullScreen(P2D);
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.RESPONSIVE);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  String[] colors = { "Rojo", "Verde", "Azul" };
  colorMenu = new UIDropdown("colors", 70, 95, 260, 52, colors, 17);
  resultLabel = new UILabel("result", 50, 240, 300, 44, "Color: Rojo", 17);
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
