# SimpleUI for Processing Desktop and Android

Current version: **0.7.0**.

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

## Text editing and caret

`UITextField` and `UITextArea` display a blinking caret while focused. Click or
tap the text to place it, then insert or delete characters at that position.
Desktop keyboard navigation supports Left, Right, Home and End in both
controls; multiline areas additionally support Up and Down across explicit and
automatically wrapped lines.

When the caret moves beyond the visible portion of a control, a single-line
field adjusts its horizontal text window and a multiline area adjusts its
internal vertical scroll. On Android, the visible SimpleUI caret remains
synchronized with the selection maintained by the hidden native `EditText`, so
IME composition, prediction and cursor movement continue to use the operating
system keyboard bridge.

The common `UITextInputBase` API exposes `getCursorPosition()` and
`setCursorPosition(index)`. Positions use Java string indices and are clamped
to the current text length.

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

void settings() {
  size(1280, 720, P2D); // Use JAVA2D for the traditional Desktop renderer.
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.FIT);
}
```

Android initialization:

```java
import simpleui.android.*;

void settings() {
  fullScreen(P2D); // Use fullScreen() for Android2D.
}

void setup() {
  SimpleUI.initUI(this, "SansSerif", 18, UIScaleMode.RESPONSIVE);
  // Optional: keep a fixed 1280 x 720 virtual design on a full-screen device.
  SimpleUI.setMode(1280, 720, UIScaleMode.RESPONSIVE);
}
```

The renderer is selected explicitly by Processing before `setup()`. The same
SimpleUI JAR supports JAVA2D and P2D on Desktop, and Android2D and P2D in
Android Mode. SimpleUI does not attempt an unreliable runtime renderer switch.

## Virtual resolution

The required `UIScaleMode` passed to `initUI(...)` configures the initial
logical resolution from the current `width` and `height`. SimpleUI controls and
SimpleCore tasks share this viewport. `setMode(width, height, mode)` remains
available for runtime changes or a virtual resolution different from the
Processing surface; its two-argument overload uses `FIT`.

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
- `STRETCH`: fill the complete surface without cropping by using independent X
  and Y scales. Aspect ratio and visual proportions may be distorted.

Use `FIT` when the complete composition must remain visible, `FILL` when edge
cropping is acceptable, `RESPONSIVE` when a layout should use surplus space,
and `STRETCH` only when filling without margins or cropping matters more than
preserving proportions.

`UISwitch` is available on both platforms for compact boolean choices and
emits `changed` with a `boolean`. `UITable` displays a proportional vertical
scrollbar only when its rows overflow; it supports wheel/touch scrolling,
content dragging, thumb dragging and rail paging.

`initUI(...)` automatically registers input and rendering. The library clears
the surface with the current theme before the sketch frame and draws the UI
after it, so a UI-only sketch does not need `draw()`, mouse callbacks or
keyboard callbacks. Content written in the sketch's own `draw()` remains
behind the controls automatically.

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
  taskCore.setMode(400, 700, ViewportMode.FIT);
  new MyTask();
}

class MyTask extends Task {
  PFont font;

  protected void initialize() {
    font = createFont("SansSerif", 24);
    scale(1.5);
  }

  protected void frame() {
    if (key(_LEFT) || key(_A)) x -= 3;
    if (key(_RIGHT) || key(_D)) x += 3;
    text(font, 24, "Score: 100", LEFT, 20, 35, color(255), 255);
  }
  protected void onDestroy() {}
}
```

Every `Task` can use `key(_A)` through `key(_Z)`, arrows and common control
keys. `mouse.x` and `mouse.y` are logical viewport coordinates;
`mouse.left`, `mouse.right` and `mouse.center` report held buttons.

When SimpleUI is present, its `SimpleUI.setMode(...)` call also configures
SimpleCore. A game using only SimpleCore calls `taskCore.setMode(...)`.

Sprite size is intentionally concise inside a `Task`:

```java
scale(2);       // both axes
scalex(-1);     // horizontal size, negative values mirror the sprite
scaley(0.75);   // vertical size
```

Tasks provide rotated-box collision checks. `overlap(other)` compares two task
graphics, while `containsPoint(x, y)` tests a logical point against one task.
Both account for position, X/Y scale, reflection and angle.

Every Task also owns minimal named timers. They start on first use, return true
for one frame after the interval, and restart automatically:

```java
if (timer("shot", 300)) shoot();
if (timer("direction", 2000)) changeDirection();
```

