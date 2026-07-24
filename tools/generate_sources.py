from pathlib import Path
import re
import shutil


ROOT = Path(__file__).resolve().parents[2]
PROJECT = Path(__file__).resolve().parents[1]

SOURCES = {
    "desktop": ROOT / "processing" / "LoraNetBBS" / "SimpleUI.pde",
    "android": ROOT / "processing" / "LoraNetClientAndroid" / "SimpleUI.pde",
}
CALENDAR_SOURCES = (
    ROOT / "processing" / "LoraNetBBS" / "SimpleUICalendar.pde",
    ROOT / "processing" / "LoraNetClientAndroid" / "SimpleUICalendar.pde",
)


RUNTIME = r'''
    public static final String LIBRARY_AUTHOR = "Luis lopez martinez";
    public static final String LIBRARY_LICENSE = "MIT";

    private static PApplet app;
    private static EventBridge eventBridge;
    private static boolean automaticEventHandling = true;
    private static KeyEventInterceptor keyEventInterceptor;
    // Processing Android omits MouseEvent.WHEEL, while Desktop defines it as
    // action 8. Android never emits this action, but the shared bridge can
    // still compile against both Processing cores.
    private static final int MOUSE_ACTION_WHEEL = 8;

    private SimpleUI() {}

    public static void attach(PApplet host) {
        if (host == null) throw new IllegalArgumentException("SimpleUI requires a PApplet host");
        if (app != null && app != host) detach();
        app = host;
        syncHostState();
        installEventBridge();
    }

    public static void detach() {
        uninstallEventBridge();
        app = null;
    }

    public static void setAutomaticEventHandling(boolean enabled) {
        automaticEventHandling = enabled;
        if (enabled) installEventBridge();
        else uninstallEventBridge();
    }

    public static boolean isAutomaticEventHandlingEnabled() { return automaticEventHandling; }
    public static boolean isEventBridgeInstalled() { return eventBridge != null; }

    public interface KeyEventInterceptor {
        boolean interceptKeyEvent(int action, char key, int keyCode);
    }

    public static void setKeyEventInterceptor(KeyEventInterceptor interceptor) {
        keyEventInterceptor = interceptor;
    }

    private static void installEventBridge() {
        if (!automaticEventHandling || app == null || eventBridge != null) return;
        eventBridge = new EventBridge();
        app.registerMethod("mouseEvent", eventBridge);
        app.registerMethod("keyEvent", eventBridge);
        app.registerMethod("dispose", eventBridge);
    }

    private static void uninstallEventBridge() {
        if (app == null || eventBridge == null) return;
        app.unregisterMethod("mouseEvent", eventBridge);
        app.unregisterMethod("keyEvent", eventBridge);
        app.unregisterMethod("dispose", eventBridge);
        eventBridge = null;
    }

    public static final class EventBridge {
        private EventBridge() {}

        public void mouseEvent(MouseEvent event) {
            if (!automaticEventHandling || event == null) return;
            syncHostState();
            mouseX = event.getX();
            mouseY = event.getY();
            switch (event.getAction()) {
                case MouseEvent.PRESS: handleUIMousePressed(); break;
                case MouseEvent.DRAG: handleUIMouseDragged(); break;
                case MouseEvent.RELEASE: handleUIMouseReleased(); break;
                case MOUSE_ACTION_WHEEL: handleUIMouseWheel(event.getCount()); break;
                default: break;
            }
        }

        public void keyEvent(KeyEvent event) {
            if (!automaticEventHandling || event == null) return;
            syncHostState();
            key = event.getKey();
            keyCode = event.getKeyCode();
            if (keyEventInterceptor != null &&
                keyEventInterceptor.interceptKeyEvent(event.getAction(), key, keyCode)) return;
            switch (event.getAction()) {
                case KeyEvent.PRESS: handleUIKeyPressed(); break;
                case KeyEvent.TYPE: handleUIKeyTyped(); break;
                default: break;
            }
        }

        public void dispose() { detach(); }
    }

    public static void handleUIMouseWheel(float count) {
        if (activeUIModal != null && activeUIModal.isVisible()) return;
        if (uiModalVisible) return;

        float mx = getScaledMouseX();
        float myAnchored = mouseY / uiScale;
        float myScrolled = (mouseY - scrollState.currentY) / uiScale;
        UIElement hit = findTopMostInteractiveElementAt(mx, myAnchored, myScrolled);
        if (!(hit instanceof UITable) || !hit.hasScrollableOverflow()) return;

        UITable table = (UITable)hit;
        float visibleHeight = table.height - table.rowHeight;
        float contentHeight = table.rows.size() * table.rowHeight;
        float minimumScroll = min(0, visibleHeight - contentHeight);
        table.internalScrollY = constrain(
            table.internalScrollY - count * table.rowHeight * 3,
            minimumScroll,
            0
        );
    }

    public static PApplet host() {
        requireHost();
        return app;
    }

    public static void initUI(PApplet host, String fontName, int baseFontSize) {
        attach(host);
        initUI(fontName, baseFontSize);
    }

    public static void syncHostState() {
        requireHost();
        width = app.width;
        height = app.height;
        mouseX = app.mouseX;
        mouseY = app.mouseY;
        pmouseX = app.pmouseX;
        pmouseY = app.pmouseY;
        key = app.key;
        keyCode = app.keyCode;
        mousePressed = app.mousePressed;
    }

    private static void requireHost() {
        if (app == null) throw new IllegalStateException("Call SimpleUI.attach(this) or initUI(this, ...) first");
    }

    public static int width, height, mouseX, mouseY, pmouseX, pmouseY, keyCode;
    public static boolean mousePressed;
    public static char key;

    public static void pushMatrix() { app.pushMatrix(); }
    public static void popMatrix() { app.popMatrix(); }
    public static void pushStyle() { app.pushStyle(); }
    public static void popStyle() { app.popStyle(); }
    public static void translate(float x, float y) { app.translate(x, y); }
    public static void scale(float value) { app.scale(value); }
    public static void stroke(int value) { app.stroke(value); }
    public static void stroke(int value, float alpha) { app.stroke(value, alpha); }
    public static void stroke(int r, int g, int b) { app.stroke(r, g, b); }
    public static void strokeWeight(float value) { app.strokeWeight(value); }
    public static void noStroke() { app.noStroke(); }
    public static void fill(int value) { app.fill(value); }
    public static void fill(int value, float alpha) { app.fill(value, alpha); }
    public static void fill(float gray) { app.fill(gray); }
    public static void fill(float r, float g, float b) { app.fill(r, g, b); }
    public static void fill(float r, float g, float b, float a) { app.fill(r, g, b, a); }
    public static void noFill() { app.noFill(); }
    public static void rect(float a, float b, float c, float d) { app.rect(a, b, c, d); }
    public static void rect(float a, float b, float c, float d, float r) { app.rect(a, b, c, d, r); }
    public static void rect(float a, float b, float c, float d, float tl, float tr, float br, float bl) { app.rect(a, b, c, d, tl, tr, br, bl); }
    public static void ellipse(float a, float b, float c, float d) { app.ellipse(a, b, c, d); }
    public static void line(float a, float b, float c, float d) { app.line(a, b, c, d); }
    public static void text(String value, float x, float y) { app.text(value, x, y); }
    public static void text(String value, float x, float y, float w, float h) { app.text(value, x, y, w, h); }
    public static void text(char value, float x, float y) { app.text(value, x, y); }
    public static void text(int value, float x, float y) { app.text(value, x, y); }
    public static void textAlign(int horizontal) { app.textAlign(horizontal); }
    public static void textAlign(int horizontal, int vertical) { app.textAlign(horizontal, vertical); }
    public static void textSize(float size) { app.textSize(size); }
    public static void textFont(PFont font) { app.textFont(font); }
    public static PFont createFont(String name, float size) { return app.createFont(name, size); }
    public static PFont createFont(String name, float size, boolean smooth) { return app.createFont(name, size, smooth); }
    public static float textWidth(String value) { return app.textWidth(value); }
    public static void imageMode(int mode) { app.imageMode(mode); }
    public static void image(PImage image, float a, float b, float c, float d) { app.image(image, a, b, c, d); }
    public static void tint(float gray, float alpha) { app.tint(gray, alpha); }
    public static void noTint() { app.noTint(); }
    public static void clip(float a, float b, float c, float d) { app.clip(a, b, c, d); }
    public static void noClip() { app.noClip(); }
    public static int color(float gray) { return app.color(gray); }
    public static int color(float r, float g, float b) { return app.color(r, g, b); }
    public static int color(float r, float g, float b, float a) { return app.color(r, g, b, a); }
    public static int lerpColor(int a, int b, float amount) { return app.lerpColor(a, b, amount); }
    public static float alpha(int value) { return app.alpha(value); }
    public static float red(int value) { return app.red(value); }
    public static float green(int value) { return app.green(value); }
    public static float blue(int value) { return app.blue(value); }
    public static int millis() { return app.millis(); }

    public interface ModalResultHandler {
        void onModalResult(String actionId, boolean confirmed);
    }
    private static ModalResultHandler modalResultHandler;
    public static void setModalResultHandler(ModalResultHandler handler) { modalResultHandler = handler; }
    public static void handleModalResult(String actionId, boolean confirmed) {
        if (modalResultHandler != null) modalResultHandler.onModalResult(actionId, confirmed);
    }
'''

