import simpleui.desktop.*;

UISlider volumeSlider;
UILabel valueLabel;

void settings() {
  size(400, 280, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);

  volumeSlider = new UISlider("volume", 50, 90, 300, 36, 0, 100, 35, 16);
  valueLabel = new UILabel("value", 50, 155, 300, 40, "Volumen: 35", 18);
  valueLabel.setTextAlignment(CENTER, CENTER);
  SimpleUI.addUIElement(volumeSlider);
  SimpleUI.addUIElement(valueLabel);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == volumeSlider && action.equals("changed")) {
        valueLabel.setText("Volumen: " + round((Float)data));
      }
    }
  });
}
