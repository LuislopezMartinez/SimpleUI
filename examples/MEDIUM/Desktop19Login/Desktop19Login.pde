import simpleui.desktop.*;

UIPanel loginPanel;
UITextField userField;
UITextField passwordField;
UICheckbox rememberCheckbox;
UIButton loginButton;
UILabel resultLabel;

void settings() {
  size(460, 500, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);

  loginPanel = new UIPanel("panel", 55, 40, 350, 380);
  loginPanel.setTitle("Acceso al aula", 20);
  userField = new UITextField("user", 85, 120, 290, 48, "Usuario", 17);
  passwordField = new UITextField("password", 85, 185, 290, 48, "Clave de ejemplo", 17);
  rememberCheckbox = new UICheckbox("remember", 85, 255, 28, "Recordar usuario", 15);
  loginButton = new UIButton("login", 110, 315, 240, 52, "Entrar", 18);
  resultLabel = new UILabel("result", 70, 435, 320, 36, "", 15);
  resultLabel.setTextAlignment(CENTER, CENTER);

  SimpleUI.addUIElement(loginPanel);
  SimpleUI.addUIElement(userField);
  SimpleUI.addUIElement(passwordField);
  SimpleUI.addUIElement(rememberCheckbox);
  SimpleUI.addUIElement(loginButton);
  SimpleUI.addUIElement(resultLabel);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == loginButton && action.equals("clicked")) validateLogin();
    }
  });
}

void validateLogin() {
  String user = trim(userField.getText());
  String password = trim(passwordField.getText());
  if (user.length() == 0 || password.length() == 0) {
    resultLabel.setText("Completa los dos campos");
    return;
  }
  resultLabel.setText("Bienvenido, " + user + "!");
}
