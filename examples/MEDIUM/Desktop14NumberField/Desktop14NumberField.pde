import simpleui.desktop.*;

UINumberField ageField;
UILabel resultLabel;

void settings() {
  size(400, 300, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);

  ageField = new UINumberField("age", 80, 75, 240, 48, "Edad: 0 a 120", 17);
  ageField.setAllowDecimal(false);
  ageField.setAllowNegative(false);
  ageField.setRange(0, 120);
  resultLabel = new UILabel("result", 45, 155, 310, 48, "Introduce una edad", 16);
  resultLabel.setTextAlignment(CENTER, CENTER);

  SimpleUI.addUIElement(ageField);
  SimpleUI.addUIElement(resultLabel);
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == ageField && action.equals("submitted")) {
        resultLabel.setText(data == null ? "Edad no valida" : "Edad guardada: " + round((Float)data));
      }
    }
  });
}
