import simpleui.desktop.SimpleUI;
import simpleui.desktop.UIButton;
import simpleui.desktop.UIImageButton;
import simpleui.desktop.UILabel;
import simpleui.desktop.UIIndicator;
import simpleui.desktop.UICheckbox;
import simpleui.desktop.UISwitch;
import simpleui.desktop.UISlider;
import simpleui.desktop.UIProgressBar;
import simpleui.desktop.UIDropdown;
import simpleui.desktop.UITabs;
import simpleui.desktop.UIList;
import simpleui.desktop.UITable;
import simpleui.desktop.UITextField;
import simpleui.desktop.UITextArea;
import simpleui.desktop.UINumberField;
import simpleui.desktop.UIPanel;
import simpleui.desktop.UISignalMeter;
import simpleui.desktop.chatArea;
import simpleui.desktop.UICalendar;
import simpleui.desktop.UIDate;
import simpleui.desktop.UIScaleMode;
import simpleui.desktop.UIEventHandler;
import simpleui.desktop.UIElement;
import simplecore.Core;
import simplecore.Task;

UIButton actionButton;
UIButton modalButton;
UIImageButton imageButton;
UILabel statusLabel;
UIIndicator indicator;
UICheckbox checkbox;
UISwitch modeSwitch;
UISlider slider;
UIProgressBar progressBar;
UIDropdown dropdown;
UITabs tabs;
UIList itemList;
UITable table;
UITextField textField;
UITextArea textArea;
UINumberField numberField;
UIPanel panel;
UISignalMeter signalMeter;
chatArea chat;
UICalendar calendar;

Core taskCore;
OrbitTask orbitTask;
float progressValue = 0.35;
boolean indicatorOn = true;

void settings() {
  size(1280, 820, P2D);
}

void setup() {
  surface.setResizable(true);
  SimpleUI.initUI(this, "SansSerif", 16, UIScaleMode.FIT);

  taskCore = Core.start(this);
  taskCore.setAutomaticRendering(false);
  orbitTask = new OrbitTask(createDisc(34));

  panel = new UIPanel("panel", 18, 18, 1244, 784);
  statusLabel = new UILabel("status", 38, 30, 720, 38,
    "SimpleUI 0.7.0 · integración Desktop", 20);
  indicator = new UIIndicator("indicator", 1190, 31, 28);
  updateIndicator();

  actionButton = new UIButton("action", 38, 88, 180, 44, "Ejecutar acción", 16);
  modalButton = new UIButton("modal", 232, 88, 180, 44, "Abrir modal", 16);
  imageButton = new UIImageButton("image", 426, 82, 58, 58, createPlayIcon(44), 44, 44);
  checkbox = new UICheckbox("check", 510, 92, 28, "Opción activa", 15);
  modeSwitch = new UISwitch("switch", 770, 90, 285, 40, "Modo intensivo", false, 15);

  slider = new UISlider("slider", 38, 168, 360, 32, 0, 100, 35, 15);
  progressBar = new UIProgressBar("progress", 420, 168, 300, 30);
  progressBar.setProgress(progressValue);
  signalMeter = new UISignalMeter("signal", 750, 148, 305, 78, "Renderer activo");
  signalMeter.setRssi(-88);

  dropdown = new UIDropdown("dropdown", 38, 250, 230, 42,
    new String[]{"FIT", "FILL", "RESPONSIVE", "STRETCH"}, 15);
  tabs = new UITabs("tabs", 288, 250, 330, 42,
    new String[]{"General", "Datos", "Eventos"}, 14);
  textField = new UITextField("text", 640, 250, 270, 42, "Escribe un mensaje", 15);
  numberField = new UINumberField("number", 930, 250, 250, 42, "Valor 0..100", 15);
  numberField.setAllowDecimal(true);
  numberField.setAllowNegative(false);
  numberField.setRange(0, 100);

  itemList = new UIList("list", 38, 330, 260, 205, 14);
  itemList.setTitle("Lista desplazable");
  for (int i = 1; i <= 14; i++) itemList.addItem("Elemento " + i);

  table = new UITable("table", 318, 330, 430, 205,
    new String[]{"Nodo", "Estado", "RSSI"},
    new float[]{0.40, 0.34, 0.26}, 13);
  for (int i = 1; i <= 12; i++) {
    table.addRow(new String[]{"node-" + nf(i, 2), i % 3 == 0 ? "PENDING" : "ACTIVE", str(-55 - i * 3)});
  }

  textArea = new UITextArea("area", 768, 330, 412, 205,
    "Área de texto multilínea con clipping OpenGL...", 14);
  textArea.setText("Prueba de UITextArea en SimpleUI 0.7.0.\n\nRedimensiona la ventana, desplaza listas, escribe texto y abre el modal.\n\nEl contenido debe permanecer dentro de sus controles.");

  chat = new chatArea("chat", 38, 565, 400, 205);
  chat.addMessage(LEFT, "SimpleUI 0.7.0 iniciado", "ahora", true);
  chat.addMessage(RIGHT, "Eventos y transparencias activos", "ahora", true);

  UIDate today = SimpleUI.getUIToday();
  calendar = new UICalendar("calendar", 458, 565, 360, 205, today);
  calendar.addEvent("test", today, "Prueba de integración");
  calendar.addEvent("next", today.plusDays(2), "Validación visual");

  addAllControls();
  installEvents();
}

