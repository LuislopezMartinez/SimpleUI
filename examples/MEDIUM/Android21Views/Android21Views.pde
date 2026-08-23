import simpleui.android.*;

UIViewManager screenManager;
HomeScreen homeScreen;
CourseScreen courseScreen;
UIButton navigationButton;

void settings() {
  fullScreen(P2D);
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.RESPONSIVE);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);

  screenManager = new UIViewManager("screens");
  homeScreen = new HomeScreen(screenManager);
  courseScreen = new CourseScreen(screenManager);
  homeScreen.show();

  navigationButton = new UIButton("navigate", 75, 290, 250, 58, "Ver curso", 18);
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


class HomeScreen extends UIView {
  HomeScreen(UIViewManager manager) { super("home", manager); }

  public void initialize() {
    UILabel title = new UILabel("homeTitle", 25, 90, 350, 54, "Academia SimpleUI", 24);
    title.setTextAlignment(CENTER, CENTER);
    addControl(title);
    addControl(new UILabel("homeInfo", 40, 165, 320, 64, "Aprende creando proyectos", 17));
  }
}

class CourseScreen extends UIView {
  CourseScreen(UIViewManager manager) { super("course", manager); }

  public void initialize() {
    UILabel title = new UILabel("courseTitle", 25, 85, 350, 54, "Curso de Processing", 23);
    title.setTextAlignment(CENTER, CENTER);
    addControl(title);
    addControl(new UILabel("courseInfo", 40, 155, 320, 64, "12 lecciones - Nivel inicial", 17));
  }
}
