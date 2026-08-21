# Changelog

All notable changes to SimpleUI are documented in this file.

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
