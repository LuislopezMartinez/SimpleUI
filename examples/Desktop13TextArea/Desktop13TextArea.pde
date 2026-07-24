import simpleui.desktop.*;

UITextArea notesArea;
UILabel counterLabel;

void settings() {
  size(440, 420);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 17);
  SimpleUI.setMode(440, 420);

  notesArea = new UITextArea("notes", 45, 65, 350, 220, "Escribe tus apuntes...", 16);
  notesArea.setMaxLen(280);
  counterLabel = new UILabel("counter", 45, 310, 350, 42, "0 caracteres", 15);

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
