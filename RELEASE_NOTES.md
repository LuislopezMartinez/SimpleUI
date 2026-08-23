# SimpleUI 0.7.0

SimpleUI 0.7.0 introduces a fully automatic UI lifecycle for Processing
Desktop and Android Mode. A normal sketch only needs to call `initUI()`:
SimpleUI clears the frame with the active theme, updates input and controls,
and renders the interface through Processing lifecycle callbacks.

## Highlights

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

## Minimal Desktop sketch

```java
import simpleui.desktop.*;

void settings() {
  size(640, 420, P2D);
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);
  SimpleUI.addUIElement(
    new UIButton("hello", 40, 50, 180, 48, "Hello", 18)
  );
}
```

## Compatibility note

`updateAndDrawUI()` remains available for advanced integrations and migration,
but it is unnecessary in the standard `initUI()` lifecycle. Existing manual
calls are protected against duplicate rendering during migration.

The library is distributed under the MIT License. Bundled audio decoder
dependencies retain their respective third-party licenses as documented in
`THIRD_PARTY_NOTICES.md`.
