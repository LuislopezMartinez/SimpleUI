import simpleui.desktop.*;

UISignalMeter signalMeter;
UISlider rssiSlider;

void settings() {
  SimpleUI.setVideoMode(this, 440, 350, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 17, UIScaleMode.FIT);

  signalMeter = new UISignalMeter("signal", 45, 55, 350, 100, "Enlace LoRa");
  signalMeter.setRssi(-90);
  rssiSlider = new UISlider("rssi", 45, 220, 350, 36, -130, -30, -90, 15);
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
