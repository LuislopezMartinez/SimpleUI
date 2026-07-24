import simpleui.desktop.*;

UITabs sectionTabs;
UILabel contentLabel;

void settings() {
  size(400, 300);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(400, 300);

  String[] sections = { "Inicio", "Curso", "Ayuda" };
  sectionTabs = new UITabs("sections", 25, 65, 350, 48, sections, 16);
  contentLabel = new UILabel("content", 40, 150, 320, 70, "Estas en Inicio", 19);
  contentLabel.setTextAlignment(CENTER, CENTER);
  SimpleUI.addUIElement(sectionTabs);
  SimpleUI.addUIElement(contentLabel);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == sectionTabs && action.equals("tabChanged")) {
        contentLabel.setText("Estas en " + data);
      }
    }
  });
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