void addAllControls() {
  SimpleUI.addUIElement(panel);
  SimpleUI.addUIElement(statusLabel);
  SimpleUI.addUIElement(indicator);
  SimpleUI.addUIElement(actionButton);
  SimpleUI.addUIElement(modalButton);
  SimpleUI.addUIElement(imageButton);
  SimpleUI.addUIElement(checkbox);
  SimpleUI.addUIElement(modeSwitch);
  SimpleUI.addUIElement(slider);
  SimpleUI.addUIElement(progressBar);
  SimpleUI.addUIElement(signalMeter);
  SimpleUI.addUIElement(dropdown);
  SimpleUI.addUIElement(tabs);
  SimpleUI.addUIElement(textField);
  SimpleUI.addUIElement(numberField);
  SimpleUI.addUIElement(itemList);
  SimpleUI.addUIElement(table);
  SimpleUI.addUIElement(textArea);
  SimpleUI.addUIElement(chat);
  SimpleUI.addUIElement(calendar);
}

void installEvents() {
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      statusLabel.setText(element.id + " · " + action);

      if (element == actionButton && action.equals("clicked")) {
        progressValue += 0.1;
        if (progressValue > 1) progressValue = 0;
        progressBar.setProgress(progressValue);
        indicatorOn = !indicatorOn;
        updateIndicator();
        chat.addMessage(RIGHT, "Acción ejecutada: " + nf(progressValue * 100, 0, 0) + "%", "ahora", true);
      } else if (element == modalButton && action.equals("clicked")) {
        openTestModal();
      } else if (element == imageButton && action.equals("clicked")) {
        chat.addMessage(LEFT, "UIImageButton pulsado", "ahora", true);
      } else if (element == slider && action.equals("changed")) {
        float value = (Float)data;
        progressBar.setProgress(value / 100.0);
        signalMeter.setRssi(round(-130 + value));
      } else if (element == dropdown && action.equals("changed")) {
        int selected = dropdown.getSelectedIndex();
        if (selected == 0) SimpleUI.setMode(1280, 820, UIScaleMode.FIT);
        if (selected == 1) SimpleUI.setMode(1280, 820, UIScaleMode.FILL);
        if (selected == 2) SimpleUI.setMode(1280, 820, UIScaleMode.RESPONSIVE);
        if (selected == 3) SimpleUI.setMode(1280, 820, UIScaleMode.STRETCH);
      } else if (element == textField && action.equals("submitted")) {
        chat.addMessage(RIGHT, textField.getText(), "ahora", true);
      } else if (element == table && action.equals("rowSelected")) {
        String[] row = (String[])data;
        statusLabel.setText("Tabla: " + row[0] + " · " + row[1] + " · " + row[2]);
      } else if (element == calendar && action.equals("dateSelected")) {
        UIDate date = calendar.getSelectedDate();
        statusLabel.setText("Fecha: " + date.day + "/" + date.month + "/" + date.year);
      }
    }
  });
}

void updateIndicator() {
  indicator.setIndicatorColor(indicatorOn ? color(45, 215, 125) : color(105, 112, 122));
}

void openTestModal() {
  SimpleUI.showConfirmModal(
    "Modal SimpleUI",
    "Comprueba overlay, texto, bordes y transparencia.",
    "Aceptar",
    "Cancelar",
    new Runnable() {
      public void run() { statusLabel.setText("Modal confirmado"); }
    },
    new Runnable() {
      public void run() { statusLabel.setText("Modal cancelado"); }
    }
  );
}

void draw() {
  background(22, 26, 34);

  pushStyle();
  noStroke();
  for (int y = 0; y < height; y += 40) {
    fill(30 + (y / 40) % 2 * 4, 36, 48);
    rect(0, y, width, 40);
  }
  popStyle();

  taskCore.renderTasks();
}

PImage createPlayIcon(int size) {
  PImage icon = createImage(size, size, ARGB);
  icon.loadPixels();
  for (int y = 0; y < size; y++) {
    for (int x = 0; x < size; x++) {
      boolean inside = x > size * 0.27 && x < size * 0.78 &&
        abs(y - size * 0.5) < (x - size * 0.22) * 0.72;
      icon.pixels[y * size + x] = inside ? color(55, 210, 145) : color(0, 0);
    }
  }
  icon.updatePixels();
  return icon;
}

PImage createDisc(int size) {
  PImage image = createImage(size, size, ARGB);
  image.loadPixels();
  float radius = size * 0.48;
  for (int y = 0; y < size; y++) {
    for (int x = 0; x < size; x++) {
      image.pixels[y * size + x] = dist(x, y, size * 0.5, size * 0.5) <= radius
        ? color(255, 185, 65, 210) : color(0, 0);
    }
  }
  image.updatePixels();
  return image;
}

class OrbitTask extends Task {
  OrbitTask(PImage image) {
    setGraph(image);
    x = 1110;
    y = 680;
    z = 1;
  }

  protected void frame() {
    float t = liveFrames * 0.025;
    x = 1080 + cos(t) * 70;
    y = 675 + sin(t) * 55;
    angle += 2;
    alpha = 215;
  }
}
