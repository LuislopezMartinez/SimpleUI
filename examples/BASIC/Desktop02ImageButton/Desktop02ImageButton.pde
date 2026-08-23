import simpleui.desktop.*;

UIImageButton playButton;
UILabel stateLabel;

void settings() {
  size(400, 300, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);

  PImage playIcon = createPlayIcon(72);
  playButton = new UIImageButton("play", 150, 70, 100, 100, playIcon, 72, 72);
  stateLabel = new UILabel("state", 50, 195, 300, 40, "Reproductor detenido", 17);
  stateLabel.setTextAlignment(CENTER, CENTER);

  SimpleUI.addUIElement(playButton);
  SimpleUI.addUIElement(stateLabel);
  SimpleUI.setUIEventHandler(new UIEventHandler() {
    public void onUIEvent(UIElement element, String action, Object data) {
      if (element == playButton && action.equals("clicked")) {
        stateLabel.setText("Reproduciendo...");
      }
    }
  });
}


PImage createPlayIcon(int size) {
  PImage icon = createImage(size, size, ARGB);
  icon.loadPixels();
  for (int y = 0; y < size; y++) {
    for (int x = 0; x < size; x++) {
      boolean inside = x > size * 0.28 && x < size * 0.76
        && abs(y - size * 0.5) < (x - size * 0.22) * 0.72;
      icon.pixels[y * size + x] = inside ? color(40, 180, 110) : color(0, 0);
    }
  }
  icon.updatePixels();
  return icon;
}
