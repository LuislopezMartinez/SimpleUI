import simpleui.desktop.*;

chatArea classroomChat;

void settings() {
  SimpleUI.setVideoMode(this, 440, 540, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 16, UIScaleMode.FIT);

  classroomChat = new chatArea("chat", 35, 40, 370, 450);
  classroomChat.addMessage(LEFT, "Ya funciona el sensor?", "Ana - 10:15", true);
  classroomChat.addMessage(RIGHT, "Si, envia un dato cada segundo.", "Bruno - 10:16", true);
  classroomChat.addMessage(LEFT, "Perfecto. Ahora dibujamos la grafica.", "Ana - 10:17", false);
  SimpleUI.addUIElement(classroomChat);
}
