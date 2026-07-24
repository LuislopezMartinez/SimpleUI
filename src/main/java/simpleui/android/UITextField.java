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
            String visibleText = tailThatFits(textValue, availableWidth);
            text(visibleText, x + textPadding, y + height / 2);
        }
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

    public void performTapAction() {
        if (!isEnabled) return;
        focused = true;
        activeTextField = this;
        openKeyboard();
    }

    public void keyPressed() {
        if (!focused || !isEnabled) return;
        if (isUsingNativeKeyboardBridge()) return;

        if (key == ENTER || key == RETURN) {
            submitAndCloseKeyboard();
            return;
        }

        if (keyCode == BACKSPACE) {
            if (textValue.length() > 0) {
                textValue = textValue.substring(0, textValue.length() - 1);
                syncNativeInputFromActiveTextField();
                triggerEvent(this, "changed", textValue);
            }
            return;
        }

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

        String next = textValue + c;
        if (uppercase) next = next.toUpperCase();
        if (next.length() <= maxLen) {
            textValue = next;
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
