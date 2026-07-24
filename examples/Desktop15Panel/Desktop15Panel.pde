import simpleui.desktop.*;

UIPanel cardPanel;
UILabel titleLabel;
UILabel detailLabel;

void settings() {
  size(440, 340);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(440, 340);

  cardPanel = new UIPanel("card", 45, 45, 350, 230);
  cardPanel.setTitle("Ficha del proyecto", 18);
  titleLabel = new UILabel("title", 70, 115, 300, 40, "Estacion meteorologica", 18);
  detailLabel = new UILabel("detail", 70, 170, 300, 60, "Equipo: Ada\nEstado: en desarrollo", 15);

  SimpleUI.addUIElement(cardPanel);
  SimpleUI.addUIElement(titleLabel);
  SimpleUI.addUIElement(detailLabel);
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