ANDROID_RUNTIME = r'''
    public static android.app.Activity getActivity() {
        requireHost();
        return app.getActivity();
    }
'''


TYPE_RE = re.compile(r"^(\s*)(abstract\s+class|class|interface|enum)\s+([A-Za-z_]\w*)")
METHOD_RE = re.compile(
    r"^(\s*)(?:(?:public|protected|private|static|final|abstract|synchronized)\s+)*"
    r"(?:[A-Za-z_$][\w$<>\[\],.? ]*\s+)?([A-Za-z_$][\w$]*)\s*\([^;]*\)\s*(?:\{\s*\}|\{|;)$"
)


def brace_delta(line: str) -> int:
    # The SimpleUI sources do not contain text blocks. Removing strings and line
    # comments is sufficient for tracking their declaration nesting.
    clean = re.sub(r'"(?:\\.|[^"\\])*"', '""', line)
    clean = re.sub(r"'(?:\\.|[^'\\])*'", "''", clean)
    clean = clean.split("//", 1)[0]
    return clean.count("{") - clean.count("}")


def paren_delta(line: str) -> int:
    clean = re.sub(r'"(?:\\.|[^"\\])*"', '""', line)
    clean = re.sub(r"'(?:\\.|[^'\\])*'", "''", clean)
    clean = clean.split("//", 1)[0]
    return clean.count("(") - clean.count(")")


