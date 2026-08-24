import simpleui.android.SimpleUI;
import simpleui.android.UIButton;
import simpleui.android.UILabel;
import simpleui.android.UIIndicator;
import simpleui.android.UICheckbox;
import simpleui.android.UISwitch;
import simpleui.android.UISlider;
import simpleui.android.UIProgressBar;
import simpleui.android.UIDropdown;
import simpleui.android.UIList;
import simpleui.android.UITable;
import simpleui.android.UITextField;
import simpleui.android.UITextArea;
import simpleui.android.UISignalMeter;
import simpleui.android.chatArea;
import simpleui.android.UICalendar;
import simpleui.android.UIDate;
import simpleui.android.UIScaleMode;
import simpleui.android.UIEventHandler;
import simpleui.android.UIElement;
import simplecore.Core;
import simplecore.Sound;
import simplecore.Task;

Core taskCore;
Sound music;
Sound errorSound;

UILabel title;
UILabel audioStatus;
UILabel positionStatus;
UIButton playMusic;
UIButton pauseMusic;
UIButton resumeMusic;
UIButton stopMusic;
UIButton rewindMusic;
UIButton playError;
UIButton modalButton;
UISlider volume;
UISwitch loopSwitch;
UIIndicator audioIndicator;
UIProgressBar playbackProgress;
UIDropdown scaleMode;
UICheckbox checkbox;
UISignalMeter signal;
UIList list;
UITable table;
UITextField textField;
UITextArea textArea;
chatArea chat;
UICalendar calendar;

int lastAudioRefresh;

void settings() {
  SimpleUI.setVideoMode(this, 400, 800, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 16,IScaleMode.RESPONSIVE);

  taskCore = Core.start(this);
  taskCore.setAutomaticRendering(false);
  new MarkerTask(createMarker(28));

  music = taskCore.loadSound("2013_15.ogg");
  errorSound = taskCore.loadSound("error.wav");

  title = new UILabel("title", 20, 18, 360, 38,
    "SimpleUI 0.7.2 · Android + audio", 19);
  audioStatus = new UILabel("audioStatus", 20, 58, 360, 34, "", 14);
  positionStatus = new UILabel("position", 20, 92, 360, 30, "", 13);
  audioIndicator = new UIIndicator("audioIndicator", 352, 23, 24);

  playMusic = new UIButton("playMusic", 20, 132, 112, 44, "Play OGG", 14);
  pauseMusic = new UIButton("pauseMusic", 144, 132, 112, 44, "Pausa", 14);
  resumeMusic = new UIButton("resumeMusic", 268, 132, 112, 44, "Continuar", 14);
  stopMusic = new UIButton("stopMusic", 20, 186, 112, 44, "Stop", 14);
  rewindMusic = new UIButton("rewindMusic", 144, 186, 112, 44, "Rebobinar", 14);
  playError = new UIButton("playError", 268, 186, 112, 44, "Play WAV", 14);

  volume = new UISlider("volume", 20, 254, 230, 32, 0, 100, 75, 14);
  loopSwitch = new UISwitch("loop", 265, 246, 115, 44, "Loop", false, 14);
  playbackProgress = new UIProgressBar("playback", 20, 304, 360, 22);

  scaleMode = new UIDropdown("scale", 20, 354, 175, 42,
    new String[]{"RESPONSIVE", "FIT", "FILL", "STRETCH"}, 14);
  checkbox = new UICheckbox("check", 215, 358, 28, "Eventos activos", 14);
  signal = new UISignalMeter("signal", 20, 420, 360, 78, "Android P2D");
  signal.setRssi(-86);

  list = new UIList("list", 20, 525, 360, 190, 14);
  list.setTitle("Lista táctil desplazable");
  for (int i = 1; i <= 18; i++) list.addItem("Elemento " + i);

  table = new UITable("table", 20, 745, 360, 205,
    new String[]{"Nodo", "Estado", "RSSI"},
    new float[]{0.38, 0.37, 0.25}, 13);
  for (int i = 1; i <= 12; i++) {
    table.addRow(new String[]{"node-" + nf(i, 2), i % 3 == 0 ? "WAIT" : "OK", str(-60 - i)});
  }

  textField = new UITextField("text", 20, 980, 360, 44, "Escribe un mensaje", 14);
  textArea = new UITextArea("area", 20, 1042, 360, 145, "Texto multilínea", 14);
  textArea.setText("Prueba Android real con P2D.\n\nDesplaza la pantalla, listas y tabla; abre el teclado y reproduce ambos formatos de sonido.");

  chat = new chatArea("chat", 20, 1215, 360, 180);
  chat.addMessage(LEFT, "Test Android iniciado", "ahora", true);
  chat.addMessage(RIGHT, "OGG y WAV preparados", "ahora", true);

  UIDate today = SimpleUI.getUIToday();
  calendar = new UICalendar("calendar", 20, 1425, 360, 300, today);
  calendar.addEvent("audio", today, "Prueba de audio Android");
  modalButton = new UIButton("modal", 20, 1755, 360, 46, "Abrir modal de prueba", 15);

  addControls();
  installEvents();
  refreshAudioStatus();
}

