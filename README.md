# SimpleUI for Processing Desktop and Android

Current version: **0.5.5**.

Designed and developed by **[Luis López Martínez](https://github.com/LuislopezMartinez)**.
Distributed under the **MIT License**.

This is the canonical development and installable Processing library
directory. All library sources, tools, examples and tests belong here. Since
version 0.5, the checked-in Java files under `src/main/java` are the canonical
sources compiled by the build. Historical PDE inputs are retained only as
reference and cannot overwrite the modern source set.

## Result

The same JAR contains two independent public packages:

- `simpleui.desktop.*`
- `simpleui.android.*`

Both packages contain the same widget/model classes and expose the same public
constructors and methods. A build-time parity check prevents the two APIs from
drifting. Android-only operating-system services (native keyboard, `Activity`
and keep-screen-awake) remain in `simpleui.android.SimpleUI`.

Import exactly one platform package in a sketch. Do not combine
`simpleui.desktop.*` and `simpleui.android.*`, because they intentionally expose
matching class names.

Widget classes (`UIButton`, `UIList`, `UIView`, etc.) are top-level public
classes, so a project needs only one wildcard import for its platform. Global
UI operations are grouped under `SimpleUI` to avoid polluting the sketch's
global namespace.

The JAR also includes `UICalendar`, `UIDate`, `UIDateTime`, `UITimeSpan`,
`UICalendarEvent` and `UICalendarSelection` in both platform packages. They are
generated from the same verified `SimpleUICalendar.pde` source.

`UIRect`, `UISignalMeter` and enriched chat messages with footer/read receipt
are available on both platforms. `UISlider` uses the shared constructor
`UISlider(id, x, y, w, h, min, max, initialValue, fontSize)`, emits `changed`
while its value changes and `released` when the gesture ends. `UIDropdown`
emits `changed` on both platforms.

The independent `simplecore.*` package contains the shared frame-task engine.
It has no dependency on SimpleUI or Android APIs.

## Install and update

In Processing, open **Sketch > Import Library... > Add Library...**, search for
**SimpleUI**, select it and click **Install**. The Contribution Manager also
offers **Update** when a newer published version is available.

For a manual installation, download `SimpleUI.zip`, extract the `SimpleUI`
folder into the Processing sketchbook's `libraries` folder and restart the
IDE. Do not keep two SimpleUI copies in that folder.

Desktop initialization:

```java
import simpleui.desktop.*;

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(1280, 720);
}
```

Android initialization:

```java
import simpleui.android.*;

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18);
  SimpleUI.setMode(1280, 720, UIScaleMode.RESPONSIVE);
}
```

## Virtual resolution

`setMode(width, height)` defines the logical resolution used to design the
interface. SimpleUI scales drawing, controls, modals and pointer/touch input
as one viewport. The two-argument overload uses `UIScaleMode.FIT`.

```java
SimpleUI.setMode(1280, 720, UIScaleMode.RESPONSIVE);
```

The available modes are:

- `FIT`: show the complete design, preserve aspect ratio and center it.
- `FILL`: fill the host surface while preserving aspect ratio; excess is
  cropped.
- `RESPONSIVE`: preserve the FIT scale but expose surplus host space through
  `getLogicalWidth()` and `getLogicalHeight()` so the application can relayout
  expandable controls.

Use `screenToDesignX/Y()` and `designToScreenX/Y()` when custom sketch
graphics need to share coordinates with SimpleUI.

`UISwitch` is available on both platforms for compact boolean choices and
emits `changed` with a `boolean`. `UITable` displays a proportional vertical
scrollbar only when its rows overflow; it supports wheel/touch scrolling,
content dragging, thumb dragging and rail paging.

`initUI(...)` automatically registers the mouse and keyboard event bridge. The
sketch does not need to declare `mousePressed`, `mouseDragged`, `mouseReleased`,
`keyPressed` or `keyTyped`. Rendering remains explicit with
`SimpleUI.updateAndDrawUI()` so the sketch controls layer order.

On Windows Desktop with the default Java2D/AWT renderer, `initUI(...)` also
installs a resize guard automatically. It briefly pauses rendering while AWT
recreates the window buffers and recovers only Processing's transient
`Buffers have not been created` failure. No extra sketch call is required.
The guard is not installed on Android, Linux, macOS or OpenGL renderers.
The Desktop-only method `simpleui.desktop.SimpleUI.isWindowsResizeGuardActive()`
reports whether it is active; that method is not part of
`simpleui.android.SimpleUI`.

Applications that need to intercept a key before SimpleUI (for example the
Android Back key) can install `SimpleUI.setKeyEventInterceptor(...)` and return
`true` when the event has been handled. Automatic forwarding can be disabled
with `SimpleUI.setAutomaticEventHandling(false)` for unusual integrations.

## Calendar

```java
UICalendar calendar = new UICalendar("calendar", 20, 60, 360, 300);
calendar.setSelectedDate(SimpleUI.getUIToday());
calendar.addEvent("demo", SimpleUI.getUIToday(), "Today");
SimpleUI.addUIElement(calendar);
```

Selecting a date emits `dateSelected` through the normal `UIEventHandler`. Its
data is a `UICalendarSelection`. Complete Desktop and Android calendar examples
are included under `examples`.

## SimpleCore

```java
import simplecore.*;

Core taskCore;

void setup() {
  taskCore = Core.start(this);
  new MyTask();
}

class MyTask extends Task {
  protected void initialize() {}
  protected void frame() {}
  protected void onDestroy() {}
}
```

`Core` is a strict singleton. Repeating `Core.start(this)` returns the existing
instance; attempting to start it with another `PApplet` throws an exception.
`Core.shutdown()` unregisters Processing callbacks, invokes `onDestroy()` once
for every task, clears all state and permits a clean restart.

Automatic task rendering is enabled by default. For custom layer ordering:

```java
taskCore.setAutomaticRendering(false);
taskCore.renderTasks();
```

Existing PDE task subclasses must declare lifecycle overrides as `protected`
or `public`, because they now extend a class from a Java package. See the
`DesktopSimpleCore` and `AndroidSimpleCore` examples.

## Teaching examples

The `examples` directory includes a numbered course of 26 Desktop/Android
pairs: one sketch for every visual control (including `UISwitch`), practical
login, modal and view projects, and four SimpleCore exercises covering
lifecycle, animated game entities, button-created particles and pointer/touch
interaction.

Open [`examples/README.md`](examples/README.md) for the complete index and the
recommended classroom order. Every numbered folder is an independent
Processing sketch and uses only generated graphics, so no external asset setup
is required.

The distribution also includes eight non-numbered Desktop/Android sketches:
`Basic`, `Calendar`, `SimpleCore` and `UnifiedFeatures` for each platform.

## Build

Run from PowerShell:

```powershell
.\build.ps1
```

The build compiles the canonical Desktop, Android and SimpleCore Java sources,
combines everything into `library/SimpleUI.jar`, runs lifecycle, external
`UIView` hook and public API smoke tests, and performs Android D8 conversion as
a compatibility check.

Build dependencies are discovered from the installed Processing applications,
the Android SDK and the Gradle cache. Continuous integration supplies explicit
paths through the `SIMPLEUI_PROCESSING_DESKTOP_CORE`,
`SIMPLEUI_PROCESSING_ANDROID_CORE`, `SIMPLEUI_ANDROID_API` and
`SIMPLEUI_D8_JAR` environment variables.

Create the Contribution Manager release files with:

```powershell
.\tools\build_release.ps1
```

The command produces `release/SimpleUI.zip`, `release/SimpleUI.txt` and
`release/SimpleUI.pdex`. The three stable filenames are used by Processing to
discover and update the library.

## Install for local testing

Copy this whole `SimpleUI` directory to the Processing sketchbook's `libraries`
folder. Processing will load `library/SimpleUI.jar`, list the sketches under
`examples`, and expose the initial HTML reference under `reference`.

The original copied projects under `TEST/processing` have not been modified.

## Authorship and license

SimpleUI and SimpleCore were designed and developed by
**[Luis López Martínez](https://github.com/LuislopezMartinez)**.
Copyright © 2026 Luis López Martínez.

The project is distributed under the MIT License. See [`LICENSE`](LICENSE) for
the complete license text. Source and binary redistributions must retain the
copyright and permission notice as required by that license.
