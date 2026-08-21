package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UITextArea extends UITextInputBase {
  public static class VisualLine {
    public String text;
    public int start;
    public int end;
    public VisualLine(String text, int start, int end) { this.text = text; this.start = start; this.end = end; }
  }
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
    setCursorPosition(textValue.length());
    ensureCursorVisible();
  }

  public void setTextColor(int c) { useCustomTextColor = true; customTextColor = c; }
  public void clearTextColor() { useCustomTextColor = false; }
  public void setBorderColor(int c) { useCustomBorderColor = true; customBorderColor = c; }
  public void clearBorderColor() { useCustomBorderColor = false; }
  public void setFocused(boolean f) { focused = f; resetCursorBlink(); if (!focused && activeTextField == this) activeTextField = null; }
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
    ArrayList<VisualLine> lines = visualLines(availableWidth);
    float contentHeight = lines.size() * lineHeight;
    float minScroll = min(0, visibleHeight - contentHeight);
    internalScrollY = constrain(internalScrollY, minScroll, 0);
    clip(x + textPadding, y + textPadding, availableWidth, visibleHeight);

    if (textValue.length() == 0) {
      fill(currentTheme.placeholderColor);
      text(placeholder, x + textPadding, y + textPadding, availableWidth, visibleHeight);
      if (shouldDrawCursor()) drawCursorAt(x + textPadding, y + textPadding + internalScrollY);
      noClip();
      return;
    }

    fill(useCustomTextColor ? customTextColor : currentTheme.textColor);
    for (int i = 0; i < lines.size(); i++) {
      float lineY = y + textPadding + internalScrollY + i * lineHeight;
      if (lineY + lineHeight < y + textPadding || lineY > y + height - textPadding) continue;
      text(lines.get(i).text, x + textPadding, lineY);
    }
    if (shouldDrawCursor()) {
      int lineIndex = cursorLineIndex(lines);
      VisualLine cursorLine = lines.get(lineIndex);
      int column = constrain(cursorPosition - cursorLine.start, 0, cursorLine.text.length());
      float cursorX = x + textPadding + textWidth(cursorLine.text.substring(0, column));
      float cursorY = y + textPadding + internalScrollY + lineIndex * lineHeight;
      drawCursorAt(cursorX, cursorY);
    }
    noClip();
  }

  public void drawCursorAt(float cursorX, float cursorY) {
    stroke(useCustomTextColor ? customTextColor : currentTheme.textColor);
    strokeWeight(1);
    line(cursorX, cursorY, cursorX, cursorY + fontSize);
  }

  public void mousePressed() {
  }

  public void performTapAction() { performTapAction(getScaledMouseX(), getScaledMouseY()); }

  public void performTapAction(float mx, float my) {
    if (!isEnabled) return;
    focused = true;
    activeTextField = this;
    textSize(fontSize);
    ArrayList<VisualLine> lines = visualLines(max(0, width - 16));
    int lineIndex = constrain(floor((my - (y + 8) - internalScrollY) / lineHeight), 0, lines.size() - 1);
    VisualLine line = lines.get(lineIndex);
    float localX = constrain(mx - (x + 8), 0, max(0, width - 16));
    setCursorPosition(line.start + closestCharacterIndex(line.text, localX));
    ensureCursorVisible();
  }

  public int closestCharacterIndex(String source, float localX) {
    for (int i = 0; i < source.length(); i++) {
      float left = textWidth(source.substring(0, i));
      float right = textWidth(source.substring(0, i + 1));
      if (localX < (left + right) / 2) return i;
    }
    return source.length();
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
      if (cursorPosition > 0) {
        textValue = textValue.substring(0, cursorPosition - 1) + textValue.substring(cursorPosition);
        setCursorPosition(cursorPosition - 1);
        triggerEvent(this, "changed", textValue);
        ensureCursorVisible();
      }
      return;
    }
    if (keyCode == DELETE) {
      if (cursorPosition < textValue.length()) {
        textValue = textValue.substring(0, cursorPosition) + textValue.substring(cursorPosition + 1);
        resetCursorBlink();
        triggerEvent(this, "changed", textValue);
        ensureCursorVisible();
      }
      return;
    }
    if (keyCode == LEFT) { setCursorPosition(cursorPosition - 1); ensureCursorVisible(); return; }
    if (keyCode == RIGHT) { setCursorPosition(cursorPosition + 1); ensureCursorVisible(); return; }
    if (keyCode == UP) { moveCursorVertically(-1); return; }
    if (keyCode == DOWN) { moveCursorVertically(1); return; }
    if (keyCode == 36) { moveCursorToVisualLineEdge(false); return; }
    if (keyCode == 35) { moveCursorToVisualLineEdge(true); return; }
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
    String next = textValue.substring(0, cursorPosition) + c + textValue.substring(cursorPosition);
    if (uppercase) next = next.toUpperCase();
    if (next.length() <= maxLen) {
      textValue = next;
      setCursorPosition(cursorPosition + 1);
      triggerEvent(this, "changed", textValue);
      ensureCursorVisible();
    }
  }

  public void scrollToBottom() {
    internalScrollY = minScrollForCurrentContent();
  }

  public float minScrollForCurrentContent() {
    float textPadding = 8;
    float availableWidth = max(0, width - textPadding * 2);
    float visibleHeight = max(0, height - textPadding * 2);
    ArrayList<VisualLine> lines = visualLines(availableWidth);
    float contentHeight = lines.size() * lineHeight;
    return min(0, visibleHeight - contentHeight);
  }

  public boolean hasScrollableOverflow() {
    return minScrollForCurrentContent() < 0;
  }

  public ArrayList<String> wrappedLines(float availableWidth) {
    ArrayList<String> result = new ArrayList<String>();
    for (VisualLine line : visualLinesForText(textValue.length() == 0 ? placeholder : textValue, availableWidth)) result.add(line.text);
    return result;
  }

  public ArrayList<VisualLine> visualLines(float availableWidth) { return visualLinesForText(textValue, availableWidth); }

  public ArrayList<VisualLine> visualLinesForText(String source, float availableWidth) {
    ArrayList<VisualLine> result = new ArrayList<VisualLine>();
    if (source == null || source.length() == 0) { result.add(new VisualLine("", 0, 0)); return result; }
    int paragraphStart = 0;
    while (paragraphStart <= source.length()) {
      int newline = source.indexOf('\n', paragraphStart);
      int paragraphEnd = newline < 0 ? source.length() : newline;
      if (paragraphStart == paragraphEnd) result.add(new VisualLine("", paragraphStart, paragraphStart));
      int position = paragraphStart;
      while (position < paragraphEnd) {
        String remaining = source.substring(position, paragraphEnd);
        int fit = max(1, charsThatFit(remaining, availableWidth));
        int end = min(paragraphEnd, position + fit);
        result.add(new VisualLine(source.substring(position, end), position, end));
        position = end;
      }
      if (newline < 0) break;
      paragraphStart = newline + 1;
    }
    return result;
  }

  public int cursorLineIndex(ArrayList<VisualLine> lines) {
    int result = lines.size() - 1;
    for (int i = 0; i < lines.size(); i++) {
      VisualLine line = lines.get(i);
      if (cursorPosition < line.end || (cursorPosition == line.end &&
        (i == lines.size() - 1 || lines.get(i + 1).start != cursorPosition))) return i;
      if (cursorPosition >= line.start) result = i;
    }
    return result;
  }

  public void moveCursorVertically(int direction) {
    textSize(fontSize);
    ArrayList<VisualLine> lines = visualLines(max(0, width - 16));
    int current = cursorLineIndex(lines);
    VisualLine currentLine = lines.get(current);
    int column = constrain(cursorPosition - currentLine.start, 0, currentLine.text.length());
    float desiredX = textWidth(currentLine.text.substring(0, column));
    int targetIndex = constrain(current + direction, 0, lines.size() - 1);
    VisualLine target = lines.get(targetIndex);
    setCursorPosition(target.start + closestCharacterIndex(target.text, desiredX));
    ensureCursorVisible();
  }

  public void moveCursorToVisualLineEdge(boolean end) {
    ArrayList<VisualLine> lines = visualLines(max(0, width - 16));
    VisualLine line = lines.get(cursorLineIndex(lines));
    setCursorPosition(end ? line.end : line.start);
    ensureCursorVisible();
  }

  public void ensureCursorVisible() {
    textSize(fontSize);
    float visibleHeight = max(0, height - 16);
    ArrayList<VisualLine> lines = visualLines(max(0, width - 16));
    int lineIndex = cursorLineIndex(lines);
    float top = lineIndex * lineHeight + internalScrollY;
    if (top < 0) internalScrollY -= top;
    else if (top + lineHeight > visibleHeight) internalScrollY -= top + lineHeight - visibleHeight;
    internalScrollY = constrain(internalScrollY, min(0, visibleHeight - lines.size() * lineHeight), 0);
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
