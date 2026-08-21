package simpleui.android;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.android.SimpleUI.*;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.text.TextWatcher;
import android.text.Editable;
import android.graphics.Rect;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.os.Build;

public class UITextField extends UITextInputBase {
    public String textValue = "";
    public String placeholder = "";
    public int fontSize;
    public int maxLen = 16;
    public boolean focused = false;
    public boolean uppercase = false;
    public char lastChar = 0;
    public int lastCharMs = -99999;
    public boolean useCustomTextColor = false;
    public int customTextColor = color(0);
    public boolean useCustomBorderColor = false;
    public int customBorderColor = color(0);
    public int visibleTextStart = 0;

    public UITextField(String id, int x, int y, int w, int h, String placeholder, int fontSize) {
        super(id, x, y, w, h);
        this.placeholder = placeholder;
        this.fontSize = fontSize;
    }

    public void setMaxLen(int maxLen) {
        this.maxLen = max(1, maxLen);
    }

    public void setUppercase(boolean uppercase) {
        this.uppercase = uppercase;
    }

    public String getText() {
        return textValue;
    }

    public String getOverlayHint() {
        return placeholder;
    }

    public void setText(String t) {
        if (t == null) t = "";
        if (uppercase) t = t.toUpperCase();
        if (t.length() > maxLen) t = t.substring(0, maxLen);
        textValue = t;
        setCursorPosition(textValue.length());
        if (focused && !(nativeInputReady && nativeInputField != null && nativeInputField.hasFocus())) {
            syncNativeInputFromActiveTextField();
        }
    }

    public void setTextColor(int c) {
        useCustomTextColor = true;
        customTextColor = c;
    }

    public void clearTextColor() {
        useCustomTextColor = false;
    }

    public void setBorderColor(int c) {
        useCustomBorderColor = true;
        customBorderColor = c;
    }

    public void clearBorderColor() {
        useCustomBorderColor = false;
    }

    public void setFocused(boolean f) {
        focused = f;
        resetCursorBlink();
        if (!focused && activeTextField == this) {
            activeTextField = null;
        }
    }

    public boolean isFocused() {
        return focused;
    }

    public void draw() {
        if (!isVisible) return;

        int borderC = focused ? currentTheme.accentColor : currentTheme.borderColor;
        if (useCustomBorderColor) borderC = customBorderColor;
        stroke(borderC);
        strokeWeight(2);
        fill(currentTheme.surfaceColor);
        rect(x, y, width, height, BORDER_RADIUS_MEDIUM);

        textAlign(LEFT, CENTER);
        textSize(fontSize);
        float textPadding = 10;
        float availableWidth = max(0, width - textPadding * 2);
        if (textValue.length() == 0) {
            fill(currentTheme.placeholderColor);
            String visiblePlaceholder = tailThatFits(placeholder, availableWidth);
            text(visiblePlaceholder, x + textPadding, y + height / 2);
        } else {
            fill(useCustomTextColor ? customTextColor : currentTheme.textColor);
            updateVisibleTextStart(availableWidth);
            String visibleText = visibleText(availableWidth);
            text(visibleText, x + textPadding, y + height / 2);
            if (shouldDrawCursor()) {
                int localCursor = constrain(cursorPosition - visibleTextStart, 0, visibleText.length());
                float cursorX = x + textPadding + textWidth(visibleText.substring(0, localCursor));
                stroke(useCustomTextColor ? customTextColor : currentTheme.textColor);
                strokeWeight(1);
                line(cursorX, y + (height - fontSize) / 2, cursorX, y + (height + fontSize) / 2);
            }
        }
        if (textValue.length() == 0 && shouldDrawCursor()) {
            stroke(useCustomTextColor ? customTextColor : currentTheme.textColor);
            strokeWeight(1);
            line(x + textPadding, y + (height - fontSize) / 2, x + textPadding, y + (height + fontSize) / 2);
        }
    }

    public void updateVisibleTextStart(float availableWidth) {
        cursorPosition = constrain(cursorPosition, 0, textValue.length());
        visibleTextStart = constrain(visibleTextStart, 0, cursorPosition);
        while (visibleTextStart < cursorPosition &&
            textWidth(textValue.substring(visibleTextStart, cursorPosition)) > availableWidth) visibleTextStart++;
        while (visibleTextStart > 0 &&
            textWidth(textValue.substring(visibleTextStart - 1, cursorPosition)) <= availableWidth) visibleTextStart--;
    }

