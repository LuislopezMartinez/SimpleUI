import simpleui.desktop.*;

UIList lessonList;
UILabel selectedLabel;

void settings() {
  size(400, 460);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 17);
  SimpleUI.setMode(400, 460);

  lessonList = new UIList("lessons", 45, 45, 310, 280, 16);
  lessonList.setTitle("Lecciones");
  lessonList.addItem("01 - Variables");
  lessonList.addItem("02 - Condiciones");
  lessonList.addItem("03 - Bucles");
  lessonList.addItem("04 - Funciones");
  lessonList.addItem("05 - Objetos");
  selectedLabel = new UILabel("selected", 45, 350, 310, 44, "Selecciona una leccion", 16);

  SimpleUI.addUIElement(lessonList);
  SimpleUI.addUIElement(selectedLabel);
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == lessonList && action.equals("itemSelected")) {
        selectedLabel.setText("Elegida: " + data);
      }
    }
  });
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
