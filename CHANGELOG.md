# Changelog

All notable changes to SimpleUI are documented in this file.

## 0.7.2

- Adds the unified `setVideoMode(this, width, height, renderer)` call to
  `settings()` on Desktop and Android. Desktop creates the requested window;
  Android uses a full-screen surface with the requested logical resolution
  and infers its orientation from that resolution.
- Migrates every SimpleUI example and integration sketch to the unified
  initialization contract, removing Android's redundant initial `setMode()`.
- Adds the equivalent `Core.setVideoMode(...)` and
  `Core.start(this, ViewportMode)` contract for SimpleCore-only sketches and
  aligns all 26 Desktop/Android examples.
- Adds the required Android Mode metadata to all 39 bundled Android examples,
  so Processing selects Android Mode automatically when opening them.
- Adds a build-time validation that rejects any Android example without a
  valid root `sketch.properties` mode declaration.
- Fixes `Desktop31CoreFingerDraw` so drawing uses its own per-pointer history
  instead of frame-sensitive deltas and remains stable across window resizing.
- Makes `Core.points` structurally safe across Android's input and GL threads,
  preventing `ConcurrentModificationException` during multitouch rendering.
- Changes `Task.isTouched()` to return false as soon as its active contact
  leaves the rotated graphic, with automatic detection again on re-entry.
- Serializes structural Task registration, sorting, removal and rendering
  across Android input and GL threads, allowing large particle bursts from UI
  callbacks without `ConcurrentModificationException`.

## 0.7.0

- Makes the normal SimpleUI lifecycle fully automatic: `initUI()` clears the
  frame with the active theme, updates the controls and renders the interface
  through Processing lifecycle callbacks on Desktop and Android.
- Removes the need for UI-only sketches to declare `draw()` or call
  `background()` and `updateAndDrawUI()` manually.
- Keeps sketch drawing naturally behind the interface and prevents duplicate
  rendering while older manual calls are being migrated.
- Enables native Desktop key repetition for text fields, text areas and number
  fields, including repeated deletion and cursor navigation.
- Cleans all bundled examples and integration sketches around the minimal
  initialization contract and updates the complete manual accordingly.

## 0.6.1

- Adds automatic screen transitions through `Core.fadeOff`, `fadeOn`,
  `isFading` and `isFaded`, with configurable color, alpha and duration.
- Adds cross-platform `Core.loadImage/loadImages` and `loadFont/loadFonts`
  resource loading, with deterministic alphabetical folder results and an
  explicit required size for fonts.
- Adds unified multitouch through `Core.points`, `TouchPoint` and
  `Task.isTouched()`, including stable per-Task contact capture and a Desktop
  mouse fallback.
- Adds minimal per-Task named intervals through `timer(name, milliseconds)` and
  `resetTimer(name)`.
- Adds `Scene` and `Camera`, automatic Task membership in the active scene,
  inactive-scene pausing, camera targets, configurable dead zones, world bounds
  and screen/world coordinate conversion.

## 0.6.0

- Adds `Task.tint(color)` to tint a task graphic while preserving its original
  alpha channel and keeping task opacity independent.
- Adds rotated-box collision helpers `Task.overlap(task)` and
  `Task.containsPoint(x, y)`.
- Adds the cross-platform `Sound` API and `Core.loadSound/loadSounds` for MP3,
  Ogg Vorbis and PCM WAV, with automatic resource release on shutdown.

- Supports explicit renderer selection from Processing: JAVA2D or P2D on
  Desktop, and Android2D or P2D in Android Mode.
- Replaces the previous initialization overloads with the single required
  `initUI(this, fontName, fontSize, UIScaleMode)` entry point. The initial
  logical resolution comes directly from `size()` or `fullScreen()`.
- Keeps `setMode()` for runtime mode changes and virtual resolutions that
  differ from the physical Processing surface.
- Adds `STRETCH`, which fills the surface without cropping by applying
  independent X and Y scales. Drawing, input, scrolling, modals and SimpleCore
  share the same non-uniform coordinate conversion.
- Retains `FIT`, `FILL` and `RESPONSIVE`: FIT centers the complete design, FILL
  preserves aspect ratio and may crop, and RESPONSIVE exposes surplus logical
  space from the top-left origin.
