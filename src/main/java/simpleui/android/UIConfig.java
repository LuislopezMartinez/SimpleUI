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

public class UIConfig {
    public int backgroundColor;
    public int surfaceColor;
    public int textColor;
    public int accentColor;
    public int borderColor;
    public int placeholderColor;

    public UIConfig(int bg, int surface, int text, int accent, int border, int placeholder) {
        backgroundColor = bg;
        surfaceColor = surface;
        textColor = text;
        accentColor = accent;
        borderColor = border;
        placeholderColor = placeholder;
    }
}
