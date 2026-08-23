package simplecore;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import processing.data.StringDict;
import java.util.ArrayList;
import java.util.HashMap;

/** Base class for frame-driven logical or drawable tasks. */
public class Task {
    private static final class TextCommand {
        final PFont font;
        final int size;
        final String value;
        final int align;
        final float x;
        final float y;
        final int color;
        final float alpha;

        TextCommand(PFont font, int size, String value, int align,
                    float x, float y, int color, float alpha) {
            this.font = font;
            this.size = size;
            this.value = value;
            this.align = align;
            this.x = x;
            this.y = y;
            this.color = color;
            this.alpha = alpha;
        }
    }

    public static final int _BACKSPACE = PConstants.BACKSPACE;
    public static final int _UP = PConstants.UP;
    public static final int _DOWN = PConstants.DOWN;
    public static final int _LEFT = PConstants.LEFT;
    public static final int _RIGHT = PConstants.RIGHT;
    public static final int _SPACE = ' ';
    public static final int _ESC = PConstants.ESC;
    public static final int _ENTER = PConstants.ENTER;
    public static final int _DELETE = PConstants.DELETE;
    public static final int _TAB = PConstants.TAB;

    public static final int _A = 'A', _B = 'B', _C = 'C', _D = 'D', _E = 'E';
    public static final int _F = 'F', _G = 'G', _H = 'H', _I = 'I', _J = 'J';
    public static final int _K = 'K', _L = 'L', _M = 'M', _N = 'N', _O = 'O';
    public static final int _P = 'P', _Q = 'Q', _R = 'R', _S = 'S', _T = 'T';
    public static final int _U = 'U', _V = 'V', _W = 'W', _X = 'X', _Y = 'Y', _Z = 'Z';

    private final Core core;
    private final ArrayList<TextCommand> textCommands = new ArrayList<TextCommand>();
    private final HashMap<String, Integer> timerMarks = new HashMap<String, Integer>();
    boolean destroyed;

    public int priority;
    public boolean live = true;
    public boolean killProtection;
    public final int id;
    public int type;
    public final String className;
    public Task father;
    public Scene scene;
    public int liveFrames;
    public final StringDict properties = new StringDict();
    public final CoreMouse mouse;
    public TouchPoint point;

    public PImage graph;
    public float x;
    public float y;
    public int z;
    private float spriteScaleX = 1.0f;
    private float spriteScaleY = 1.0f;
    public float angle;
    public float alpha = 255.0f;
    public int tintColor = 0xFFFFFFFF;
    public boolean visible = true;

    public Task() {
        core = Core.requireInstance();
        mouse = core.mouse;
        className = getClass().getSimpleName();
        id = core.registerTask(this);
        core.attachToActiveScene(this);
    }

    public final Core getCore() {
        return core;
    }

    public final boolean exists() {
        return Core.exists(this);
    }

    /** Minimal keyboard query inherited by every Task. */
    public final boolean key(int code) {
        return core.key(code);
    }

    /** Returns true for one frame whenever this named interval elapses. */
    public final boolean timer(String name, int milliseconds) {
        requireTimerArguments(name, milliseconds);
        int now = core.getParent().millis();
        Integer mark = timerMarks.get(name);
        if (mark == null) {
            timerMarks.put(name, now);
            return false;
        }
        if (now - mark.intValue() < milliseconds) return false;
        timerMarks.put(name, now);
        return true;
    }

