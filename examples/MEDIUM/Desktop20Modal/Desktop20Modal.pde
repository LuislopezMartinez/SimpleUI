import simpleui.desktop.*;

UIButton deleteButton;
UILabel resultLabel;

void settings() {
  SimpleUI.setVideoMode(this, 460, 320, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);

  deleteButton = new UIButton("delete", 100, 85, 260, 54, "Borrar proyecto", 18);
  resultLabel = new UILabel("result", 55, 180, 350, 48, "El proyecto sigue guardado", 16);
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
