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

public class UIGestureState {
    public UIElement pressedElement = null;
    public UIElement capturedElement = null;
    public UIGestureTarget target = UIGestureTarget.GESTURE_NONE;
    public UIGestureAxis axis = UIGestureAxis.AXIS_UNDECIDED;
    public float startX = 0;
    public float startY = 0;
    public float lastX = 0;
    public float lastY = 0;
    public boolean passedThreshold = false;
    public boolean isTapCandidate = false;

    public void reset() {
        pressedElement = null;
        capturedElement = null;
        target = UIGestureTarget.GESTURE_NONE;
        axis = UIGestureAxis.AXIS_UNDECIDED;
        startX = 0;
        startY = 0;
        lastX = 0;
        lastY = 0;
        passedThreshold = false;
        isTapCandidate = false;
    }
}
