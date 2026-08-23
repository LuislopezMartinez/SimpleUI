/*
 * SimpleCore — designed and developed by Luis lopez martinez.
 * Copyright (c) 2026 Luis lopez martinez. Licensed under the MIT License.
 * SPDX-License-Identifier: MIT
 */
package simplecore;

/** One active mouse or touch-screen contact in logical viewport coordinates. */
public final class TouchPoint {
    public final int id;
    public float x;
    public float y;
    public float previousX;
    public float previousY;
    public float deltaX;
    public float deltaY;
    public float area;
    public float pressure;

    TouchPoint(int id, float x, float y, float area, float pressure) {
        this.id = id;
        this.x = previousX = x;
        this.y = previousY = y;
        this.area = area;
        this.pressure = pressure;
    }

    void update(float newX, float newY, float newArea, float newPressure) {
        previousX = x;
        previousY = y;
        x = newX;
        y = newY;
        deltaX = x - previousX;
        deltaY = y - previousY;
        area = newArea;
        pressure = newPressure;
    }
}
