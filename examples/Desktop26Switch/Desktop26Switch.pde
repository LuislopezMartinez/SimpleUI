import simpleui.desktop.*;

UISwitch signalSwitch;
UILabel resultLabel;

void settings() {
  size(440, 280);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(440, 280);

  signalSwitch = new UISwitch(
    "signalMode", 55, 75, 330, 42,
    "Ordenar por intensidad", false, 17
  );
  resultLabel = new UILabel(
    "result", 55, 145, 330, 42,
    "Topología de red", 16
  );
  SimpleUI.addUIElement(signalSwitch);
  SimpleUI.addUIElement(resultLabel);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == signalSwitch && action.equals("changed")) {
        resultLabel.setText(
          (Boolean)data ? "Orden por intensidad" : "Topología de red"
        );
      }
    }
  });
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
