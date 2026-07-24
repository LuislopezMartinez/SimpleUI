import simpleui.android.*;

UISignalMeter signal;
UISlider level;
UITabs tabs;
UILabel status;
chatArea chat;

void settings() {
  fullScreen();
}

void setup() {
  orientation(PORTRAIT);
  SimpleUI.initUI(this, "SansSerif", 16);
  SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);
  signal = new UISignalMeter("signal", 25, 35, 350, 85, "LoRa");
  signal.setRssi(-92);
  level = new UISlider("level", 25, 170, 350, 32, 0, 100, 50, 16);
  tabs = new UITabs("tabs", 25, 230, 350, 44,
    new String[]{ "Status", "Messages" }, 15);
  status = new UILabel("status", 25, 295, 350, 40, "Level: 50.00", 16);
  chat = new chatArea("chat", 25, 350, 350, 250);
  chat.addMessage(LEFT, "Unified Desktop/Android API", "10:30", true);
  SimpleUI.addUIElement(signal);
  SimpleUI.addUIElement(level);
  SimpleUI.addUIElement(tabs);
  SimpleUI.addUIElement(status);
  SimpleUI.addUIElement(chat);
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == level && action.equals("changed")) {
        status.setText("Level: " + nf((Float)data, 0, 2));
      }
    }
  });
}

void draw() {
  background(SimpleUI.currentTheme.backgroundColor);
  SimpleUI.updateAndDrawUI();
}
