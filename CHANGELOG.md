# Changelog

All notable changes to SimpleUI are documented in this file.

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
