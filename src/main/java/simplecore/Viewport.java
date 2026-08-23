package simplecore;

import processing.core.PApplet;

/** The single virtual-resolution viewport shared by SimpleCore and SimpleUI. */
public final class Viewport {
    private static PApplet host;
    private static boolean configured;
    private static float designWidth;
    private static float designHeight;
    private static float logicalWidth;
    private static float logicalHeight;
    private static float scaleX = 1.0f;
    private static float scaleY = 1.0f;
    private static float offsetX;
    private static float offsetY;
    private static ViewportMode mode = ViewportMode.FIT;

    private Viewport() {}

    public static synchronized void attach(PApplet parent) {
        if (parent == null) throw new IllegalArgumentException("Viewport requires a PApplet host");
        if (host != parent) {
            host = parent;
            resetConfiguration();
        }
        update();
    }

    public static synchronized void setMode(float width, float height, ViewportMode newMode) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Viewport dimensions must be positive");
        }
        designWidth = width;
        designHeight = height;
        mode = newMode == null ? ViewportMode.FIT : newMode;
        configured = true;
        update();
    }

    public static synchronized void setManualScale(float value) {
        if (!Float.isFinite(value) || value <= 0) return;
        requireHost();
        designWidth = host.width / value;
        designHeight = host.height / value;
        mode = ViewportMode.RESPONSIVE;
        configured = true;
        update();
    }

    public static synchronized void update() {
        requireHost();
        float surfaceWidth = Math.max(0, host.width);
        float surfaceHeight = Math.max(0, host.height);
        if (!configured || designWidth <= 0 || designHeight <= 0) {
            scaleX = scaleY = 1.0f;
            offsetX = offsetY = 0.0f;
            logicalWidth = surfaceWidth;
            logicalHeight = surfaceHeight;
            return;
        }

        float widthScale = surfaceWidth / designWidth;
        float heightScale = surfaceHeight / designHeight;
        if (mode == ViewportMode.STRETCH) {
            scaleX = Math.max(0.0001f, widthScale);
            scaleY = Math.max(0.0001f, heightScale);
            offsetX = offsetY = 0.0f;
            logicalWidth = designWidth;
            logicalHeight = designHeight;
            return;
        }

        float fit = Math.min(widthScale, heightScale);
        float fill = Math.max(widthScale, heightScale);
        float scale = Math.max(0.0001f, mode == ViewportMode.FILL ? fill : fit);
        scaleX = scaleY = scale;
        if (mode == ViewportMode.RESPONSIVE) {
            offsetX = offsetY = 0.0f;
            logicalWidth = surfaceWidth / scaleX;
            logicalHeight = surfaceHeight / scaleY;
        } else {
            logicalWidth = designWidth;
            logicalHeight = designHeight;
            offsetX = (surfaceWidth - designWidth * scaleX) * 0.5f;
            offsetY = (surfaceHeight - designHeight * scaleY) * 0.5f;
        }
    }

    public static boolean isConfigured() { return configured; }
    public static float getDesignWidth() { return designWidth; }
    public static float getDesignHeight() { return designHeight; }
    public static float getLogicalWidth() { return logicalWidth; }
    public static float getLogicalHeight() { return logicalHeight; }
    public static float getScaleX() { return scaleX; }
    public static float getScaleY() { return scaleY; }
    public static float getOffsetX() { return offsetX; }
    public static float getOffsetY() { return offsetY; }
    public static ViewportMode getMode() { return mode; }
    public static float screenToDesignX(float value) { return (value - offsetX) / scaleX; }
    public static float screenToDesignY(float value) { return (value - offsetY) / scaleY; }
    public static float designToScreenX(float value) { return offsetX + value * scaleX; }
    public static float designToScreenY(float value) { return offsetY + value * scaleY; }

    private static void requireHost() {
        if (host == null) throw new IllegalStateException("Viewport has not been attached");
    }

    private static void resetConfiguration() {
        configured = false;
        designWidth = designHeight = 0.0f;
        logicalWidth = logicalHeight = 0.0f;
        scaleX = scaleY = 1.0f;
        offsetX = offsetY = 0.0f;
        mode = ViewportMode.FIT;
    }
}
