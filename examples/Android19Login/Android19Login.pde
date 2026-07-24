import simpleui.android.*;

UIPanel loginPanel;
UITextField userField;
UITextField passwordField;
UICheckbox rememberCheckbox;
UIButton loginButton;
UILabel resultLabel;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  loginPanel = new UIPanel("panel", 30, 50, 340, 400);
  loginPanel.setTitle("Acceso al aula", 20);
  userField = new UITextField("user", 55, 130, 290, 52, "Usuario", 17);
  passwordField = new UITextField("password", 55, 200, 290, 52, "Clave de ejemplo", 17);
  rememberCheckbox = new UICheckbox("remember", 55, 275, 32, "Recordar usuario", 15);
  loginButton = new UIButton("login", 80, 340, 240, 56, "Entrar", 18);
  resultLabel = new UILabel("result", 40, 470, 320, 40, "", 15);
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

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
