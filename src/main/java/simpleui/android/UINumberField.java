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

public class UINumberField extends UITextInputBase {
    public String textValue = "";
    public String placeholder = "";
    public int fontSize;
    public boolean focused = false;
    public boolean allowDecimal = false;
    public boolean allowNegative = false;
    public boolean useCustomTextColor = false;
    public int customTextColor = color(0);
    public boolean useCustomBorderColor = false;
    public int customBorderColor = color(0);
    public Float minValue = null;
    public Float maxValue = null;
    public char lastChar = 0;
    public int lastCharMs = -99999;

    public UINumberField(String id, int x, int y, int w, int h, String placeholder, int fontSize) {
        super(id, x, y, w, h);
        this.placeholder = placeholder;
        this.fontSize = fontSize;
    }

    public void setAllowDecimal(boolean allowDecimal) {
        this.allowDecimal = allowDecimal;
    }

    public void setAllowNegative(boolean allowNegative) {
        this.allowNegative = allowNegative;
    }

    public boolean allowsDecimal() {
        return allowDecimal;
    }

    public boolean allowsNegative() {
        return allowNegative;
    }

    public void setRange(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public void clearRange() {
        this.minValue = null;
        this.maxValue = null;
    }

    public boolean isValidNumber() {
        Float value = parseValueOrNull(textValue);
        if (value == null) return false;
        if (minValue != null && value < minValue) return false;
        if (maxValue != null && value > maxValue) return false;
        return true;
    }

    public Float getFloatValue() {
        Float value = parseValueOrNull(textValue);
        if (value == null) return null;
        if (minValue != null && value < minValue) return null;
        if (maxValue != null && value > maxValue) return null;
        return value;
    }

    public Integer getIntValue() {
        Float value = getFloatValue();
        if (value == null) return null;
        return round(value);
    }

    public String getText() {
        return textValue;
    }

    public String getOverlayHint() {
        return placeholder;
    }

    public void setText(String t) {
        if (t == null) t = "";
        textValue = sanitizeNumericText(t);
        if (focused) {
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
        if (textValue.length() > 0 && !isValidOrPartial(textValue)) {
            borderC = color(190, 70, 70);
        }

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
        triggerEvent(this, "submitted", getFloatValue());
        focused = false;
        if (activeTextField == this) activeTextField = null;
        closeKeyboard();
    }

    public void appendPrintableChar(char c) {
        if (c == CODED) return;
        if (!isAllowedChar(c)) return;

        int now = millis();
        if (c == lastChar && (now - lastCharMs) < 40) return;
        lastChar = c;
        lastCharMs = now;

        String next = textValue + c;
        String sanitized = sanitizeNumericText(next);
        if (sanitized.equals(textValue)) return;
        textValue = sanitized;
        syncNativeInputFromActiveTextField();
        triggerEvent(this, "changed", textValue);
    }

    public boolean isUsingNativeKeyboardBridge() {
        return nativeInputReady &&
            nativeInputField != null &&
            focused &&
            nativeInputField.hasFocus();
    }

    public boolean isAllowedChar(char c) {
        if (c >= '0' && c <= '9') return true;
        if (allowDecimal && c == '.') return true;
        if (allowNegative && c == '-') return true;
        return false;
    }

    public String sanitizeNumericText(String source) {
        if (source == null || source.length() == 0) return "";

        StringBuilder out = new StringBuilder();
        boolean hasDecimal = false;
        boolean hasSign = false;

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c >= '0' && c <= '9') {
                out.append(c);
                continue;
            }
            if (allowDecimal && c == '.' && !hasDecimal) {
                hasDecimal = true;
                out.append(c);
                continue;
            }
            if (allowNegative && c == '-' && !hasSign && out.length() == 0) {
                hasSign = true;
                out.append(c);
            }
        }
        return out.toString();
    }

    public boolean isValidOrPartial(String source) {
        if (source == null || source.length() == 0) return true;
        if (allowNegative && source.equals("-")) return true;
        if (allowDecimal && source.equals(".")) return true;
        if (allowNegative && allowDecimal && source.equals("-.")) return true;
        return parseValueOrNull(source) != null;
    }

    public Float parseValueOrNull(String source) {
        if (source == null || source.length() == 0) return null;
        if (source.equals("-") || source.equals(".") || source.equals("-.")) return null;
        try {
            return Float.parseFloat(source);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
