package simplecore;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.data.StringDict;

/** Base class for frame-driven logical or drawable tasks. */
public class Task {
    private final Core core;
    boolean destroyed;

    public int priority;
    public boolean live = true;
    public boolean killProtection;
    public final int id;
    public int type;
    public final String className;
    public Task father;
    public int liveFrames;
    public final StringDict properties = new StringDict();

    public PImage graph;
    public float x;
    public float y;
    public int z;
    public float scale = 1.0f;
    public float scaleX = 1.0f;
    public float scaleY = 1.0f;
    public float angle;
    public float alpha = 255.0f;
    public int tintColor = 0xFFFFFFFF;
    public boolean visible = true;

    public Task() {
        core = Core.requireInstance();
        className = getClass().getSimpleName();
        id = core.registerTask(this);
    }

    public final Core getCore() {
        return core;
    }

    public final boolean exists() {
        return Core.exists(this);
    }

    public final Task kill() {
        core.signal(this, Core.SIGNAL_KILL);
        return this;
    }

    public final Task setKillProtection(boolean enabled) {
        core.signal(this, enabled ? Core.SIGNAL_PROTECT : Core.SIGNAL_UNPROTECT);
        return this;
    }

    protected void initialize() {}
    protected void onDestroy() {}
    protected void frame() {}

    public Task setGraph(PImage image) {
        graph = image;
        return this;
    }

    public Task clearGraph() {
        graph = null;
        return this;
    }

    public Task setPosition(float newX, float newY) {
        x = newX;
        y = newY;
        return this;
    }

    public Task setZ(int newZ) {
        z = newZ;
        return this;
    }

    public Task setScale(float newScale) {
        scale = newScale;
        return this;
    }

    public Task setAxisScale(float newScaleX, float newScaleY) {
        scaleX = newScaleX;
        scaleY = newScaleY;
        return this;
    }

    public Task resetAxisScale() {
        scaleX = 1.0f;
        scaleY = 1.0f;
        return this;
    }

    public Task setAngle(float newAngle) {
        angle = newAngle;
        return this;
    }

    public Task setAlpha(float newAlpha) {
        alpha = PApplet.constrain(newAlpha, 0.0f, 255.0f);
        return this;
    }

    public Task setTint(int newTintColor) {
        tintColor = newTintColor;
        return this;
    }

    public Task clearTint() {
        tintColor = 0xFFFFFFFF;
        return this;
    }

    public Task setVisible(boolean newVisible) {
        visible = newVisible;
        return this;
    }

    public float getDist(Task target) {
        if (target == null) return Float.NaN;
        float deltaX = target.x - x;
        float deltaY = target.y - y;
        return (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public float getAngle(Task target) {
        if (target == null) return Float.NaN;
        float result = (float)Math.toDegrees(Math.atan2(target.y - y, target.x - x));
        return result < 0.0f ? result + 360.0f : result;
    }

    public Task advance(float directionAngle) {
        return advance(directionAngle, 1.0f);
    }

    public Task advance(float directionAngle, float distance) {
        if (!Float.isFinite(directionAngle) || !Float.isFinite(distance)) return this;
        double radians = Math.toRadians(directionAngle);
        x += (float)Math.cos(radians) * distance;
        y += (float)Math.sin(radians) * distance;
        return this;
    }

    public boolean usesAxisScale() {
        return Math.abs(scaleX - 1.0f) > 0.0001f || Math.abs(scaleY - 1.0f) > 0.0001f;
    }

    public float getEffectiveScaleX() {
        return usesAxisScale() ? scaleX : scale;
    }

    public float getEffectiveScaleY() {
        return usesAxisScale() ? scaleY : scale;
    }

    public boolean isDrawable() {
        return exists() && visible && graph != null && alpha > 0.0f;
    }

    protected void render() {
        if (!isDrawable()) return;
        PApplet parent = core.getParent();
        parent.pushMatrix();
        parent.pushStyle();
        try {
            parent.translate(x, y);
            parent.rotate(PApplet.radians(angle));
            parent.scale(getEffectiveScaleX(), getEffectiveScaleY());
            parent.imageMode(PConstants.CENTER);
            parent.tint(tintColor, PApplet.constrain(alpha, 0.0f, 255.0f));
            parent.image(graph, 0, 0);
        } finally {
            parent.popStyle();
            parent.popMatrix();
        }
    }
}
