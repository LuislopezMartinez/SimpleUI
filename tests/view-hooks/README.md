# UIView external hook test

This folder protects the public `UIView` subclass contract used by Processing
sketches.

- `DesktopUIViewHooksSmoke.java` runs outside the `simpleui.desktop` package
  and verifies `initialize()`, `onUIEvent()`, `update()`, show, hide and destroy.
- `AndroidUIViewHooksCompileSmoke.java` uses `@Override` outside the
  `simpleui.android` package. Compilation fails if the hooks lose public
  visibility.

Both checks are executed automatically by:

```powershell
.\build.ps1
```
