package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UITextArea extends UITextInputBase {
  public String textValue = "";
  public String placeholder = "";
  public int fontSize;
  public int maxLen = 512;
  public boolean focused = false;
  public boolean uppercase = false;
  public char lastChar = 0;
  public int lastCharMs = -99999;
  public boolean useCustomTextColor = false;
  public int customTextColor = color(0);
  public boolean useCustomBorderColor = false;
  public int customBorderColor = color(0);
  public float internalScrollY = 0;
  public float lineHeight;

  public UITextArea(String id, int x, int y, int w, int h, String placeholder, int fontSize) {
    super(id, x, y, w, h);
    this.placeholder = placeholder;
    this.fontSize = fontSize;
    this.lineHeight = fontSize * 1.5f;
  }

  public void setMaxLen(int maxLen) { this.maxLen = max(1, maxLen); }
  public void setUppercase(boolean uppercase) { this.uppercase = uppercase; }
  public String getText() { return textValue; }
  public String getOverlayHint() { return placeholder; }

  public void setText(String t) {
    if (t == null) t = "";
    if (uppercase) t = t.toUpperCase();
    if (t.length() > maxLen) t = t.substring(0, maxLen);
    textValue = t;
    scrollToBottom();
  }

  public void setTextColor(int c) { useCustomTextColor = true; customTextColor = c; }
  public void clearTextColor() { useCustomTextColor = false; }
  public void setBorderColor(int c) { useCustomBorderColor = true; customBorderColor = c; }
  public void clearBorderColor() { useCustomBorderColor = false; }
  public void setFocused(boolean f) { focused = f; if (!focused && activeTextField == this) activeTextField = null; }
  public boolean isFocused() { return focused; }

  public void draw() {
    if (!isVisible) return;
    int borderC = focused ? currentTheme.accentColor : currentTheme.borderColor;
    if (useCustomBorderColor) borderC = customBorderColor;
    stroke(borderC);
    strokeWeight(2);
    fill(currentTheme.surfaceColor);
    rect(x, y, width, height, BORDER_RADIUS_MEDIUM);

    float textPadding = 8;
    float availableWidth = max(0, width - textPadding * 2);
    float visibleHeight = max(0, height - textPadding * 2);
    textAlign(LEFT, TOP);
    textSize(fontSize);
    ArrayList<String> lines = wrappedLines(availableWidth);
    float contentHeight = lines.size() * lineHeight;
    float minScroll = min(0, visibleHeight - contentHeight);
    internalScrollY = constrain(internalScrollY, minScroll, 0);

    if (textValue.length() == 0) {
      fill(currentTheme.placeholderColor);
      text(placeholder, x + textPadding, y + textPadding, availableWidth, visibleHeight);
      return;
    }

    fill(useCustomTextColor ? customTextColor : currentTheme.textColor);
    for (int i = 0; i < lines.size(); i++) {
      float lineY = y + textPadding + internalScrollY + i * lineHeight;
      if (lineY + lineHeight < y + textPadding || lineY > y + height - textPadding) continue;
      text(lines.get(i), x + textPadding, lineY);
    }
  }

  public void mousePressed() {
  }

  public void performTapAction() {
    if (!isEnabled) return;
    focused = true;
    activeTextField = this;
  }

  public void mouseDragged() {
    if (!isEnabled) return;
    float mx = getScaledMouseX();
    float my = getScaledMouseY();
    if (containsPoint(mx, my)) {
      internalScrollY += (mouseY - pmouseY) / uiScale;
      internalScrollY = constrain(internalScrollY, minScrollForCurrentContent(), 0);
    }
  }

  public void keyPressed() {
    if (!focused || !isEnabled) return;
    if (keyCode == BACKSPACE) {
      if (textValue.length() > 0) {
        textValue = textValue.substring(0, textValue.length() - 1);
        triggerEvent(this, "changed", textValue);
        scrollToBottom();
      }
      return;
    }
    if (key == TAB) return;
    if (key == ENTER || key == RETURN) {
      appendPrintableChar('\n');
      return;
    }
    // La escritura normal y los caracteres compuestos llegan por keyTyped().
  }

  public void keyTyped() {
    if (!focused || !isEnabled) return;
    if (key == '\r') return;
    if (key == '\n') return;
    appendPrintableChar(key);
  }

  public void submitAndCloseKeyboard() {
    triggerEvent(this, "submitted", textValue);
  }

  public void appendPrintableChar(char c) {
    if ((c < 32 || c == CODED) && c != '\n' && c != '\t') return;
    int now = millis();
    if (c == lastChar && (now - lastCharMs) < 40 && c != '\n') return;
    lastChar = c;
    lastCharMs = now;
    String next = textValue + c;
    if (uppercase) next = next.toUpperCase();
    if (next.length() <= maxLen) {
      textValue = next;
      triggerEvent(this, "changed", textValue);
      scrollToBottom();
    }
  }

  public void scrollToBottom() {
    internalScrollY = minScrollForCurrentContent();
  }

  public float minScrollForCurrentContent() {
    float textPadding = 8;
    float availableWidth = max(0, width - textPadding * 2);
    float visibleHeight = max(0, height - textPadding * 2);
    ArrayList<String> lines = wrappedLines(availableWidth);
    float contentHeight = lines.size() * lineHeight;
    return min(0, visibleHeight - contentHeight);
  }

  public boolean hasScrollableOverflow() {
    return minScrollForCurrentContent() < 0;
  }

  public ArrayList<String> wrappedLines(float availableWidth) {
    ArrayList<String> result = new ArrayList<String>();
    String[] rawLines = split(textValue.length() == 0 ? placeholder : textValue, '\n');
    if (rawLines == null || rawLines.length == 0) {
      result.add("");
      return result;
    }
    for (int i = 0; i < rawLines.length; i++) {
      String line = rawLines[i];
      if (line.length() == 0) {
        result.add("");
        continue;
      }
      while (line.length() > 0) {
        int fit = charsThatFit(line, availableWidth);
        if (fit <= 0) fit = 1;
        result.add(line.substring(0, fit));
        line = line.substring(fit);
      }
    }
    return result;
  }

  public int charsThatFit(String source, float availableWidth) {
    int best = 0;
    for (int i = 1; i <= source.length(); i++) {
      String part = source.substring(0, i);
      if (textWidth(part) > availableWidth) break;
      best = i;
    }
    return best;
  }

  public boolean isUsingNativeKeyboardBridge() { return false; }
}
