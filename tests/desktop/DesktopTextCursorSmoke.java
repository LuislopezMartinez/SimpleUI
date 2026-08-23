import java.util.ArrayList;
import processing.core.PApplet;
import processing.opengl.PGraphics2D;
import simpleui.desktop.*;

public class DesktopTextCursorSmoke {
    private static final class FixedWidthArea extends UITextArea {
        FixedWidthArea() { super("area", 0, 0, 100, 80, "", 12); }
        public int charsThatFit(String source, float width) { return Math.min(2, source.length()); }
        public void ensureCursorVisible() {}
    }

    public static void main(String[] args) {
        PApplet host = new PApplet();
        host.g = new PGraphics2D();
        SimpleUI.attach(host);
        UITextField field = new UITextField("field", 0, 0, 100, 24, "", 12);
        field.setFocused(true);
        field.setText("abcd");
        field.setCursorPosition(2);
        field.appendPrintableChar('X');
        assertState("abXcd", 3, field.getText(), field.getCursorPosition());
        SimpleUI.keyCode = PApplet.BACKSPACE;
        field.keyPressed();
        assertState("abcd", 2, field.getText(), field.getCursorPosition());
        SimpleUI.keyCode = PApplet.DELETE;
        field.keyPressed();
        assertState("abd", 2, field.getText(), field.getCursorPosition());
        SimpleUI.keyCode = PApplet.LEFT;
        field.keyPressed();
        assertState("abd", 1, field.getText(), field.getCursorPosition());

        FixedWidthArea area = new FixedWidthArea();
        area.setText("abcd\nef");
        ArrayList<UITextArea.VisualLine> lines = area.visualLines(100);
        if (lines.size() != 3 || lines.get(0).start != 0 || lines.get(0).end != 2 ||
            lines.get(1).start != 2 || lines.get(1).end != 4 ||
            lines.get(2).start != 5 || lines.get(2).end != 7) {
            throw new AssertionError("Wrapped lines must retain source indices");
        }
        area.setFocused(true);
        area.setCursorPosition(2);
        area.appendPrintableChar('X');
        assertState("abXcd\nef", 3, area.getText(), area.getCursorPosition());
        SimpleUI.detach();
        System.out.println("Desktop text cursor editing passed.");
    }

    private static void assertState(String text, int cursor, String actualText, int actualCursor) {
        if (!text.equals(actualText) || cursor != actualCursor) {
            throw new AssertionError("Expected " + text + " @ " + cursor +
                ", got " + actualText + " @ " + actualCursor);
        }
    }
}
