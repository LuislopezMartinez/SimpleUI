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

public class UISlider extends UIElement {
    public float minValue, maxValue, currentValue;
    public boolean isDragging = false;
    public boolean wasDragging = false; // Nuevo: para detectar si se estaba arrastrando
    public int fontSize;

    public UISlider(String id, int x, int y, int w, int h, float min, float max, float initialValue, int fontSize) {
        super(id, x, y, w, h);
        this.minValue = min;
        this.maxValue = max;
        this.currentValue = constrain(initialValue, min, max);
        this.fontSize = fontSize;
    }

    public float getValue() {
        return currentValue;
    }

    public void setValue(float value) {
        currentValue = constrain(value, minValue, maxValue);
    }

    public void draw() {
        if (!isVisible) return;
        pushStyle();

        // L??nea de fondo
        stroke(currentTheme.borderColor);
        strokeWeight(3);
        line(x, y + height / 2, x + width, y + height / 2);

        // L??nea de progreso
        float handleX = map(currentValue, minValue, maxValue, x, x + width);
        stroke(currentTheme.accentColor);
        line(x, y + height / 2, handleX, y + height / 2);

        // Mango del slider
        fill(currentTheme.accentColor);
        noStroke();
        float handleRadius = height * SLIDER_HANDLE_RADIUS_RATIO / 2;
        ellipse(handleX, y + height / 2, handleRadius * 2, handleRadius * 2);

        // Valor actual (opcional)
        if (isEnabled) {
            fill(currentTheme.textColor);
            textAlign(CENTER, CENTER);
            textSize(fontSize);
            text(nf(currentValue, 0, 2), x + width / 2, y - fontSize);
        }
        popStyle();
    }

    public void update() {
        if (!isEnabled || !isDragging) return;

        updateFromMouse(getScaledMouseX());
    }

    public void mousePressed() {
        if (!isEnabled) return;
    }

    public void mouseDragged() {
        // Solo actualizar si estamos en modo dragging
        if (isDragging) {
            update();
        }
    }

    public void beginGestureDrag() {
        if (!isEnabled) return;
        isDragging = true;
        wasDragging = true;
        update();
    }

    public void mouseReleased() {
        // IMPORTANTE: Siempre limpiar el estado de dragging
        if (isDragging) {
            isDragging = false;
            triggerEvent(this, "released", currentValue);
        }
        // Tambi??n limpiar wasDragging para el pr??ximo ciclo
        wasDragging = false;

        // Forzar la limpieza del estado si por alguna raz??n se qued?? activo
        if (!mousePressed) {
            isDragging = false;
        }
    }

    // M??todo adicional para limpiar estado si se pierde el foco
    public void forceRelease() {
        if (isDragging) {
            isDragging = false;
            triggerEvent(this, "released", currentValue);
        }
        wasDragging = false;
    }

    public void updateFromMouse(float mx) {
        float oldValue = currentValue;
        float ratio = constrain((mx - x) / width, 0, 1);
        currentValue = lerp(minValue, maxValue, ratio);
        if (abs(oldValue - currentValue) > 0.001f) {
            triggerEvent(this, "changed", currentValue);
        }
    }
}
