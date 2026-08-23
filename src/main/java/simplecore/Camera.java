/*
 * SimpleCore — designed and developed by Luis lopez martinez.
 * Copyright (c) 2026 Luis lopez martinez. Licensed under the MIT License.
 * SPDX-License-Identifier: MIT
 */
package simplecore;

import processing.core.PApplet;

/** A Scene camera with a screen-space dead zone and optional world bounds. */
public final class Camera {
    private final Scene scene;
    public float x;
    public float y;
    public Task target;
    private float deadZoneX;
    private float deadZoneY;
    private float deadZoneWidth;
    private float deadZoneHeight;
    private boolean deadZoneConfigured;
    private float boundsX;
    private float boundsY;
    private float boundsWidth;
    private float boundsHeight;
    private boolean boundsConfigured;

    Camera(Scene scene) {
        this.scene = scene;
    }

    public Camera setTarget(Task task) {
        if (task != null && task.getCore() != scene.getCore()) {
            throw new IllegalArgumentException("Camera target belongs to another Core");
        }
        if (task != null && task.scene != scene) scene.add(task);
        target = task;
        return this;
    }

    public Camera clearTarget() {
        target = null;
        return this;
    }

    public Camera setPosition(float newX, float newY) {
        if (Float.isFinite(newX)) x = newX;
        if (Float.isFinite(newY)) y = newY;
        constrainToBounds();
        return this;
    }

    public Camera setDeadZone(float zoneX, float zoneY, float width, float height) {
        if (!Float.isFinite(zoneX) || !Float.isFinite(zoneY) ||
            !Float.isFinite(width) || !Float.isFinite(height) || width < 0 || height < 0) {
            throw new IllegalArgumentException("Camera dead zone must be finite and non-negative");
        }
        deadZoneX = zoneX;
        deadZoneY = zoneY;
        deadZoneWidth = width;
        deadZoneHeight = height;
        deadZoneConfigured = true;
        return this;
    }

    public Camera setBounds(float worldX, float worldY, float width, float height) {
        if (!Float.isFinite(worldX) || !Float.isFinite(worldY) ||
            !Float.isFinite(width) || !Float.isFinite(height) || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Camera bounds must be finite and positive");
        }
        boundsX = worldX;
        boundsY = worldY;
        boundsWidth = width;
        boundsHeight = height;
        boundsConfigured = true;
        constrainToBounds();
        return this;
    }

    public Camera clearBounds() {
        boundsConfigured = false;
        return this;
    }

    public float screenToWorldX(float screenX) { return screenX + x; }
    public float screenToWorldY(float screenY) { return screenY + y; }
    public float worldToScreenX(float worldX) { return worldX - x; }
    public float worldToScreenY(float worldY) { return worldY - y; }

    void update() {
        if (target == null || !target.exists() || target.scene != scene) {
            if (target != null && !target.exists()) target = null;
            constrainToBounds();
            return;
        }
        float viewportWidth = Viewport.getLogicalWidth();
        float viewportHeight = Viewport.getLogicalHeight();
        float zoneX = deadZoneConfigured ? deadZoneX : viewportWidth * 0.25f;
        float zoneY = deadZoneConfigured ? deadZoneY : viewportHeight * 0.25f;
        float zoneWidth = deadZoneConfigured ? deadZoneWidth : viewportWidth * 0.5f;
        float zoneHeight = deadZoneConfigured ? deadZoneHeight : viewportHeight * 0.5f;
        float left = PApplet.constrain(zoneX, 0, viewportWidth);
        float top = PApplet.constrain(zoneY, 0, viewportHeight);
        float right = PApplet.constrain(zoneX + zoneWidth, left, viewportWidth);
        float bottom = PApplet.constrain(zoneY + zoneHeight, top, viewportHeight);
        float targetScreenX = target.x - x;
        float targetScreenY = target.y - y;
        if (targetScreenX < left) x = target.x - left;
        else if (targetScreenX > right) x = target.x - right;
        if (targetScreenY < top) y = target.y - top;
        else if (targetScreenY > bottom) y = target.y - bottom;
        constrainToBounds();
    }

    private void constrainToBounds() {
        if (!boundsConfigured) return;
        float viewportWidth = Viewport.getLogicalWidth();
        float viewportHeight = Viewport.getLogicalHeight();
        float maximumX = boundsX + boundsWidth - viewportWidth;
        float maximumY = boundsY + boundsHeight - viewportHeight;
        x = maximumX < boundsX ? boundsX + (boundsWidth - viewportWidth) * 0.5f
            : PApplet.constrain(x, boundsX, maximumX);
        y = maximumY < boundsY ? boundsY + (boundsHeight - viewportHeight) * 0.5f
            : PApplet.constrain(y, boundsY, maximumY);
    }
}
