import simpleui.android.*;

chatArea classroomChat;

void settings() {
  SimpleUI.setVideoMode(this, 400, 800, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 16,IScaleMode.RESPONSIVE);

  classroomChat = new chatArea("chat", 25, 55, 350, 500);
  classroomChat.addMessage(LEFT, "Ya funciona el sensor?", "Ana - 10:15", true);
  classroomChat.addMessage(RIGHT, "Si, envia un dato cada segundo.", "Bruno - 10:16", true);
  classroomChat.addMessage(LEFT, "Perfecto. Ahora dibujamos la grafica.", "Ana - 10:17", false);
  SimpleUI.addUIElement(classroomChat);
}
