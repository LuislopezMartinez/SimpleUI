import simpleui.desktop.*;

UIViewManager screenManager;
HomeScreen homeScreen;
CourseScreen courseScreen;
UIButton navigationButton;

void settings() {
  size(460, 360);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(460, 360);

  screenManager = new UIViewManager("screens");
  homeScreen = new HomeScreen(screenManager);
  courseScreen = new CourseScreen(screenManager);
  homeScreen.show();

  navigationButton = new UIButton("navigate", 105, 245, 250, 54, "Ver curso", 18);
  SimpleUI.addUIElement(navigationButton);

  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element != navigationButton || !action.equals("clicked")) return;
      if (screenManager.getActiveView() == homeScreen) {
        courseScreen.show();
        navigationButton.setLabel("Volver");
      } else {
        homeScreen.show();
        navigationButton.setLabel("Ver curso");
      }
    }
  });
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}

class HomeScreen extends UIView {
  HomeScreen(UIViewManager manager) { super("home", manager); }

  public void initialize() {
    UILabel title = new UILabel("homeTitle", 55, 70, 350, 50, "Academia SimpleUI", 24);
    title.setTextAlignment(CENTER, CENTER);
    addControl(title);
    addControl(new UILabel("homeInfo", 70, 140, 320, 60, "Aprende creando proyectos", 17));
  }
}

class CourseScreen extends UIView {
  CourseScreen(UIViewManager manager) { super("course", manager); }

  public void initialize() {
    UILabel title = new UILabel("courseTitle", 55, 65, 350, 50, "Curso de Processing", 23);
    title.setTextAlignment(CENTER, CENTER);
    addControl(title);
    addControl(new UILabel("courseInfo", 70, 130, 320, 60, "12 lecciones - Nivel inicial", 17));
  }
}
