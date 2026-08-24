# SimpleUI 0.7.2

SimpleUI 0.7.2 fixes the mode metadata of every bundled Android example.
Opening any `Android*` sketch from Processing now selects Android Mode
automatically. The build validates this metadata so future packages cannot
silently ship Android examples as Java Mode sketches.

A normal sketch defines its video mode in `settings()` and calls `initUI()` in
`setup()`. SimpleUI clears the frame with the active theme, updates input and
controls, and renders the interface through Processing lifecycle callbacks.

## Highlights

- All 39 Android examples include a valid root `sketch.properties` file.
- The finger-drawing example now paints reliably on Desktop and preserves its
  drawing when the P2D surface is resized or recreated.
- Android touch updates can no longer modify `Core.points` while GLThread is
  iterating it, eliminating the observed `ConcurrentModificationException`.
- `Task.isTouched()` now reflects the contact's current position and becomes
  false immediately when the finger leaves the Task graphic.
- Creating large Task bursts from Android UI callbacks is safe while GLThread
  updates and sorts the engine, fixing the example 24 particle crash.
- UI-only sketches no longer need to declare `draw()`.
- `background(SimpleUI.currentTheme.backgroundColor)` and
  `SimpleUI.updateAndDrawUI()` are no longer required in normal sketches.
- Custom sketch drawing remains behind the interface automatically.
- Desktop text inputs use the operating system's native key repetition.
- Desktop supports explicit JAVA2D and P2D selection; Android supports its
  traditional renderer and P2D.
- SimpleCore includes scenes, dead-zone cameras, multitouch, timers, fading,
  audio, resource loaders and Task collision helpers.
- All Desktop and Android examples use the minimal initialization contract.
- Desktop and Android now share the same explicit video-mode call:
  `SimpleUI.setVideoMode(this, width, height, renderer)`.
- SimpleCore-only sketches use the same cross-platform pair:
  `Core.setVideoMode(this, width, height, renderer)` and
  `Core.start(this, ViewportMode)`.

## Minimal Desktop sketch

```java
import simpleui.desktop.*;

void settings() {
  SimpleUI.setVideoMode(this, 640, 420, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);
  SimpleUI.addUIElement(
    new UIButton("hello", 40, 50, 180, 48, "Hello", 18)
  );
}
```

## Advanced rendering

`updateAndDrawUI()` is available for advanced manual integrations. The normal
`initUI()` lifecycle renders the interface automatically and does not require
this call.

The library is distributed under the MIT License. Bundled audio decoder
dependencies retain their respective third-party licenses as documented in
`THIRD_PARTY_NOTICES.md`.
