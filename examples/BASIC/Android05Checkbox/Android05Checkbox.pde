import simpleui.android.*;

UICheckbox soundCheckbox;
UILabel resultLabel;

void settings() {
  SimpleUI.setVideoMode(this, 400, 800, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18,IScaleMode.RESPONSIVE);

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