void addControls() {
  SimpleUI.addUIElement(title);
  SimpleUI.addUIElement(audioStatus);
  SimpleUI.addUIElement(positionStatus);
  SimpleUI.addUIElement(audioIndicator);
  SimpleUI.addUIElement(playMusic);
  SimpleUI.addUIElement(pauseMusic);
  SimpleUI.addUIElement(resumeMusic);
  SimpleUI.addUIElement(stopMusic);
  SimpleUI.addUIElement(rewindMusic);
  SimpleUI.addUIElement(playError);
  SimpleUI.addUIElement(volume);
  SimpleUI.addUIElement(loopSwitch);
  SimpleUI.addUIElement(playbackProgress);
  SimpleUI.addUIElement(scaleMode);
  SimpleUI.addUIElement(checkbox);
  SimpleUI.addUIElement(signal);
  SimpleUI.addUIElement(list);
  SimpleUI.addUIElement(table);
  SimpleUI.addUIElement(textField);
  SimpleUI.addUIElement(textArea);
  SimpleUI.addUIElement(chat);
  SimpleUI.addUIElement(calendar);
  SimpleUI.addUIElement(modalButton);
}

void installEvents() {
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == playMusic && action.equals("clicked")) {
        music.play();
        chat.addMessage(RIGHT, "Reproduciendo 2013_15.ogg", "ahora", true);
      } else if (element == pauseMusic && action.equals("clicked")) {
        music.pause();
      } else if (element == resumeMusic && action.equals("clicked")) {
        music.resume();
      } else if (element == stopMusic && action.equals("clicked")) {
        music.stop();
      } else if (element == rewindMusic && action.equals("clicked")) {
        music.rewind();
      } else if (element == playError && action.equals("clicked")) {
        errorSound.play();
        chat.addMessage(LEFT, "Reproduciendo error.wav", "ahora", true);
      } else if (element == volume && action.equals("changed")) {
        float level = (Float)data / 100.0f;
        music.setVolume(level);
        errorSound.setVolume(level);
      } else if (element == loopSwitch && action.equals("changed")) {
        music.setLoop((Boolean)data);
      } else if (element == scaleMode && action.equals("changed")) {
        int selected = scaleMode.getSelectedIndex();
        if (selected == 0) SimpleUI.setMode(400, 800, UIScaleMode.RESPONSIVE);
        if (selected == 1) SimpleUI.setMode(400, 800, UIScaleMode.FIT);
        if (selected == 2) SimpleUI.setMode(400, 800, UIScaleMode.FILL);
        if (selected == 3) SimpleUI.setMode(400, 800, UIScaleMode.STRETCH);
      } else if (element == textField && action.equals("submitted")) {
        chat.addMessage(RIGHT, textField.getText(), "ahora", true);
      } else if (element == modalButton && action.equals("clicked")) {
        SimpleUI.showConfirmModal(
          "Android Mode", "Audio y controles funcionando bajo P2D.",
          "Aceptar", "Cancelar", null, null);
      }
      refreshAudioStatus();
    }
  });
}

void refreshAudioStatus() {
  String ogg = music.isLoaded() ? "OGG OK" : "OGG ERROR: " + music.getError();
  String wav = errorSound.isLoaded() ? "WAV OK" : "WAV ERROR: " + errorSound.getError();
  audioStatus.setText(ogg + " · " + wav);
  audioIndicator.setIndicatorColor(music.isPlaying() ? color(45, 215, 125) : color(110));

  int position = music.getPosition();
  int duration = music.getDuration();
  positionStatus.setText(
    "Posición " + position + " / " + duration + " ms" +
    (music.isPaused() ? " · PAUSA" : music.isPlaying() ? " · PLAY" : " · STOP"));
  playbackProgress.setProgress(duration > 0 ? constrain(position / float(duration), 0, 1) : 0);
}

void draw() {
  if (millis() - lastAudioRefresh >= 150) {
    lastAudioRefresh = millis();
    refreshAudioStatus();
  }
  taskCore.renderTasks();
}

PImage createMarker(int size) {
  PImage image = createImage(size, size, ARGB);
  image.loadPixels();
  for (int py = 0; py < size; py++) {
    for (int px = 0; px < size; px++) {
      image.pixels[py * size + px] = dist(px, py, size * 0.5f, size * 0.5f) < size * 0.46f
        ? color(255, 178, 55, 220) : color(0, 0);
    }
  }
  image.updatePixels();
  return image;
}

class MarkerTask extends Task {
  MarkerTask(PImage graph) {
    setGraph(graph);
    x = 350;
    y = 470;
    tint(color(80, 190, 255));
  }

  protected void frame() {
    angle += 2;
    scale(0.85f + sin(liveFrames * 0.05f) * 0.15f);
  }
}
