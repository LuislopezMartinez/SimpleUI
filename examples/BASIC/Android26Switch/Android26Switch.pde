import simpleui.android.*;

UISwitch signalSwitch;
UILabel resultLabel;

void settings() {
  fullScreen(P2D);
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.RESPONSIVE);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  signalSwitch = new UISwitch(
    "signalMode", 35, 95, 330, 46,
    "Ordenar por intensidad", false, 17
  );
  resultLabel = new UILabel(
    "result", 35, 170, 330, 44,
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