- Removes the former Windows JAVA2D/AWT resize guard and renderer-specific
  initialization complexity.
- Reworks multiline text, lists, tables and chat overflow geometrically so UI
  widgets do not retain renderer clipping state while a surface is resized.
- Adds renderer, STRETCH coordinate, Desktop/Android parity and D8 regression
  checks, plus a comprehensive Desktop integration sketch.

## 0.5.7

- Adds automatic SimpleCore keyboard and pointer state with the classroom-friendly
  `key(_LEFT)`, `key(_A)` and `mouse.left/right/center` API inherited by every
  `Task`, shared across Desktop and Android.
- Unifies SimpleUI and SimpleCore under one FIT, FILL or RESPONSIVE virtual
  viewport, including scale, offsets, logical dimensions and coordinate conversion.
- Adds the classroom-oriented `scale()`, `scalex()` and `scaley()` Task methods.
  The older `setScale()` and `setAxisScale()` methods remain deprecated for
  temporary source compatibility.
- Adds `Task.text()` for viewport-scaled text rendered in Task z-order on
  Desktop and Android.
- Adds focused Desktop/Android teaching examples for `key()` and `Task.text()`.

## 0.5.6

- Adds a blinking text caret to focused `UITextField` and `UITextArea`
  controls on Desktop and Android.
- Allows placing the caret by clicking or tapping the nearest character.
- Inserts typed text at the caret and makes Backspace/Delete operate before or
  after the current position instead of always editing the end of the value.
- Adds Left, Right, Home and End navigation to single-line and multiline text
  controls, plus Up and Down navigation across explicit and wrapped lines in
  `UITextArea`.
- Keeps the caret visible by horizontally adjusting single-line text and
  vertically scrolling multiline content when necessary.
- Preserves source-text indices while wrapping multiline content, allowing
  caret movement to distinguish explicit newline characters from visual line
  wrapping.
- Synchronizes the SimpleUI caret with the hidden native Android `EditText`
  selection, including text changes made through IME composition and
  predictive keyboards.
- Adds public cursor-position accessors to `UITextInputBase` and a Desktop
  regression test for insertion, deletion, movement and wrapped-line indices.
- Restores Android D8 compatibility for the combined Desktop/Android JAR by
  keeping Windows AWT resize integration out of Android-resolved bytecode.
- Makes the build prefer Processing's bundled JDK 17 toolchain, avoiding class
  files from newer `javac` releases that older Processing Android/D8 versions
  cannot desugar reliably.
- Contains scrolled `UITextArea` text and its caret inside the padded content
  box. Desktop uses renderer clipping; Android filters partially visible lines
  geometrically to avoid the unsupported Android2D `noClip()` operation.

## 0.5.5

- Restores the Android dropdown open/closed arrow glyphs that had been
  corrupted into question marks.
- Prevents the Android native keyboard bridge from copying the previous
  control's text when focus changes between a single-line field and a
  multiline text area.
- Keeps native input reconfiguration muted until the newly focused control's
  own text has been synchronized.

## 0.5.4

- Improves Android keyboard activation by keeping the hidden native editor
  focusable inside the activity and retrying the IME request when necessary.
- Uses Android window insets and resize handling to keep the visible UI area
  synchronized with the software keyboard.

## 0.5.3

- Fixes Android dropdown row selection when tapping the expanded menu outside
  the control's closed bounds.
- Adds an executable Android regression test for expanded dropdown selection.

## 0.5.2

- Automatically protects resizable Windows Java2D sketches against the
  transient `Buffers have not been created` failure in Processing/AWT.
- Pauses rendering while AWT rebuilds its buffers and resumes after the window
  size stabilizes; serial and other background input remain independent.
- Keeps the resize guard exclusive to Desktop and inactive on Android, Linux,
  macOS and OpenGL renderers.

## 0.5.1

- Uses checked-in Java sources as the canonical source set.
- Adds matching Desktop and Android widget APIs.
- Adds calendar models and selection events.
- Adds responsive virtual-resolution modes.
- Adds automatic mouse and keyboard event forwarding.
- Adds `UISwitch`, table scrolling and the shared SimpleCore task engine.
- Adds Desktop, Android, lifecycle, view-hook and D8 compatibility checks.

## 0.5.0

- Introduced the unified Desktop, Android and SimpleCore distribution.
