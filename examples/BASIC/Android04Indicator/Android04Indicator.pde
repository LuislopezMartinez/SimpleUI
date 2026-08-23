import simpleui.android.*;

UIIndicator statusLight;
UIButton toggleButton;
boolean connected = false;

void settings() {
  fullScreen(P2D);
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.RESPONSIVE);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  statusLight = new UIIndicator("status", 174, 70, 52);
  toggleButton = new UIButton("toggle", 80, 165, 240, 56, "Conectar", 18);
  updateConnectionState();

  SimpleUI.addUIElement(statusLight);
  SimpleUI.addUIElement(toggleButton);
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == toggleButton && action.equals("clicked")) {
        connected = !connected;
        updateConnectionState();
      }
    }
  });
}

void updateConnectionState() {
  statusLight.setIndicatorColor(connected ? color(45, 190, 105) : color(220, 75, 75));
  toggleButton.setLabel(connected ? "Desconectar" : "Conectar");
}
