package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UIImageButton extends UIButton {
  public PImage imageAsset;
  public int imageWidth;
  public int imageHeight;

  public UIImageButton(String id, int x, int y, int w, int h, PImage imageAsset) {
    this(id, x, y, w, h, imageAsset,
      imageAsset == null ? 0 : imageAsset.width,
      imageAsset == null ? 0 : imageAsset.height);
  }

  public UIImageButton(String id, int x, int y, int w, int h, PImage imageAsset, int imageWidth, int imageHeight) {
    super(id, x, y, w, h, "", 1);
    this.imageAsset = imageAsset;
    this.imageWidth = max(0, imageWidth);
    this.imageHeight = max(0, imageHeight);
  }

  public void setImage(PImage imageAsset) { this.imageAsset = imageAsset; }

  public void setImageSize(int imageWidth, int imageHeight) {
    this.imageWidth = max(0, imageWidth);
    this.imageHeight = max(0, imageHeight);
  }

  public void draw() {
    if (!isVisible) return;
    super.draw();
    if (imageAsset == null || imageWidth <= 0 || imageHeight <= 0) return;
    pushStyle();
    imageMode(CENTER);
    tint(255, isEnabled ? 255 : 110);
    image(imageAsset, x + width * 0.5f, y + height * 0.5f, imageWidth, imageHeight);
    noTint();
    popStyle();
  }
}
