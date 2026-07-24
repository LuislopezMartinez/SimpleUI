import simpleui.android.*;

UITabs sectionTabs;
UILabel contentLabel;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  String[] sections = { "Inicio", "Curso", "Ayuda" };
  sectionTabs = new UITabs("sections", 25, 85, 350, 52, sections, 16);
  contentLabel = new UILabel("content", 40, 175, 320, 74, "Estas en Inicio", 19);
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
