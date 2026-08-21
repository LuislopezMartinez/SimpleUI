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

public abstract class UITextInputBase extends UIElement {
    public int cursorPosition = 0;
    public long cursorBlinkStartedAt = System.currentTimeMillis();

    public UITextInputBase(String id, int x, int y, int w, int h) {
        super(id, x, y, w, h);
    }

    public abstract String getText();
    public abstract String getOverlayHint();
    public abstract void setText(String t);
    public abstract void setFocused(boolean f);
    public abstract boolean isFocused();
    public abstract void submitAndCloseKeyboard();
    public abstract boolean isUsingNativeKeyboardBridge();

    public void setCursorPosition(int position) {
        String value = getText();
        int length = value == null ? 0 : value.length();
        cursorPosition = constrain(position, 0, length);
        resetCursorBlink();
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void resetCursorBlink() {
        cursorBlinkStartedAt = System.currentTimeMillis();
    }

    public boolean shouldDrawCursor() {
        return isFocused() && isVisible && isEnabled &&
            ((System.currentTimeMillis() - cursorBlinkStartedAt) % 1000L) < 500L;
    }
}