def make_public_member(line: str, class_name: str) -> str:
    stripped = line.strip()
    if not stripped or stripped.startswith("//") or stripped.startswith("/*") or stripped.startswith("*"):
        return line
    if re.match(r"^(public|protected|private)\b", stripped):
        return re.sub(r"^(\s*)(protected|private)\b", r"\1public", line, count=1)
    if re.match(rf"^{re.escape(class_name)}\s*\(", stripped):
        return re.sub(r"^(\s*)", r"\1public ", line, count=1)
    if METHOD_RE.match(line):
        return re.sub(r"^(\s*)", r"\1public ", line, count=1)
    # Fields are deliberately public for source compatibility with the PDE
    # sketches, which access widget geometry and state directly.
    if ";" in stripped and not re.match(r"^(return|break|continue|throw|assert)\b", stripped):
        return re.sub(r"^(\s*)", r"\1public ", line, count=1)
    return line


def source_header(platform: str, include_simpleui_static: bool = False) -> list[str]:
    imports = [
        "/*",
        " * SimpleUI — designed and developed by Luis lopez martinez.",
        " * Copyright (c) 2026 Luis lopez martinez. Licensed under the MIT License.",
        " * SPDX-License-Identifier: MIT",
        " */",
        f"package simpleui.{platform};",
        "",
        "import java.util.*;",
        "import processing.core.*;",
        "import processing.event.*;",
        "import static processing.core.PApplet.*;",
        "import static processing.core.PConstants.*;",
    ]
    if include_simpleui_static:
        imports.append(f"import static simpleui.{platform}.SimpleUI.*;")
    if platform == "android":
        imports.extend([
            "import android.view.inputmethod.InputMethodManager;",
            "import android.view.inputmethod.EditorInfo;",
            "import android.content.Context;",
            "import android.view.View;",
            "import android.view.WindowManager;",
            "import android.text.InputType;",
            "import android.widget.EditText;",
            "import android.widget.FrameLayout;",
            "import android.text.TextWatcher;",
            "import android.text.Editable;",
            "import android.graphics.Rect;",
            "import android.view.ViewTreeObserver;",
            "import android.view.WindowInsets;",
            "import android.os.Build;",
        ])
    return imports + [""]


def split_types(lines: list[str]) -> tuple[list[str], dict[str, list[str]]]:
    globals_: list[str] = []
    types: dict[str, list[str]] = {}
    index = 0
    while index < len(lines):
        match = TYPE_RE.match(lines[index])
        if not match:
            globals_.append(lines[index])
            index += 1
            continue

        name = match.group(3)
        block = []
        depth = 0
        opened = False
        while index < len(lines):
            line = lines[index]
            block.append(line)
            delta = brace_delta(line)
            if "{" in line.split("//", 1)[0]:
                opened = True
            depth += delta
            index += 1
            if opened and depth == 0:
                break
        types[name] = block
    return globals_, types


