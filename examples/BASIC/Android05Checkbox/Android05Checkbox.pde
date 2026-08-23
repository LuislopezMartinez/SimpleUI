import simpleui.android.*;

UICheckbox soundCheckbox;
UILabel resultLabel;

void settings() {
  fullScreen(P2D);
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.RESPONSIVE);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  soundCheckbox = new UICheckbox("sound", 70, 95, 36, "Activar sonido", 18);
  resultLabel = new UILabel("result", 50, 170, 300, 44, "Sonido desactivado", 16);
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
