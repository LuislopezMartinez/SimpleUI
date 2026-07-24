import simpleui.android.*;

UISignalMeter signalMeter;
UISlider rssiSlider;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 17);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  signalMeter = new UISignalMeter("signal", 25, 75, 350, 110, "Enlace LoRa");
  signalMeter.setRssi(-90);
  rssiSlider = new UISlider("rssi", 25, 250, 350, 40, -130, -30, -90, 15);
  SimpleUI.addUIElement(signalMeter);
  SimpleUI.addUIElement(rssiSlider);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == rssiSlider && action.equals("changed")) {
        signalMeter.setRssi(round((Float)data));
      }
    }
  });
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
