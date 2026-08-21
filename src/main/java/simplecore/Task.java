package simplecore;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import processing.data.StringDict;
import java.util.ArrayList;

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
    public final CoreMouse mouse;

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
