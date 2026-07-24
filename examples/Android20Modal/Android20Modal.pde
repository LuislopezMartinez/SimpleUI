import simpleui.android.*;

UIButton deleteButton;
UILabel resultLabel;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  deleteButton = new UIButton("delete", 70, 105, 260, 58, "Borrar proyecto", 18);
  resultLabel = new UILabel("result", 40, 205, 320, 52, "El proyecto sigue guardado", 16);
  resultLabel.setTextAlignment(CENTER, CENTER);
  SimpleUI.addUIElement(deleteButton);
  SimpleUI.addUIElement(resultLabel);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == deleteButton && action.equals("clicked")) askForConfirmation();
    }
  });
}

void askForConfirmation() {
  SimpleUI.showConfirmModal(
    "Confirmar borrado",
    "Esta accion no se puede deshacer.",
    "Borrar",
    "Cancelar",
    new Runnable() {
      public void run() { resultLabel.setText("Proyecto borrado"); }
    },
    new Runnable() {
      public void run() { resultLabel.setText("Operacion cancelada"); }
    }
  );
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
