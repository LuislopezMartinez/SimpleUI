import simpleui.android.*;

UITable gradeTable;
UILabel selectedLabel;

void settings() {
  fullScreen();
}

void setup() {
  orientation(LANDSCAPE);
  SimpleUI.initUI(this, "SansSerif", 17);
  SimpleUI.setMode(520, 800, UIScaleMode.RESPONSIVE);

  String[] headers = { "Alumno", "Proyecto", "Nota" };
  float[] widths = { 0.40, 0.38, 0.22 };
  gradeTable = new UITable("grades", 35, 45, 450, 250, headers, widths, 15);
  gradeTable.addRow(new String[]{ "Ana", "Robot", "9" });
  gradeTable.addRow(new String[]{ "Bruno", "Juego", "8" });
  gradeTable.addRow(new String[]{ "Carla", "Sensor", "10" });
  selectedLabel = new UILabel("selected", 35, 320, 450, 44, "Selecciona una fila", 16);

  SimpleUI.addUIElement(gradeTable);
  SimpleUI.addUIElement(selectedLabel);
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == gradeTable && action.equals("rowSelected")) {
        String[] row = (String[])data;
        selectedLabel.setText(row[0] + " obtuvo un " + row[2]);
      }
    }
  });
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
