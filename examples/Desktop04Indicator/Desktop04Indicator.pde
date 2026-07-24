import simpleui.desktop.*;

UIIndicator statusLight;
UIButton toggleButton;
boolean connected = false;

void settings() {
  size(400, 280);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(400, 280);

  statusLight = new UIIndicator("status", 174, 50, 52);
  toggleButton = new UIButton("toggle", 80, 145, 240, 52, "Conectar", 18);
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

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