def transform_globals(lines: list[str]) -> list[str]:
    depth = 0
    parentheses = 0
    output = []
    for original in lines:
        line = original
        if depth == 0 and parentheses == 0:
            stripped = line.strip()
            if stripped and not stripped.startswith(("//", "/*", "*", "*/")):
                if not re.match(r"^(public|protected|private)\b", stripped):
                    line = re.sub(r"^(\s*)", r"\1public static ", line, count=1)
                else:
                    line = re.sub(r"^(\s*)(public|protected|private)\s+", r"\1public static ", line, count=1)
        output.append(line)
        depth += brace_delta(original)
        parentheses += paren_delta(original)
    return output


def transform_type(name: str, lines: list[str]) -> list[str]:
    output = []
    depth = 0
    for index, original in enumerate(lines):
        line = original
        if index == 0:
            match = TYPE_RE.match(line)
            indent, kind, _ = match.groups()
            line = TYPE_RE.sub(f"{indent}public {kind} {name}", line, count=1)
        elif depth == 1:
            line = make_public_member(line, name)
        output.append(line)
        depth += brace_delta(original)
    return output


def transform(platform: str, source: Path, calendar_raw: str) -> dict[str, str]:
    raw = source.read_text(encoding="utf-8-sig") + "\n\n" + calendar_raw
    raw = re.sub(r"(?m)^\s*import\s+[^;]+;\s*$", "", raw)
    # In PDE, `color` is both an int-like type and a PApplet function. Convert
    # only the type form and leave calls such as color(255) intact.
    raw = re.sub(r"\bcolor\b(?!\s*\()", "int", raw)
    raw = re.sub(r"(?<![\w.])(\d+\.\d+)(?![\w.])", r"\1f", raw)
    if platform == "android":
        raw = re.sub(
            r"\s*if\s*\(e\s+instanceof\s+UICalendar\)\s*\{\s*\(\(UICalendar\)e\)\.performTapAction\(mx,\s*localY\);\s*return;\s*\}",
            "",
            raw,
            flags=re.S,
        )
    for entrypoint in (
        "updateUI", "drawUI", "drawUIContent", "drawUIOverlays",
        "handleUIMousePressed", "handleUIMouseDragged", "handleUIMouseReleased",
        "handleUIKeyPressed", "handleUIKeyTyped",
    ):
        raw = re.sub(
            rf"(?m)^(void\s+{entrypoint}\s*\(\s*\)\s*\{{)",
            rf"\1\n    syncHostState();",
            raw,
            count=1,
        )
    global_lines, types = split_types(raw.splitlines())
    imports = source_header(platform)
    imports.extend(["public final class SimpleUI {", RUNTIME])
    if platform == "android":
        imports.append(ANDROID_RUNTIME)
    result = {
        "SimpleUI": "\n".join(imports + transform_globals(global_lines) + ["}", ""])
    }
    for name, block in types.items():
        result[name] = "\n".join(source_header(platform, True) + transform_type(name, block) + [""])
    return result


def main() -> None:
    required_modern_types = ("enum UIScaleMode", "class UISwitch")
    for platform, source in SOURCES.items():
        source_text = source.read_text(encoding="utf-8-sig")
        missing = [name for name in required_modern_types if name not in source_text]
        if missing:
            raise RuntimeError(
                f"{source} is a legacy PDE input missing {', '.join(missing)}. "
                "Refusing to overwrite the canonical SimpleUI 0.5+ Java sources."
            )

    desktop_calendar = CALENDAR_SOURCES[0].read_text(encoding="utf-8-sig")
    android_calendar = CALENDAR_SOURCES[1].read_text(encoding="utf-8-sig")
    if desktop_calendar != android_calendar:
        raise RuntimeError("Desktop and Android SimpleUICalendar.pde copies have diverged")

    for platform, source in SOURCES.items():
        target_dir = PROJECT / "src" / "main" / "java" / "simpleui" / platform
        if target_dir.exists():
            shutil.rmtree(target_dir)
        target_dir.mkdir(parents=True, exist_ok=True)
        generated = transform(platform, source, desktop_calendar)
        for name, content in generated.items():
            target = target_dir / f"{name}.java"
            target.write_text(content, encoding="utf-8", newline="\n")
        print(f"generated {len(generated)} sources in {target_dir.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