    public String visibleText(float availableWidth) {
        int end = visibleTextStart;
        while (end < textValue.length() &&
            textWidth(textValue.substring(visibleTextStart, end + 1)) <= availableWidth) end++;
        return textValue.substring(visibleTextStart, end);
    }

    public String tailThatFits(String source, float availableWidth) {
        if (source == null || source.length() == 0) return "";
        if (textWidth(source) <= availableWidth) return source;

        int start = source.length() - 1;
        String visible = source.substring(start);
        while (start > 0) {
            String candidate = source.substring(start - 1);
            if (textWidth(candidate) > availableWidth) {
                break;
            }
            start--;
            visible = candidate;
        }
        return visible;
    }

    public void mousePressed() {
    }

    public void performTapAction() { performTapAction(getScaledMouseX(), getScaledMouseY()); }

    public void performTapAction(float mx, float my) {
        if (!isEnabled) return;
        focused = true;
        activeTextField = this;
        textSize(fontSize);
        updateVisibleTextStart(max(0, width - 20));
        String visible = visibleText(max(0, width - 20));
        float localX = constrain(mx - (x + 10), 0, max(0, width - 20));
        setCursorPosition(visibleTextStart + closestCharacterIndex(visible, localX));
        openKeyboard();
    }

    public int closestCharacterIndex(String source, float localX) {
        for (int i = 0; i < source.length(); i++) {
            float left = textWidth(source.substring(0, i));
            float right = textWidth(source.substring(0, i + 1));
            if (localX < (left + right) / 2) return i;
        }
        return source.length();
    }

    public void keyPressed() {
        if (!focused || !isEnabled) return;
        if (isUsingNativeKeyboardBridge()) return;

        if (key == ENTER || key == RETURN) {
            submitAndCloseKeyboard();
            return;
        }

        if (keyCode == BACKSPACE) {
            if (cursorPosition > 0) {
                textValue = textValue.substring(0, cursorPosition - 1) + textValue.substring(cursorPosition);
                setCursorPosition(cursorPosition - 1);
                syncNativeInputFromActiveTextField();
                triggerEvent(this, "changed", textValue);
            }
            return;
        }

        if (keyCode == DELETE) {
            if (cursorPosition < textValue.length()) {
                textValue = textValue.substring(0, cursorPosition) + textValue.substring(cursorPosition + 1);
                resetCursorBlink();
                syncNativeInputFromActiveTextField();
                triggerEvent(this, "changed", textValue);
            }
            return;
        }
        if (keyCode == LEFT) { setCursorPosition(cursorPosition - 1); syncNativeSelectionFromActiveTextField(); return; }
        if (keyCode == RIGHT) { setCursorPosition(cursorPosition + 1); syncNativeSelectionFromActiveTextField(); return; }
        if (keyCode == 36) { setCursorPosition(0); syncNativeSelectionFromActiveTextField(); return; }
        if (keyCode == 35) { setCursorPosition(textValue.length()); syncNativeSelectionFromActiveTextField(); return; }

        // Fallback Android: algunos teclados virtuales reportan caracteres por keyPressed.
        appendPrintableChar(key);

    }

    public void keyTyped() {
        if (!focused || !isEnabled) return;
        if (isUsingNativeKeyboardBridge()) return;
        if (key == '\n' || key == '\r') {
            submitAndCloseKeyboard();
            return;
        }
        appendPrintableChar(key);
    }

    public void submitAndCloseKeyboard() {
        triggerEvent(this, "submitted", textValue);
        focused = false;
        if (activeTextField == this) activeTextField = null;
        closeKeyboard();
    }

    public void appendPrintableChar(char c) {
        if (c < 32 || c == CODED) return;

        // Evitar doble insercion cuando Android emite keyPressed + keyTyped para la misma tecla.
        int now = millis();
        if (c == lastChar && (now - lastCharMs) < 40) return;
        lastChar = c;
        lastCharMs = now;

        String next = textValue.substring(0, cursorPosition) + c + textValue.substring(cursorPosition);
        if (uppercase) next = next.toUpperCase();
        if (next.length() <= maxLen) {
            textValue = next;
            setCursorPosition(cursorPosition + 1);
            syncNativeInputFromActiveTextField();
            triggerEvent(this, "changed", textValue);
        }
    }

    public boolean isUsingNativeKeyboardBridge() {
        return nativeInputReady &&
            nativeInputField != null &&
            focused &&
            nativeInputField.hasFocus();
    }
}
