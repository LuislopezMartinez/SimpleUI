package simplecore;

/** Simple mouse state exposed to sketches and Tasks. */
public final class CoreMouse {
    public float x;
    public float y;
    public boolean left;
    public boolean right;
    public boolean center;

    void press(int button) {
        if (button == Core.MOUSE_LEFT) left = true;
        else if (button == Core.MOUSE_RIGHT) right = true;
        else if (button == Core.MOUSE_CENTER) center = true;
    }

    void release(int button) {
        if (button == Core.MOUSE_LEFT) left = false;
        else if (button == Core.MOUSE_RIGHT) right = false;
        else if (button == Core.MOUSE_CENTER) center = false;
    }

    /** Clears every button, for example after the sketch loses focus. */
    public void clear() {
        left = false;
        right = false;
        center = false;
    }

    void move(float screenX, float screenY) {
        x = Viewport.screenToDesignX(screenX);
        y = Viewport.screenToDesignY(screenY);
    }
}