`resetTimer(name)` starts an existing or new timer again from the current
instant. Timer names belong only to their Task and are discarded with it.

## Scenes and camera

Each `Scene` groups Tasks under one 2D camera. New Tasks join the active scene
automatically; Tasks in inactive scenes remain paused:

```java
Scene level = core.createScene().activate();
Player player = new Player();
new Enemy();

level.camera.setTarget(player);
level.camera.setDeadZone(280, 170, 240, 160);
level.camera.setBounds(0, 0, 1800, 900);
```

The dead zone uses logical screen coordinates. The target moves freely inside
it; on reaching an edge, the camera offset advances while every Task keeps its
real world position. Bounds prevent the camera from showing space outside the
world. `screenToWorldX/Y()` and `worldToScreenX/Y()` convert coordinates when
custom world interaction is needed. Tasks without a Scene remain global and
are not displaced by the camera, which is useful for HUD elements.

`Core.points` exposes every active contact as logical `TouchPoint` data on
Android, and one equivalent point while a Desktop mouse button is held. A Task
can capture the first contact inside its rotated graphic with `isTouched()`:

```java
if (isTouched()) {
  x += point.deltaX;
  y += point.deltaY;
}
```

The captured `point` keeps the same ID until that finger or mouse button is
released, even if it moves outside the graphic. Each point provides `id`,
`x/y`, `previousX/previousY`, `deltaX/deltaY`, `area` and `pressure`.

`setScale()` and `setAxisScale()` remain temporarily available for source
compatibility. New code should use the shorter methods above.

Text is drawn in logical coordinates and participates in the Task render order:

```java
text(font, 24, "Score: 100", LEFT, 20, 35, color(255), 255);
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

## Screen fading

`Core` draws screen transitions automatically after the sketch, tasks and UI.
Durations use milliseconds, and Processing color alpha is preserved:

```java
taskCore.fadeOff(color(0), 600); // cover the screen

if (taskCore.isFaded()) {
  changeScene();
  taskCore.fadeOn(600);          // reveal the new scene
}
```

`fadeOff()` and `fadeOn()` use a default duration of 500 ms. The one-argument
variants select a duration, while `fadeOff(color, duration)` also selects the
overlay color. `isFading()` is true only during a transition; `isFaded()` is
true once the screen has been completely covered.

## Images and fonts

Images and fonts can be loaded individually or by data-folder directory on
Desktop and Android. Folder results are ordered alphabetically and never
return `null`:

```java
PImage player = taskCore.loadImage("images/player.png");
PImage[] frames = taskCore.loadImages("images/walk");

PFont title = taskCore.loadFont("fonts/title.ttf", 32);
PFont[] fonts = taskCore.loadFonts("fonts", 18);
```

Images support PNG, JPG/JPEG, GIF and TGA. Fonts support TTF, OTF and VLW.
TTF/OTF files are created at the required logical size; VLW files retain their
prebuilt size. Unsupported or failed files are omitted from folder results.

## Audio

SimpleCore loads MP3, Ogg Vorbis and PCM WAV with the same API on Desktop and
Android. Desktop decoders are embedded in `SimpleUI.jar`; Android uses its
native media backend, so sketches do not need another Processing library.

```java
Sound music = taskCore.loadSound("music/theme.ogg");
music.setVolume(0.7f).setLoop(true).play();

music.pause();
music.resume();
music.setPosition(30_000); // milliseconds
music.stop();
```

`loadSounds(folder)` returns supported files in alphabetical order. A failed
load returns a safe `Sound` with `isLoaded() == false` and a diagnostic from
`getError()`. `Core.shutdown()` releases every sound automatically; use
`dispose()` only when a resource should be released earlier.

Existing PDE task subclasses must declare lifecycle overrides as `protected`
or `public`, because they now extend a class from a Java package. See the
`DesktopSimpleCore` and `AndroidSimpleCore` examples.

## Teaching examples

The `examples` directory includes a numbered course of 35 Desktop/Android
pairs: one sketch for every visual control (including `UISwitch`), practical
login, modal and view projects, and SimpleCore exercises covering lifecycle,
game entities, particles, fading, multitouch, drawing, keyboard and text.

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

On Windows, the build prefers the JDK bundled with Processing (currently JDK
17) so the generated Java 8 class files remain compatible with the older D8
version used by Processing for Android. A different compatible toolchain can be
selected with `SIMPLEUI_JAVA_HOME` or the `-JavaHome` build parameter.

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
