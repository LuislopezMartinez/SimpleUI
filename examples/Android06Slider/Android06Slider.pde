import simpleui.android.*;

UISlider volumeSlider;
UILabel valueLabel;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  volumeSlider = new UISlider("volume", 50, 110, 300, 40, 0, 100, 35, 16);
  valueLabel = new UILabel("value", 50, 180, 300, 44, "Volumen: 35", 18);
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

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
