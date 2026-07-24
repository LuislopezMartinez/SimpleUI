package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public abstract class UIModal {
  public String id;
  public String title;
  public int modalWidth;
  public int modalHeight;
  public ArrayList<UIElement> controls = new ArrayList<UIElement>();
  public ArrayList<Integer> localX = new ArrayList<Integer>();
  public ArrayList<Integer> localY = new ArrayList<Integer>();
  public boolean initialized = false;
  public boolean visible = false;
  public boolean dismissOnOutsideTap = false;
  public int screenX = 0;
  public int screenY = 0;

  public UIModal(String id, String title, int width, int height) {
    this.id = id == null ? "" : id;
    this.title = title == null ? "" : title;
    this.modalWidth = max(120, width);
    this.modalHeight = max(100, height);
  }

  public abstract void initialize();
  public void onUIEvent(UIElement element, String action, Object data) {}
  public void onShow() {}
  public void onHide() {}

  public final void addControl(UIElement element) {
    if (element == null || controls.contains(element)) return;
    controls.add(element);
    localX.add(element.x);
    localY.add(element.y);
  }

  public final boolean ownsControl(UIElement element) {
    return element != null && controls.contains(element);
  }

  public final void show() {
    if (activeUIModal != null && activeUIModal != this) activeUIModal.hide();
    hideAlertModal();
    if (!initialized) {
      initialize();
      initialized = true;
    }
    visible = true;
    activeUIModal = this;
    closeAllDropdownsExcept(null);
    gestureState.reset();
    layout();
    onShow();
  }

  public final void hide() {
    if (!visible) return;
    if (activeTextField != null && ownsControl(activeTextField)) {
      activeTextField.setFocused(false);
      activeTextField = null;
    }
    visible = false;
    if (activeUIModal == this) activeUIModal = null;
    cleanupCapturedGestureState();
    gestureState.reset();
    onHide();
  }

  public final boolean isVisible() { return visible; }
  public final String getId() { return id; }
  public final void setDismissOnOutsideTap(boolean enabled) { dismissOnOutsideTap = enabled; }

  public void layout() {
    float designWidth = getLogicalWidth();
    float designHeight = getLogicalHeight();
    screenX = round((designWidth - modalWidth) * 0.5f);
    screenY = round(max(10, (designHeight - modalHeight) * 0.5f));
    for (int i = 0; i < controls.size(); i++) {
      controls.get(i).setPosition(screenX + localX.get(i), screenY + localY.get(i));
    }
  }

  public void draw() {
    if (!visible) return;
    layout();
    float designWidth = getLogicalWidth();
    float designHeight = getLogicalHeight();
    pushStyle();
    pushMatrix();
    translate(viewportOffsetX, viewportOffsetY);
    scale(uiScale);
    noStroke();
    fill(0, 150);
    rect(0, 0, designWidth, designHeight);
    stroke(currentTheme.borderColor);
    strokeWeight(2);
    fill(currentTheme.surfaceColor);
    rect(screenX, screenY, modalWidth, modalHeight, BORDER_RADIUS_LARGE);
    if (title.length() > 0) {
      fill(currentTheme.accentColor);
      textAlign(CENTER, TOP);
      textSize(18);
      text(title, screenX + 18, screenY + 16, modalWidth - 36, 28);
    }
    for (UIElement element : controls) element.draw();
    popMatrix();
    popStyle();
  }

  public UIElement findTopMostControlAt(float mx, float my) {
    for (int i = controls.size() - 1; i >= 0; i--) {
      UIElement element = controls.get(i);
      if (!element.isVisible || !element.isEnabled || element instanceof UILabel) continue;
      if (element.containsPoint(mx, my)) return element;
    }
    return null;
  }

  public boolean containsPoint(float mx, float my) {
    return mx >= screenX && mx <= screenX + modalWidth &&
      my >= screenY && my <= screenY + modalHeight;
  }
}
