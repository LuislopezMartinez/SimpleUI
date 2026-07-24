import simpleui.android.*;

UITextArea notesArea;
UILabel counterLabel;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 17);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  notesArea = new UITextArea("notes", 40, 85, 320, 240, "Escribe tus apuntes...", 16);
  notesArea.setMaxLen(280);
  counterLabel = new UILabel("counter", 40, 350, 320, 44, "0 caracteres", 15);

  SimpleUI.addUIElement(notesArea);
  SimpleUI.addUIElement(counterLabel);
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == notesArea && action.equals("changed")) {
        counterLabel.setText(notesArea.getText().length() + " caracteres");
      }
    }
  });
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
