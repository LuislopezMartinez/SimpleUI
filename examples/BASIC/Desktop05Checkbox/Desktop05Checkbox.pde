import simpleui.desktop.*;

UICheckbox soundCheckbox;
UILabel resultLabel;

void settings() {
  size(400, 260, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);

  soundCheckbox = new UICheckbox("sound", 70, 75, 32, "Activar sonido", 18);
  resultLabel = new UILabel("result", 50, 145, 300, 40, "Sonido desactivado", 16);
  SimpleUI.addUIElement(soundCheckbox);
  SimpleUI.addUIElement(resultLabel);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == soundCheckbox && action.equals("changed")) {
        resultLabel.setText((Boolean)data ? "Sonido activado" : "Sonido desactivado");
      }
    }
  });
}