    /** Starts this named timer again from the current instant. */
    public final void resetTimer(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Timer name must not be empty");
        }
        timerMarks.put(name, core.getParent().millis());
    }

    private static void requireTimerArguments(String name, int milliseconds) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Timer name must not be empty");
        }
        if (milliseconds <= 0) {
            throw new IllegalArgumentException("Timer duration must be greater than zero");
        }
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

    public Task scale(float value) {
        if (!Float.isFinite(value)) return this;
        spriteScaleX = value;
        spriteScaleY = value;
        return this;
    }

    public Task scalex(float value) {
        if (Float.isFinite(value)) spriteScaleX = value;
        return this;
    }

    public Task scaley(float value) {
        if (Float.isFinite(value)) spriteScaleY = value;
        return this;
    }

    /** @deprecated Use scale(value). */
    @Deprecated
    public Task setScale(float newScale) {
        return scale(newScale);
    }

    /** @deprecated Use scalex(x) and scaley(y). */
    @Deprecated
    public Task setAxisScale(float newScaleX, float newScaleY) {
        scalex(newScaleX);
        scaley(newScaleY);
        return this;
    }

    /** @deprecated Use scale(1). */
    @Deprecated
    public Task resetAxisScale() {
        return scale(1.0f);
    }

    public Task setAngle(float newAngle) {
        angle = newAngle;
        return this;
    }

    public Task setAlpha(float newAlpha) {
        alpha = PApplet.constrain(newAlpha, 0.0f, 255.0f);
        return this;
    }

    /** Tints the task graphic without changing its alpha. */
    public Task tint(int color) {
        tintColor = color;
        return this;
    }

    /** @deprecated Use tint(color). */
    @Deprecated
    public Task setTint(int newTintColor) {
        return tint(newTintColor);
    }

    public Task clearTint() {
        tintColor = 0xFFFFFFFF;
        return this;
    }

    public Task setVisible(boolean newVisible) {
        visible = newVisible;
        return this;
    }

    /** Queues text in logical viewport coordinates for this frame. */
    public final void text(PFont font, int size, String text, int align,
                           float x, float y, int color, float alpha) {
        if (text == null || size <= 0 || !Float.isFinite(x) || !Float.isFinite(y)) return;
        if (!Float.isFinite(alpha)) return;
        int safeAlign = align == PConstants.CENTER || align == PConstants.RIGHT
            ? align : PConstants.LEFT;
        textCommands.add(new TextCommand(
            font, size, text, safeAlign, x, y, color,
            PApplet.constrain(alpha, 0.0f, 255.0f)
        ));
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

    /** Returns whether a logical point lies inside this task's rotated graphic box. */
    public boolean containsPoint(float pointX, float pointY) {
        if (!hasCollisionBox() || !Float.isFinite(pointX) || !Float.isFinite(pointY)) return false;
        double radians = Math.toRadians(angle);
        float cosine = (float)Math.cos(radians);
        float sine = (float)Math.sin(radians);
        float deltaX = pointX - x;
        float deltaY = pointY - y;
        float localX = deltaX * cosine + deltaY * sine;
        float localY = -deltaX * sine + deltaY * cosine;
        float halfWidth = graph.width * Math.abs(getEffectiveScaleX()) * 0.5f;
        float halfHeight = graph.height * Math.abs(getEffectiveScaleY()) * 0.5f;
        return Math.abs(localX) <= halfWidth && Math.abs(localY) <= halfHeight;
    }

    /** Captures one active mouse or touch point over this Task until release. */
    public boolean isTouched() {
        if (!hasCollisionBox()) {
            point = null;
            return false;
        }
        if (point != null) {
            if (core.isActivePoint(point)) return true;
            point = null;
        }
        for (TouchPoint candidate : core.points) {
            float candidateX = candidate.x;
            float candidateY = candidate.y;
            if (scene != null && scene.isActive()) {
                candidateX = scene.camera.screenToWorldX(candidateX);
                candidateY = scene.camera.screenToWorldY(candidateY);
            }
            if (containsPoint(candidateX, candidateY)) {
                point = candidate;
                return true;
            }
        }
        return false;
    }

    /** Returns whether this task's rotated graphic box overlaps another one. */
    public boolean overlap(Task target) {
        if (target == null || !hasCollisionBox() || !target.hasCollisionBox()) return false;
        double thisRadians = Math.toRadians(angle);
        double targetRadians = Math.toRadians(target.angle);
        float thisCosine = (float)Math.cos(thisRadians);
        float thisSine = (float)Math.sin(thisRadians);
        float targetCosine = (float)Math.cos(targetRadians);
        float targetSine = (float)Math.sin(targetRadians);
        float thisHalfWidth = graph.width * Math.abs(getEffectiveScaleX()) * 0.5f;
        float thisHalfHeight = graph.height * Math.abs(getEffectiveScaleY()) * 0.5f;
        float targetHalfWidth = target.graph.width * Math.abs(target.getEffectiveScaleX()) * 0.5f;
        float targetHalfHeight = target.graph.height * Math.abs(target.getEffectiveScaleY()) * 0.5f;
        float deltaX = target.x - x;
        float deltaY = target.y - y;

        return overlapsOnAxis(deltaX, deltaY, thisCosine, thisSine,
                    thisCosine, thisSine, thisHalfWidth, thisHalfHeight,
                    targetCosine, targetSine, targetHalfWidth, targetHalfHeight)
            && overlapsOnAxis(deltaX, deltaY, -thisSine, thisCosine,
                    thisCosine, thisSine, thisHalfWidth, thisHalfHeight,
                    targetCosine, targetSine, targetHalfWidth, targetHalfHeight)
            && overlapsOnAxis(deltaX, deltaY, targetCosine, targetSine,
                    thisCosine, thisSine, thisHalfWidth, thisHalfHeight,
                    targetCosine, targetSine, targetHalfWidth, targetHalfHeight)
            && overlapsOnAxis(deltaX, deltaY, -targetSine, targetCosine,
                    thisCosine, thisSine, thisHalfWidth, thisHalfHeight,
                    targetCosine, targetSine, targetHalfWidth, targetHalfHeight);
    }

    private boolean hasCollisionBox() {
        return exists() && visible && alpha > 0.0f && graph != null
            && graph.width > 0 && graph.height > 0
            && Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(angle)
            && Float.isFinite(getEffectiveScaleX()) && Float.isFinite(getEffectiveScaleY())
            && getEffectiveScaleX() != 0.0f && getEffectiveScaleY() != 0.0f;
    }

    private static boolean overlapsOnAxis(
            float deltaX, float deltaY, float axisX, float axisY,
            float firstAxisX, float firstAxisY, float firstHalfWidth, float firstHalfHeight,
            float secondAxisX, float secondAxisY, float secondHalfWidth, float secondHalfHeight) {
        float distance = Math.abs(deltaX * axisX + deltaY * axisY);
        float firstRadius = firstHalfWidth * Math.abs(axisX * firstAxisX + axisY * firstAxisY)
            + firstHalfHeight * Math.abs(axisX * -firstAxisY + axisY * firstAxisX);
        float secondRadius = secondHalfWidth * Math.abs(axisX * secondAxisX + axisY * secondAxisY)
            + secondHalfHeight * Math.abs(axisX * -secondAxisY + axisY * secondAxisX);
        return distance <= firstRadius + secondRadius;
    }

    public float getEffectiveScaleX() {
        return spriteScaleX;
    }

    public float getEffectiveScaleY() {
        return spriteScaleY;
    }

    public boolean isDrawable() {
        return exists() && visible && alpha > 0.0f && (graph != null || !textCommands.isEmpty());
    }

    final void beginFrame() {
        textCommands.clear();
    }

    final void releaseInternalState() {
        point = null;
        timerMarks.clear();
        textCommands.clear();
    }

    protected void render() {
        if (!exists() || !visible || graph == null || alpha <= 0.0f) return;
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

    final void renderText() {
        if (!exists() || !visible || alpha <= 0.0f || textCommands.isEmpty()) return;
        PApplet parent = core.getParent();
        parent.pushStyle();
        try {
            for (TextCommand command : textCommands) {
                if (command.font != null) parent.textFont(command.font);
                parent.textSize(command.size);
                parent.textAlign(command.align, PConstants.CENTER);
                parent.fill(command.color, command.alpha * alpha / 255.0f);
                parent.text(command.value, command.x, command.y);
            }
        } finally {
            parent.popStyle();
        }
    }
}
