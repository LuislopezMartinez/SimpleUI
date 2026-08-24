import simpleui.android.*;

UIPanel cardPanel;
UILabel titleLabel;
UILabel detailLabel;

void settings() {
  SimpleUI.setVideoMode(this, 400, 800, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18,IScaleMode.RESPONSIVE);

  cardPanel = new UIPanel("card", 35, 65, 330, 250);
  cardPanel.setTitle("Ficha del proyecto", 18);
  titleLabel = new UILabel("title", 55, 140, 290, 40, "Estacion meteorologica", 18);
  detailLabel = new UILabel("detail", 55, 195, 290, 64, "Equipo: Ada\nEstado: en desarrollo", 15);

  SimpleUI.addUIElement(cardPanel);
  SimpleUI.addUIElement(titleLabel);
  SimpleUI.addUIElement(detailLabel);
}
